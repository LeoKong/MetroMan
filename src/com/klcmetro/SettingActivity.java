package com.klcmetro;

import com.klcutil.Selectpopwindow;
import com.klcutil.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingActivity extends Activity implements OnClickListener {
	private TextView backButton;
	private TextView searchAreaView, busAreaView;
	private CheckBox keepCheckBox, promptGpsCheckBox;

	SharedPreferences preferences;

	private RelativeLayout searchingAreaBtn, busAreaBtn, shareBtn, aboutBtn;

	Selectpopwindow popwindow;

	private int searchA, busA;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_laytout);

		// 初始化控件
		initWidget();

		// 读取设置信息
		preferences = this
				.getSharedPreferences("setting", Context.MODE_PRIVATE);

		// 读取搜索范围并显示
		searchA = Utils.getSearchArea(preferences);
		busA = Utils.getBusArea(preferences);
		searchAreaView.setText(searchA + "米");
		// 读取公车提示范围并显示
		if (busA == 0) {
			busAreaView.setText("不提示");
		} else {
			busAreaView.setText(busA + "米");
		}
		// 读取是否保持屏幕
		keepCheckBox.setChecked(Utils.getKeepScreen(preferences));
		//读取是否提醒GPS
		promptGpsCheckBox.setChecked(Utils.getPromptGps(preferences));

	}

	public void initWidget() {
		backButton = (TextView) findViewById(R.id.back_btn);
		searchingAreaBtn = (RelativeLayout) findViewById(R.id.setting_searchingarea_btn);
		busAreaBtn = (RelativeLayout) findViewById(R.id.setting_busarea_btn);
		aboutBtn = (RelativeLayout) findViewById(R.id.about_btn);
		shareBtn = (RelativeLayout) findViewById(R.id.share_btn);
		searchAreaView = (TextView) findViewById(R.id.searching_area_tv);
		busAreaView = (TextView) findViewById(R.id.bus_area_tv);
		keepCheckBox = (CheckBox) findViewById(R.id.keep_checkbox);
		promptGpsCheckBox = (CheckBox) findViewById(R.id.promptGps_checkbox);

		backButton.setOnClickListener(this);
		searchingAreaBtn.setOnClickListener(this);
		busAreaBtn.setOnClickListener(this);
		aboutBtn.setOnClickListener(this);
		shareBtn.setOnClickListener(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// 进行文件保存
		saveSetting(searchA, busA, keepCheckBox.isChecked(),
				promptGpsCheckBox.isChecked());
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_btn:
			SettingActivity.this.finish();
			break;

		case R.id.setting_searchingarea_btn:
			// 是实例化弹出窗体
			popwindow = new Selectpopwindow(SettingActivity.this,
					itemsOnClick1, "1000米", "3000米", "5000米", "8000米");

			// 设置窗体显示位置
			popwindow.showAtLocation(
					SettingActivity.this.findViewById(R.id.setting),
					Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

			break;

		case R.id.setting_busarea_btn:

			// 是实例化弹出窗体
			popwindow = new Selectpopwindow(SettingActivity.this,
					itemsOnClick2, "不提示", "1000米", "1500米", "2000米");

			// 设置窗体显示位置
			popwindow.showAtLocation(
					SettingActivity.this.findViewById(R.id.setting),
					Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

			break;

		case R.id.share_btn:
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("image/*");
			// 设置邮件默认标题
			intent.putExtra(Intent.EXTRA_SUBJECT, "我发现了一个很不多的APP！");
			// 分享的内容
			intent.putExtra(Intent.EXTRA_TEXT,
					"我正在使用由@最新电影下载频道 出品的APP--地铁小助手，出门乘搭地铁必备，在安卓市场搜索“地铁小助手”进行下载！");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// gettitle获取app的名字，显示在最上面
			startActivity(Intent.createChooser(intent, getTitle()));

			break;

		case R.id.about_btn:
			Intent intent2 = new Intent(SettingActivity.this,
					AboutActivity.class);
			startActivity(intent2);
		}
	}

	// 为弹出窗口1实现监听类
	private OnClickListener itemsOnClick1 = new OnClickListener() {

		public void onClick(View v) {
			Button btn = (Button) v;
			searchAreaView.setText(btn.getText());
			popwindow.dismiss();
			switch (v.getId()) {
			case R.id.popitem1:
				searchA = 1000;
				break;
			case R.id.popitem2:
				searchA = 3000;
				break;
			case R.id.popitem3:
				searchA = 5000;
				break;
			case R.id.popitem4:
				searchA = 8000;
				break;
			}
		}
	};

	// 为弹出窗口2实现监听类
	private OnClickListener itemsOnClick2 = new OnClickListener() {

		public void onClick(View v) {
			Button btn = (Button) v;
			busAreaView.setText(btn.getText());
			popwindow.dismiss();
			switch (v.getId()) {
			case R.id.popitem1:
				busA = 0;
				break;
			case R.id.popitem2:
				busA = 1000;
				break;
			case R.id.popitem3:
				busA = 1500;
				break;
			case R.id.popitem4:
				busA = 2000;
				break;
			}
		}
	};


	public void saveSetting(int searchArea, int busArea, boolean keep,
			boolean prompt) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("searchArea", searchArea);
		editor.putInt("busArea", busArea);
		editor.putBoolean("keepScreen", keep);
		editor.putBoolean("promptGps", prompt);
		editor.commit();
	}

}
