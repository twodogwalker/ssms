package nl.frankprins.ssms;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
	private static final String TAG = "SSMS-Debug";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "in onCreate()");
        setContentView(R.layout.main);
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "in onDestroy()");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "in onPause()");
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "in onResume()");
		super.onResume();
	}
}