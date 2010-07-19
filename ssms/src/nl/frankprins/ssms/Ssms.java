/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.frankprins.ssms;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.frankprins.ssms.ClickableListAdapter.ViewHolder;

/**
 *
 * @author Frank
 */
public class Ssms extends ListActivity {

  SmsBoxListAdapter smsBoxListAdapter;
  private static final String TAG = "SSMS-Debug";
  // TODO: create a settings menu
  // TODO: create a settings menu item, change theme
  // TODO: create a settings menu item, show ctc picture in list / body window

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.smsboxes);
    List boxes =
            new ArrayList(Arrays.asList(getResources().getStringArray(R.array.boxes_array)));
    ListView listView = getListView();
    smsBoxListAdapter = new SmsBoxListAdapter(this, R.layout.smsbox_row, boxes);
    listView.setAdapter(smsBoxListAdapter);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle item selection
    switch (item.getItemId()) {
      case R.id.new_sms:
        Log.v(TAG, "clicked new SMS option");
        Intent intent = new Intent(Ssms.this, NewSmsMessage.class);
        startActivity(intent);
        return true;
      case R.id.quit:
        Log.v(TAG, "clicked quit button, to be implemented");
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  static class MyViewHolder extends ViewHolder {

    TextView boxName;
    TextView totalNumMessages;

    public MyViewHolder(TextView name, TextView num) {
      boxName = name;
      totalNumMessages = num;
    }
  }

  private class SmsBoxListAdapter extends ClickableListAdapter {

    public SmsBoxListAdapter(Context context, int viewid, List<String> objects) {
      super(context, viewid, objects);
    }

    protected void bindHolder(ViewHolder h) {
      MyViewHolder mvh = (MyViewHolder) h;
      String box = (String) mvh.data;
      mvh.boxName.setText(box);
      int totalCount = Utilities.countNumberOfMessages(Ssms.this, box, false);
      int unreadCount = Utilities.countNumberOfMessages(Ssms.this, box, true);
      mvh.totalNumMessages.setText(totalCount + " messages, " + unreadCount + " unread.");
    }

    @Override
    protected ViewHolder createHolder(View v) {
      TextView name = (TextView) v.findViewById(R.id.boxname);
      TextView totalNum = (TextView) v.findViewById(R.id.num_total);
      ViewHolder mvh = new MyViewHolder(name, totalNum);
      v.setOnClickListener(new ClickableListAdapter.OnClickListener(mvh) {

        public void onClick(View v, ViewHolder viewHolder) {
          MyViewHolder mvh = (MyViewHolder) viewHolder;
          String box = (String) mvh.data;
          Log.v(TAG, "clicked row " + box);
          Intent intent = new Intent(Ssms.this, ViewBox.class);
          Bundle bundle = new Bundle();
          bundle.putString("nl.frankprins.ssms.showbox", box.toLowerCase());
          intent.putExtras(bundle);
          startActivity(intent);
        }
      });
      return mvh;
    }
  }
}
