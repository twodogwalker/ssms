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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;
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
    startManagingCursor(cursor);
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
    this.getListView().setFastScrollEnabled(true);
    setListAdapter(myAdapter);
    getListView().setOnScrollListener(this);
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
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
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
      v.setOnClickListener(new ClickableListAdapter.OnClickListener(mvh) {

        public void onClick(View v, ViewHolder viewHolder) {
          MyViewHolder mvh = (MyViewHolder) viewHolder;
          SmsMessage m = (SmsMessage) mvh.data;
          // TODO: make a friendlier looking window for the body
          Builder adb = new AlertDialog.Builder(ViewBox.this);
          adb.setTitle(m.getAddressName());
          adb.setMessage(m.getBody());
          adb.setPositiveButton("close", null);
          adb.show();
        }
      });

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
