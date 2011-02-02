package nl.frankprins.ssms;

import nl.frankprins.ssms.ClickableListAdapter.ViewHolder;
import android.widget.TextView;

public class MsgViewHolder extends ViewHolder {

	TextView firstRow;
	TextView secondRow;

	public MsgViewHolder(TextView first, TextView second) {
		firstRow = first;
		secondRow = second;
	}
}
