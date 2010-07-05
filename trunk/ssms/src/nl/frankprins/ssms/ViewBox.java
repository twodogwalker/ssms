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
import android.view.View;
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
public class ViewBox extends ListActivity {

  private static final String TAG = "SSMS-Debug";
  private ArrayList<SmsMessage> messageList = null;
  MyClickableListAdapter myAdapter;
  private ProgressDialog m_ProgressDialog = null;
  private Runnable viewMessages;
  private final String dateFormat = "MMM dd, yyyy HH:mm";
  Format formatter = new SimpleDateFormat(dateFormat);

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
    final String boxName = box;
    setTitle(boxToView);
    final Uri boxUri = Uri.parse("content://sms/" + boxToView);
    messageList = new ArrayList<SmsMessage>();
    // load messages
    viewMessages = new Runnable() {

      @Override
      public void run() {
        getMessages(boxName, boxUri);
      }
    };
    Thread thread = new Thread(null, viewMessages, "MagentoBackground");
    thread.start();
    m_ProgressDialog = ProgressDialog.show(ViewBox.this, "", "loading...", true);
    myAdapter = new MyClickableListAdapter(this, R.layout.listrow, messageList, boxToView);
    this.getListView().setFastScrollEnabled(true);
    setListAdapter(myAdapter);
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

  private void getMessages(String boxName, Uri boxUri) {
    int count = 0;
    messageList = new ArrayList<SmsMessage>();
    Cursor cursor = getContentResolver().query(boxUri,
            new String[]{"_id", "thread_id", "address", "person", "date", "body"}, null, null, null);
    if (cursor != null) {
      count = cursor.getCount();
      if (count > 0) {
        while (cursor.moveToNext()) {
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
          messageList.add(m);
        }
      }
    }
    cursor.close();
    runOnUiThread(returnRes);
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
      Log.d(TAG, String.valueOf(message.getBody().length()));
      if(message.getBody().length() > 36) {
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
