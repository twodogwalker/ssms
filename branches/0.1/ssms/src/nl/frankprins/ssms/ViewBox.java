/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.frankprins.ssms;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import nl.frankprins.ssms.ClickableListAdapter.ViewHolder;

/**
 *
 * @author Frank
 */
public class ViewBox extends ListActivity implements OnScrollListener {

  private static final String TAG = "SSMS-Debug";
  private ArrayList<SmsMessage> messageList = null;
  MyClickableListAdapter myAdapter;
  private ProgressDialog m_ProgressDialog = null;
  private Runnable viewMessages;
// TODO put default listsize into preferences
  private int listSize = 15, shown = 0;
  //TODO put date format into preferences
  private final String dateFormat = "MMM dd, yyyy HH:mm";
  Format formatter = new SimpleDateFormat(dateFormat);
  String boxName = "";
  Uri boxUri = null;
  Cursor cursor;
  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 250;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;
  private GestureDetector gestureDetector;
  View.OnTouchListener gestureListener;
  ListView lv;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.box);
    Bundle extras = getIntent().getExtras();
    String box = "", boxToView = "";
    if (extras != null) {
      box = extras.getString("nl.frankprins.ssms.showbox");
      if (box != null) {
        boxToView = box;
      }
    }
    boxName = box;
    setTitle(boxToView);
    boxUri = Uri.parse("content://sms/" + boxToView);
    messageList = new ArrayList<SmsMessage>();
    cursor = getContentResolver().query(boxUri,
            new String[]{"_id", "thread_id", "address", "person", "date", "body"}, null, null, null);
    // load messages
    viewMessages = new Runnable() {

      @Override
      public void run() {
        List messages = getMessages(boxName, 0);
        messageList.addAll(messages);
        shown = messageList.size();
      }
    };
    Thread thread = new Thread(null, viewMessages, "MagentoBackground");
    thread.start();
    m_ProgressDialog = ProgressDialog.show(ViewBox.this, "", "loading...", true);
    myAdapter = new MyClickableListAdapter(this, R.layout.listrow, messageList, boxToView);
    setListAdapter(myAdapter);
    lv = getListView();
    lv.setOnScrollListener(this);
//    // Gesture detection
    gestureDetector = new GestureDetector(new MyGestureDetector());
    gestureListener = new View.OnTouchListener() {

      public boolean onTouch(View v, MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
          return true;
        }
        return false;
      }
    };
    lv.setOnTouchListener(gestureListener);

  }
  private Runnable returnRes = new Runnable() {

    @Override
    public void run() {
      if (messageList != null && messageList.size() > 0) {
        myAdapter.notifyDataSetChanged();
      }
      m_ProgressDialog.dismiss();
      myAdapter.notifyDataSetChanged();
    }
  };

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && !cursor.isClosed()) {
      cursor.close();
    }
    return super.onKeyDown(keyCode, event);
  }

  private ArrayList<SmsMessage> getMessages(String boxName, int start) {
    ArrayList<SmsMessage> requestList = new ArrayList<SmsMessage>();
    int count = 0, size = 0;
    if (cursor != null) {
      count = cursor.getCount();
      if (count > 0) {
        cursor.moveToPosition(start - 1);
        while (cursor.moveToNext() && size <= listSize) {
          size++;
          SmsMessage m = new SmsMessage();
          m.setMessageId(cursor.getLong(0));
          m.setThreadId(cursor.getLong(1));
          String number = cursor.getString(2);
          m.setAddress(number);
          String contactName = Utilities.getPersonNameFromNumber(this, boxName, number);
          m.setAddressName(contactName);
          m.setContactId(cursor.getLong(3));
          m.setTimestamp(cursor.getLong(4));
          m.setBody(cursor.getString(5));
          requestList.add(m);
        }
      }
    }
    //cursor.close();
    runOnUiThread(returnRes);
    return requestList;
  }

  public void onScrollStateChanged(AbsListView view, int scrollState) {
    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
      int first = view.getFirstVisiblePosition();
      int count = view.getChildCount();
      if (first + count >= shown) {
        // load messages
        viewMessages = new Runnable() {

          @Override
          public void run() {
            List messages = getMessages(boxName, shown);
            messageList.addAll(messages);
            shown = messageList.size();
          }
        };
        Thread thread = new Thread(null, viewMessages, "MagentoBackground");
        thread.start();
        m_ProgressDialog = ProgressDialog.show(ViewBox.this, "", "loading...", true);
      }
    }
  }

  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
          int totalItemCount) {
  }

  public class MyGestureDetector extends SimpleOnGestureListener {

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      try {
        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
          return false;
        }

        // right to left swipe
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
          int x = (int) e1.getX();
          int y = (int) e1.getY();
          SmsMessage m = (SmsMessage) myAdapter.getItem(lv.pointToPosition(x, y));
//          // TODO: make a friendlier looking window for the body
          Builder adb = new AlertDialog.Builder(ViewBox.this);
          adb.setTitle(m.getAddressName());
          adb.setMessage(m.getBody());
          adb.setPositiveButton("close", null);
          adb.show();
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
          int x = (int) e1.getX();
          int y = (int) e1.getY();
          SmsMessage m = (SmsMessage) myAdapter.getItem(lv.pointToPosition(x, y));
          Intent intent = new Intent(ViewBox.this, NewSmsMessage.class);
          Bundle bundle = new Bundle();
          bundle.putString("nl.frankprins.ssms.replynumber", m.getAddress());
          intent.putExtras(bundle);
          startActivity(intent);
        }
      } catch (Exception e) {
        // nothing
      }
      return false;
    }
  }

  static class MyViewHolder extends ViewHolder {

    TextView firstRow;
    TextView secondRow;

    public MyViewHolder(TextView first, TextView second) {
      firstRow = first;
      secondRow = second;
    }
  }

  private class MyClickableListAdapter extends ClickableListAdapter {

    private boolean incoming;

    public MyClickableListAdapter(Context context, int viewid, List<SmsMessage> objects,
            String box) {
      super(context, viewid, objects);
      incoming = box.equalsIgnoreCase("Inbox") ? true : false;
    }

    protected void bindHolder(ViewHolder h) {
      MyViewHolder mvh = (MyViewHolder) h;
      SmsMessage message = (SmsMessage) mvh.data;
      String namePreFix = "-";
      if (incoming) {
        namePreFix = getResources().getString(R.string.messageListNamePrefixFrom);
      } else {
        namePreFix = getResources().getString(R.string.messageListNamePrefixTo);
      }
      mvh.firstRow.setText(namePreFix + ": " + message.getAddressName()
              + ", on:" + formatDate(message.getTimestamp()));
      if (message.getBody().length() > 36) {
        mvh.secondRow.setText(message.getBody().substring(0, 35));
      } else {
        mvh.secondRow.setText(message.getBody());
      }

    }

    @Override
    protected ViewHolder createHolder(View v) {
      TextView firstRow = (TextView) v.findViewById(R.id.toptext);
      TextView secondRow = (TextView) v.findViewById(R.id.bottomtext);
      ViewHolder mvh = new MyViewHolder(firstRow, secondRow);
      return mvh; // finally, we return our new holder
    }

    private String formatDate(long timestamp) {
      if (timestamp > 0) {
        Date date = new Date(timestamp);
        return formatter.format(date);
      }
      return null;
    }
  }
}
