package com.klcmetro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends Activity {
	private TextView backButton;
	private TextView proTextView, termsTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);

		proTextView = (TextView) findViewById(R.id.produce);
		termsTextView = (TextView) findViewById(R.id.terms);
		backButton = (TextView) findViewById(R.id.back_btn);

		proTextView
				.setText(Html
						.fromHtml("本产品由<font color=#2E5F93><a href='http://weibo.com/btmovie'>@最新电影下载频道</a></font>出品"));
		
		proTextView.setMovementMethod(LinkMovementMethod.getInstance());
		
		termsTextView.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(AboutActivity.this, TermsActivity.class);
				startActivity(intent);
			}
		});

		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				AboutActivity.this.finish();
			}
		});
	}

}
