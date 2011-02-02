package nl.frankprins.ssms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends MainActivity {
	private static final String TAG = "SSMS-Debug";
	private static final String INBOX = "inbox";
	private static final String SENDBOX = "sent";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		Button inboxBtn = (Button) findViewById(R.id.btn_inbox);
		inboxBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						ViewBoxActivity.class);
				intent.putExtra("BOX", INBOX);
				startActivity(intent);
			}
		});
		Button sendboxBtn = (Button) findViewById(R.id.btn_send);
		sendboxBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),
						ViewBoxActivity.class);
				intent.putExtra("BOX", SENDBOX);
				startActivity(intent);
			}
		});
	}

}