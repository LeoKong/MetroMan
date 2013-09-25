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
	 * 保持屏幕唤醒状态（即背景灯不熄灭）
	 * 
	 * @param context
	 * @param on
	 *            是否保持常亮
	 */
	public static void keepScreenOn(Context context, boolean on) {
		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "MyLock");
		wl.setReferenceCounted(false);// 这句不能少
		if (on) {
			wl.acquire();
		} else {
			wl.release();
			wl = null;
		}
	}

	/**
	 * 检查是否开启的GPS
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
	 * 检查是否连接网络
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNet(Context context) {
		try {
			// 获取手机所有连接管理对象
			ConnectivityManager connectManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectManager != null) {
				// 获取网络连接管理对象
				NetworkInfo networkInfo = connectManager.getActiveNetworkInfo();
				if (networkInfo != null & networkInfo.isConnected()) {
					// 判断当前网络是否已经连接
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

	// 获取最近地铁站的搜索范围
	public static int getSearchArea(SharedPreferences preferences) {
		// 默认为5000米
		return preferences.getInt("searchArea", 5000);
	}

	// 获取公车提示范围
	public static int getBusArea(SharedPreferences preferences) {
		// 默认为1500米
		return preferences.getInt("busArea", 1500);
	}

	// 获取是否保持屏幕
	public static boolean getKeepScreen(SharedPreferences preferences) {
		// 默认否
		return preferences.getBoolean("keepScreen", false);
	}

	// 获取是否需要提醒GPS
	public static boolean getPromptGps(SharedPreferences preferences) {
		// 默认是
		return preferences.getBoolean("promptGps", true);
	}
	
	public static void setPromptGps(SharedPreferences preferences,boolean prompt){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("promptGps", prompt);
		editor.commit();
	}

}
