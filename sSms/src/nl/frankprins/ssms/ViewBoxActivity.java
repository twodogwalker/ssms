package nl.frankprins.ssms;

import android.os.Bundle;
import android.widget.EditText;

public class ViewBoxActivity extends MainActivity {
	private int box = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewbox);
		box = getIntent().getIntExtra("BOX", 1);
		EditText label = (EditText) findViewById(R.id.lbl_box);
		label.setText(String.valueOf(box));
	}

}
