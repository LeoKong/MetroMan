package com.klcmetro;

import com.klcmetro.R;
import com.klcutil.Utils;
import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final int TERMS = 1;

	public static final String PUBLISHER_ID = "56OJzRw4uNWX0U4Qfe";
	public static final String InlinePPID = "16TLmKWlApynHNUHj0AXP0Ei";
	public static final String InterstitialPPID = "16TLmKWlApynHNUHYp610YPs";

	private LinearLayout nearButton, searchButton, mapButton, settingButton,
			helpButton, feedbackButton;

	SharedPreferences sharedPreferences;

	// ��ȡ��ǰ�汾��
	int Vcode = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// ��ʾ�û����»ظ�
		UMFeedbackService.enableNewReplyNotification(this,
				NotificationType.NotificationBar);

		// ��ʼ���ؼ�
		initWidget();
		
		sharedPreferences = this.getSharedPreferences("setting",
				Context.MODE_PRIVATE);

		if (Utils.getPromptGps(sharedPreferences)) {
			if (!Utils.checkGPS(MainActivity.this)) {
				showDialog(Utils.OPENGPS);
				System.out.println("û��gps");
			}
		}

		if (!Utils.checkNet(MainActivity.this)) {
			showDialog(Utils.OPENNET);
			System.out.println("û������");
		}

		try {
			Vcode = getPackageManager()
					.getPackageInfo(this.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// �����ȡ�ı���İ汾���뵱ǰ�汾�Ų�ͬ��˵����Ҫ��ʾ������������
		if (getVersionCode() != Vcode) {
			showDialog(TERMS);
		}

	}

	public void initWidget() {
		nearButton = (LinearLayout) findViewById(R.id.near_btn);
		searchButton = (LinearLayout) findViewById(R.id.search_btn);
		mapButton = (LinearLayout) findViewById(R.id.map_btn);
		settingButton = (LinearLayout) findViewById(R.id.setting_btn);
		helpButton = (LinearLayout) findViewById(R.id.help_btn);
		feedbackButton = (LinearLayout) findViewById(R.id.feedback_btn);

		nearButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		mapButton.setOnClickListener(this);
		settingButton.setOnClickListener(this);
		helpButton.setOnClickListener(this);
		feedbackButton.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// ��������ͳ��
		// MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// ֹͣ����ͳ��
		// MobclickAgent.onPause(this);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setCancelable(false);

		switch (id) {
		case Utils.OPENGPS:
			builder.setTitle("��ߡ��ҵ�λ�á��ľ�ȷ��");
			builder.setMessage("��û�д�GPS����,��GPS���ṩ��׼ȷ�Ķ�λ��");
			final CheckBox checkBox=new CheckBox(this);
			checkBox.setText("������ʾ");
			builder.setView(checkBox);
			builder.setPositiveButton("����",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
							Utils.setPromptGps(sharedPreferences, !checkBox.isChecked());
						}
					});
			builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Utils.setPromptGps(sharedPreferences, !checkBox.isChecked());
				}
			});
			return builder.create();

		case Utils.OPENNET:
			builder.setTitle("�޿��õ���������");
			builder.setMessage("�뿪��GPRS��WIFI��������!");
			builder.setPositiveButton("������",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							startActivity(new Intent(Settings.ACTION_SETTINGS));
						}
					});
			builder.setNegativeButton("ȡ��", null);
			return builder.create();

		case TERMS:
			builder.setTitle("����С���ַ�������");
			builder.setMessage(getString(R.string.clause));
			builder.setPositiveButton("ͬ��",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							setVersionCode(Vcode);
						}
					});
			builder.setNegativeButton("�ܾ�",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							MainActivity.this.finish();
						}
					});
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.near_btn:
			intent.setClass(MainActivity.this, RecommandActivity.class);
			startActivityForResult(intent, 1);
			break;

		case R.id.map_btn:
			intent.setClass(MainActivity.this, MetroMapActivity.class);
			startActivity(intent);
			break;

		case R.id.search_btn:
			intent.setClass(MainActivity.this, SearchInMapActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.setting_btn:
			intent.setClass(MainActivity.this, SettingActivity.class);
			startActivity(intent);
			break;

		case R.id.help_btn:
			intent.setClass(MainActivity.this, HelpActivity.class);
			startActivity(intent);
			break;

		case R.id.feedback_btn:
			// �����������
			UMFeedbackService.openUmengFeedbackSDK(this);
			break;
		}

	}

	// ��ȡ�������ļ��еİ汾��
	public int getVersionCode() {
		return sharedPreferences.getInt("vcode", 0);
	}

	/**
	 * ����汾��
	 * 
	 * @param code
	 */
	public void setVersionCode(int code) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("vcode", code);
		editor.commit();
	}

	// ʵ���ٵ�һ���˳�����
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast toast = Toast.makeText(MainActivity.this, "�ٰ�һ���˳�����",
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();

				exitTime = System.currentTimeMillis();
			} else {
				MainActivity.this.finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
