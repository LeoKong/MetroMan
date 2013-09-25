package com.klcmetro;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class HelpActivity extends Activity {
	private TextView backButton;
	private TextView fAQView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_layout);

		backButton = (TextView) findViewById(R.id.back_btn);
		fAQView = (TextView) findViewById(R.id.FAQ_tv);
		
		fAQView.setMovementMethod(ScrollingMovementMethod.getInstance());

		String fAQString = "<big><font color=#000000>常见问题<br>Q：如何提高定位的准确度和实时刷新距离？<br></font><font color=#707070>A：保持网络畅通并开启GPS.<br><br></font>" +
				"<font color=#000000>Q：为什么我开启GPS但是不能跟随到我的位置?<br></font><font color=#707070>A：请在无建筑遮盖的地方(非室内)使用.<br><br></font>" +
				"<font color=#000000>Q：下载一个地铁线路图需要耗费多少流量？<br></font><font color=#707070>A：每个线路图仅需下载一次即可，大约只需要200K左右.<br><br></font>" +
				"<font color=#000000>Q：为什么有时会搜索失败？<br></font><font color=#707070>A：在网络信号差时会出项这种情况,请保持网络畅通.<br><br></font>" +
				"<font color=#000000>Q：地铁里使用为什么加载比较慢？<br></font><font color=#707070>A：因为在地铁里信号强度弱，这将影响网络数据的流通.<br><br></font>";

		fAQView.setText(Html.fromHtml(fAQString));

		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				HelpActivity.this.finish();
			}
		});
	}

}
