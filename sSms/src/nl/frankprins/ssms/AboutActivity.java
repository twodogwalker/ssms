package nl.frankprins.ssms;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutActivity extends MainActivity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.about);
    TextView aboutMsg = (TextView) findViewById(R.id.about_msg);
    Animation fadeIn1 = AnimationUtils.loadAnimation(this, R.anim.about_anim_msg);
    aboutMsg.startAnimation(fadeIn1);
    LinearLayout ctcLayout = (LinearLayout) findViewById(R.id.about_bottomlayout);
    Animation fadeIn2 = AnimationUtils.loadAnimation(this, R.anim.about_anim_ctc);
    ctcLayout.startAnimation(fadeIn2);
  }
}
