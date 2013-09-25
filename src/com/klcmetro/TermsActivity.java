package com.klcmetro;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TermsActivity extends Activity {
	private TextView backButton;

	private TextView termView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terms_layout);
		
		backButton=(TextView) findViewById(R.id.back_btn);
		termView=(TextView) findViewById(R.id.terms_tv);
		
		termView.setMovementMethod(new ScrollingMovementMethod());
		
		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				TermsActivity.this.finish();
			}
		});
		
	}

}
