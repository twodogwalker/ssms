package nl.frankprins.ssms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	public static final String TAG = "SSMS-Debug";
	public static final String SSMS_PREFS = "sSmsPrefs";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SharedPreferences settings = getSharedPreferences(SSMS_PREFS, MODE_PRIVATE);
    // read last use
    Long lastUse = new Long(0);
    if (settings.contains("lastUse") == true) {
      lastUse = settings.getLong("lastUse", 0);
    }
    Date lastDate = new Date(lastUse);
    DateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");
    Log.i(TAG, "last startup: " + df.format(lastDate));
    
    // write new value
    SharedPreferences.Editor prefsEditor = settings.edit();
    Date now = new Date();
    prefsEditor.putLong("lastUse", now.getTime());
    prefsEditor.commit();
  }
	
}