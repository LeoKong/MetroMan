package com.klcutil;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.Preference;

public class Utils {
	public static final int OPENNET = 8;
	public static final int OPENGPS = 9;

	private static WakeLock wl;
	private static PowerManager pm;

	/**
	 * ������Ļ����״̬���������Ʋ�Ϩ��
	 * 
	 * @param context
	 * @param on
	 *            �Ƿ񱣳ֳ���
	 */
	public static void keepScreenOn(Context context, boolean on) {
		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyLock");
		wl.setReferenceCounted(false);// ��䲻����
		if (on) {
			wl.acquire();
		} else {
			wl.release();
			wl = null;
		}
	}

	/**
	 * ����Ƿ�����GPS
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkGPS(Context context) {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		return locationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
	}

	/**
	 * ����Ƿ���������
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNet(Context context) {
		try {
			// ��ȡ�ֻ��������ӹ������
			ConnectivityManager connectManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectManager != null) {
				// ��ȡ�������ӹ������
				NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();
				if (networkInfo != null & networkInfo.isConnected()) {
					// �жϵ�ǰ�����Ƿ��Ѿ�����
					if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return false;
	}

	// ��ȡ�������վ��������Χ
	public static int getSearchArea(SharedPreferences preferences) {
		// Ĭ��Ϊ5000��
		return preferences.getInt("searchArea", 5000);
	}

	// ��ȡ������ʾ��Χ
	public static int getBusArea(SharedPreferences preferences) {
		// Ĭ��Ϊ1500��
		return preferences.getInt("busArea", 1500);
	}

	// ��ȡ�Ƿ񱣳���Ļ
	public static boolean getKeepScreen(SharedPreferences preferences) {
		// Ĭ�Ϸ�
		return preferences.getBoolean("keepScreen", false);
	}

	// ��ȡ�Ƿ���Ҫ����GPS
	public static boolean getPromptGps(SharedPreferences preferences) {
		// Ĭ����
		return preferences.getBoolean("promptGps", true);
	}
	
	public static void setPromptGps(SharedPreferences preferences,boolean prompt){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("promptGps", prompt);
		editor.commit();
	}

}
