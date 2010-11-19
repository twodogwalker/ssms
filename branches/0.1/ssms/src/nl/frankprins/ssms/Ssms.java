/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.frankprins.ssms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 *
 * @author Frank
 */
public class Ssms extends Activity {

  private static final String TAG = "SSMS-Debug";
  // TODO: create a settings menu
  // TODO: create a settings menu item, change theme
  // TODO: create a settings menu item, show ctc picture in list / body window

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.dashboard);
    Button inboxBtn = (Button) findViewById(R.id.inBtn);
    inboxBtn.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        Intent intent = new Intent(Ssms.this, ViewBox.class);
        Bundle bundle = new Bundle();
        bundle.putString("nl.frankprins.ssms.showbox", "inbox");
        intent.putExtras(bundle);
        startActivity(intent);
      }
    });
    Button sentBtn = (Button) findViewById(R.id.outBtn);
    sentBtn.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        Intent intent = new Intent(Ssms.this, ViewBox.class);
        Bundle bundle = new Bundle();
        bundle.putString("nl.frankprins.ssms.showbox", "sent");
        intent.putExtras(bundle);
        startActivity(intent);
      }
    });
    Button newBtn = (Button) findViewById(R.id.newBtn);
    newBtn.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {
        Log.v(TAG, "clicked new SMS button");
        Intent intent = new Intent(Ssms.this, NewSmsMessage.class);
        startActivity(intent);
      }
    });
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
}
