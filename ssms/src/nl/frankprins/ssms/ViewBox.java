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

  private ArrayList<SmsMessage> messageList = null;
  MyClickableListAdapter myAdapter;
  private ProgressDialog m_ProgressDialog = null;
  private Runnable viewMessages;
  private final String dateFormat = "MMM dd, yyyy HH:mm:ss";
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
    setTitle(boxToView);
    final Uri boxUri = Uri.parse("content://sms/" + boxToView);
    messageList = new ArrayList<SmsMessage>();
    // load messages
    viewMessages = new Runnable() {

      @Override
      public void run() {
        getMessages(boxUri);
      }
    };
    Thread thread = new Thread(null, viewMessages, "MagentoBackground");
    thread.start();
    m_ProgressDialog = ProgressDialog.show(ViewBox.this, "", "loading...", true);
    myAdapter = new MyClickableListAdapter(this, R.layout.listrow, messageList);
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

  private void getMessages(Uri boxUri) {
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
          String contactName = Utilities.getPersonNameFromNumber(this, number);
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

    public MyClickableListAdapter(Context context, int viewid, List<SmsMessage> objects) {
      super(context, viewid, objects);
    }

    protected void bindHolder(ViewHolder h) {
      MyViewHolder mvh = (MyViewHolder) h;
      SmsMessage message = (SmsMessage) mvh.data;
      mvh.firstRow.setText("Date: " + formatDate(message.getTimestamp()));
      mvh.secondRow.setText("Phone: " + message.getAddressName());
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
