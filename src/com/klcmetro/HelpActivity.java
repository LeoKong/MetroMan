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

		String fAQString = "<big><font color=#000000>��������<br>Q�������߶�λ��׼ȷ�Ⱥ�ʵʱˢ�¾��룿<br></font><font color=#707070>A���������糩ͨ������GPS.<br><br></font>" +
				"<font color=#000000>Q��Ϊʲô�ҿ���GPS���ǲ��ܸ��浽�ҵ�λ��?<br></font><font color=#707070>A�������޽����ڸǵĵط�(������)ʹ��.<br><br></font>" +
				"<font color=#000000>Q������һ��������·ͼ��Ҫ�ķѶ���������<br></font><font color=#707070>A��ÿ����·ͼ��������һ�μ��ɣ���Լֻ��Ҫ200K����.<br><br></font>" +
				"<font color=#000000>Q��Ϊʲô��ʱ������ʧ�ܣ�<br></font><font color=#707070>A���������źŲ�ʱ������������,�뱣�����糩ͨ.<br><br></font>" +
				"<font color=#000000>Q��������ʹ��Ϊʲô���رȽ�����<br></font><font color=#707070>A����Ϊ�ڵ������ź�ǿ�������⽫Ӱ���������ݵ���ͨ.<br><br></font>";

		fAQView.setText(Html.fromHtml(fAQString));

		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				HelpActivity.this.finish();
			}
		});
	}

}
