package com.klcmetro;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.GeoPoint;
import com.baidu.mapapi.LocationListener;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeocoderAddressComponent;
import com.baidu.mapapi.MKLine;
import com.baidu.mapapi.MKLocationManager;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRoutePlan;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.baidu.mapapi.MapActivity;
import com.baidu.mapapi.MapView;
import com.baidu.mapapi.MyLocationOverlay;
import com.baidu.mapapi.Overlay;
import com.baidu.mapapi.TransitOverlay;
import com.klcmetro.RecommandActivity.MySearchListener;
import com.klcutil.Utils;

import android.R.integer;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SearchInMapActivity extends MapActivity implements
		LocationListener, OnClickListener {
	private static final int SEARCHING = 1;
	private static final int LOCATING = 2;

	private BMapManager bMapManager = null;// 地图引擎
	private MapView mapView = null;
	private MKSearch mkSearch = null;// 定义地图查找类
	private MyLocationOverlay myLocationOverlay = null;
	private String key = "7AC130722DF5C9838FA595A1152C3951DBD1EB48";
	GeoPoint myGeoPoint;
	private double startLat = 0;
	private double startLon = 0;
	private double endLat = 0;
	private double endLon = 0;
	private MKPlanNode startNode, endNode;// 定义起始点
	private String myCity = "";

	private ImageButton searchButton1, searchButton2;
	private RelativeLayout layout1, layout2;
	private EditText searchEditText1, searchEditText2;
	private View popView = null;// 弹出的气泡
	private List<View> popList = new ArrayList<View>();// 记录下所有的popview方便清除
	private Button popBtn;
	private TextView searchingTextView;

	SharedPreferences preferences;
	ProgressDialog searchingDialog;
	ProgressDialog locatingDialog;

	private int flag = 0;// 标志onlocationchange是不是第一次运行
	private int whichEdit = 1;// 标志哪一个edittext传过来的

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_station);

		// 读取用户设置
		preferences = this.getSharedPreferences("setting", MODE_PRIVATE);

		searchingTextView = (TextView) findViewById(R.id.show_searching);
		searchButton1 = (ImageButton) findViewById(R.id.search_btn1);
		searchButton2 = (ImageButton) findViewById(R.id.search_btn2);
		searchEditText1 = (EditText) findViewById(R.id.editsearch1);
		searchEditText2 = (EditText) findViewById(R.id.editsearch2);

		layout1 = (RelativeLayout) findViewById(R.id.first_relativelayout);// 先显示布局1，提示用户正在获取位置信息
		layout2 = (RelativeLayout) findViewById(R.id.second_relativelayout);

		searchButton1.setOnClickListener(this);
		searchButton2.setOnClickListener(this);

		// 初始化地图
		initMap();
		// 实例化查询类
		mkSearch = new MKSearch();
		mkSearch.init(bMapManager, new MySearchListener());

		// 显示"正在获取位置信息"的提示框
		showDialog(LOCATING);

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.search_btn1:
			// 让软键盘消失
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(SearchInMapActivity.this
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

			String startStation = searchEditText1.getText().toString();
			if (startStation.equals("")) {
				searchEditText1.setError("请输入起点站");
				searchEditText1.requestFocus();
				return;
			}
			if (!startStation.contains("地铁站")) {
				startStation = startStation + "地铁站";
			}

			// 显示提示框
			showDialog(SEARCHING);

			// 查找出起点地铁站的经纬度
			mkSearch.poiSearchInCity(myCity, startStation);
			whichEdit = 1;

			break;

		case R.id.search_btn2:
			layout2.setVisibility(View.VISIBLE);// 重新搜索
			break;
		}

	}

	// 重写查找监听
	public class MySearchListener implements MKSearchListener {

		public void onGetAddrResult(MKAddrInfo result, int error) {
			// TODO Auto-generated method stub
			// 让提示框消失
			dismissDialog(LOCATING);
			if (error != 0) {
				if (error == MKEvent.ERROR_NETWORK_CONNECT) {
					searchingTextView.setText("网络连接失败，请检查网络!");
				}
				if (error == MKEvent.ERROR_NETWORK_DATA) {
					searchingTextView.setText("网络数据错误!");
				}
				return;
			}
			String[] citys = new String[] { "北京", "上海", "广州", "深圳", "香港", "天津",
					"南京", "沈阳", "成都", "佛山", "重庆", "西安", "苏州", "杭州", "昆明", "武汉",
					"大连", "长春" };
			MKGeocoderAddressComponent address = result.addressComponents;
			boolean have = false;// 记录是否开通地铁

			for (int i = 0; i < citys.length; i++) {
				if (address.city.contains(citys[i])) {
					have = true;
					break;
				}
			}

			if (have) {
				myCity = address.city;
				// 获取用户所在城市之后，显示搜索
				layout2.setVisibility(View.VISIBLE);
			} else {
				searchingTextView.setText("您所在的城市为:" + address.city
						+ ",暂未开通地铁服务");
			}

		}

		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		public void onGetPoiDetailSearchResult(int arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		public void onGetPoiResult(MKPoiResult result, int type, int error) {
			// TODO Auto-generated method stub
			// 找不到直接返回
			if (result == null || error != 0) {
				// 让提示框消失
				dismissDialog(SEARCHING);
				// 判断网络连接是否正常
				if (error == MKEvent.ERROR_NETWORK_CONNECT) {
					Toast.makeText(SearchInMapActivity.this, "网络连接失败!",
							Toast.LENGTH_LONG).show();
					return;
				} else if (error == MKEvent.ERROR_NETWORK_DATA) {
					Toast.makeText(SearchInMapActivity.this, "网络数据错误!",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (whichEdit == 1) {
					searchEditText1.setError("找不到该地铁站");
				} else {
					searchEditText2.setError("找不到该地铁站");
				}
				System.out.println("nofound" + ";error=" + error);
				return;
			}

			// 找到但是不是地铁站也返回
			if (result.getPoi(0) == null) {
				// 让提示框消失
				dismissDialog(SEARCHING);
				if (whichEdit == 1) {
					searchEditText1.setError("找不到该地铁站");
				} else {
					searchEditText2.setError("找不到该地铁站");
				}
				System.out.println("nofound2");
				return;
			}

			int bestStation = 0;// 记录查找结果最准确的
			MKPoiInfo mkPoiInfo = null;
			System.out.println("城市名：" + myCity);
			for (int i = 0; i < result.getAllPoi().size(); i++) {
				System.out.println("返回的点:" + result.getPoi(i).name);
			}
			switch (whichEdit) {
			case 1:
				// 提高查找的精确度
				for (int i = 0; i < result.getAllPoi().size(); i++) {
					if (result.getPoi(i).name.contains(searchEditText1
							.getText().toString())) {
						bestStation = i;
						break;
					}
					System.out.println("返回的点:" + result.getPoi(i).name);
				}
				mkPoiInfo = result.getPoi(bestStation);
				mapView.getController().animateTo(mkPoiInfo.pt);// 设起点为中心点
				// 设置起点
				startNode = new MKPlanNode();
				startNode.pt = mkPoiInfo.pt;
				System.out.println("起点：" + mkPoiInfo.name);
				whichEdit = 2;

				// 查询终点站的经纬度
				String endStation = searchEditText2.getText().toString();
				if (endStation.equals("")) {
					// 让提示框消失
					dismissDialog(SEARCHING);
					searchEditText2.setError("请输入终点站");
					searchEditText2.requestFocus();
					return;
				}
				if (!endStation.contains("地铁站")) {
					endStation = endStation + "地铁站";
				}
				// 再调用一次这个函数
				mkSearch.poiSearchInCity(myCity, endStation);

				break;

			case 2:
				// 提高查找的精确度
				for (int i = 0; i < result.getAllPoi().size(); i++) {
					if (result.getPoi(i).name.contains(searchEditText2
							.getText().toString())) {
						bestStation = i;
						break;
					}
					System.out.println("返回的点:" + result.getPoi(i).name);
				}
				mkPoiInfo = result.getPoi(bestStation);
				// 设置终点
				endNode = new MKPlanNode();
				endNode.pt = mkPoiInfo.pt;
				System.out.println("终点：" + mkPoiInfo.name);
				// 得到终点地铁站之后，进入路线查找
				mkSearch.setTransitPolicy(MKSearch.EBUS_TIME_FIRST);
				mkSearch.transitSearch(myCity, startNode, endNode);
				break;
			}

			// 把知道了地铁站之后，进入公车查找

			// startNode = new MKPlanNode();
			// startNode.pt = new GeoPoint((int) (startLat * 1e6),
			// (int) (startLon * 1e6));
			// endNode = new MKPlanNode();
			// endNode.pt = mkPoiInfo.pt;
			// mkSearch.setTransitPolicy(MKSearch.EBUS_TIME_FIRST);
			// mkSearch.transitSearch(myCity, startNode, endNode);

		}

		public void onGetRGCShareUrlResult(String arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			if (arg0 == null) {
				dismissDialog(SEARCHING);
				Toast.makeText(SearchInMapActivity.this, "对不起，无地铁路线",
						Toast.LENGTH_LONG).show();
				return;
			}

			System.out.println("方案：" + arg0.getNumPlan());

			int best = 0;
			int mlines = 0;// 记录i方案有多少地铁线路
			int blines = 0;// 目前最多的地铁线路

			for (int i = 0; i < arg0.getNumPlan(); i++) {
				MKTransitRoutePlan mkTransitRoutePlan = arg0.getPlan(i);
				int buslines = mkTransitRoutePlan.getNumLines();
				for (int j = 0; j < buslines; j++) {
					mlines = 0;
					MKLine mkLine = mkTransitRoutePlan.getLine(j);
					if (mkLine.getType() == 1) {
						mlines++;
					}
				}
				if (mlines > blines) {
					blines = mlines;
					best = i;
				}

				System.out.println("第" + (i + 1) + "条的公车路线包含了" + mlines);
			}

			System.out.println("best:" + (best + 1));

			// 在标题栏显示站点数和大约需要的时间
			layout2.setVisibility(View.GONE);
			searchButton2.setVisibility(View.VISIBLE);

			clearTopOverLay();// 清除原有路线
			System.out.println("进入画线");
			TransitOverlay transitOverlay = new TransitOverlay(
					SearchInMapActivity.this, mapView);
			transitOverlay.setData(arg0.getPlan(best));
			mapView.getOverlays().add(transitOverlay);
			mapView.invalidate();

			MKTransitRoutePlan mkTransitRoutePlan = arg0.getPlan(best);
			// System.out.println("一共" + mkTransitRoutePlan.getNumLines());

			int busLines = mkTransitRoutePlan.getNumLines();// 获取一共要乘坐多少次地铁线路
			int busStations = 0;// 一共有多少个站
			MKLine mkLine = null;
			String popString;// 需要在popview上显示的内容
			int slice;// 分割点

			if (busLines == 1) {// 若不需要换乘
				mkLine = mkTransitRoutePlan.getLine(0);
				slice = mkLine.getTitle().indexOf("(");
				// 显示起点
				popString = "从'" + mkLine.getGetOnStop().name + "'站乘坐'"
						+ mkLine.getTitle().substring(0, slice) + "'";
				MyPopView(mkLine.getGetOnStop().pt, popString);
				// 显示终点
				popString = "到'" + mkLine.getGetOffStop().name + "'站下车";
				MyPopView(mkLine.getGetOffStop().pt, popString);

				// 计算一共有多少个站
				busStations = mkLine.getNumViaStops();
			} else {// 若需要换乘

				for (int i = 0; i < mkTransitRoutePlan.getNumLines(); i++) {
					mkLine = mkTransitRoutePlan.getLine(i);
					slice = mkLine.getTitle().indexOf("(");

					if (i == 0) {// 当为第一条线时
						popString = "从'" + mkLine.getGetOnStop().name + "'站乘坐'"
								+ mkLine.getTitle().substring(0, slice) + "'";
					} else {// 中间的所有的换乘点
						popString = "再到'" + mkLine.getGetOnStop().name
								+ "'站下车换乘'"
								+ mkLine.getTitle().substring(0, slice) + "'";
					}
					MyPopView(mkLine.getGetOnStop().pt, popString);

					// 计算一共有多少个站
					busStations = busStations + mkLine.getNumViaStops();
				}
				// 最后显示终点
				popString = "到'" + mkLine.getGetOffStop().name + "'站下车";
				MyPopView(mkLine.getGetOffStop().pt, popString);
			}

			searchingTextView.setText("一共" + busStations + "站，预计需要"
					+ busStations * 3 + "分钟");
			// 最后让提示框消失
			dismissDialog(SEARCHING);

		}

		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

	}

	// 通过气泡显示详细信息
	public void MyPopView(GeoPoint geoPoint, String detail) {
		// 初始化气泡
		popView = super.getLayoutInflater().inflate(R.layout.popview, null);
		popBtn = (Button) popView.findViewById(R.id.pop);
		// popBtn.setOnClickListener(listener);
		// 放在百度地图上
		mapView.addView(popView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
				MapView.LayoutParams.TOP_LEFT));

		// 获取坐标给气泡定义显示的位置
		mapView.updateViewLayout(popView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, geoPoint,
				MapView.LayoutParams.BOTTOM_CENTER));
		// popView.setVisibility(View.VISIBLE);
		popBtn.setText(detail);
		popList.add(popView);
	}

	// 初始化地图
	public void initMap() {

		bMapManager = new BMapManager(this);
		bMapManager.init(key, null);
		initMapActivity(bMapManager);

		mapView = (MapView) findViewById(R.id.bmapView);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(15);

		myLocationOverlay = new MyLocationOverlay(this, mapView);
		// 在地图上显示我的位置
		mapView.getOverlays().add(myLocationOverlay);

	}

	// 地图和我的位置都不可以用
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (bMapManager != null) {
			bMapManager.getLocationManager().removeUpdates(this);
			// 关闭GPS定位
			bMapManager.getLocationManager().enableProvider(
					(int) MKLocationManager.MK_GPS_PROVIDER);
			myLocationOverlay.disableMyLocation();// 停止位置更新
			myLocationOverlay.disableCompass();// 关闭指南针更新
			bMapManager.stop();
		}
		// 读取用户是否需要保持屏幕常亮
		if (Utils.getKeepScreen(preferences)) {
			// 释放屏幕常亮
			Utils.keepScreenOn(SearchInMapActivity.this, false);
		}
		super.onPause();
	}

	// 恢复地图和我的位置的显示
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (bMapManager != null) {
			bMapManager.getLocationManager().requestLocationUpdates(this);
			// 使用GPS定位
			bMapManager.getLocationManager().enableProvider(
					(int) MKLocationManager.MK_GPS_PROVIDER);

			myLocationOverlay.enableMyLocation();
			myLocationOverlay.enableCompass();
			bMapManager.start();
			System.out.println("resume");
		}

		// 读取用户是否需要保持屏幕常亮
		if (Utils.getKeepScreen(preferences)) {
			// 开启屏幕常亮
			Utils.keepScreenOn(SearchInMapActivity.this, true);
		}
		super.onResume();
	}

	// 根据MyLocationOverlay配置的属性确定是否在地图上显示当前位置
	@Override
	protected boolean isLocationDisplayed() {
		// TODO Auto-generated method stub
		return myLocationOverlay.isMyLocationEnabled();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (bMapManager != null) {
			bMapManager.destroy();
			bMapManager = null;
		}
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case SEARCHING:
			searchingDialog = new ProgressDialog(SearchInMapActivity.this);
			searchingDialog.setMessage("正在查询...");
			searchingDialog.setCancelable(false);
			searchingDialog.setButton("取消",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							searchingDialog.dismiss();
						}
					});
			searchingDialog.show();
			return searchingDialog;

		case LOCATING:
			locatingDialog = new ProgressDialog(SearchInMapActivity.this);
			locatingDialog.setMessage("正在获取您的位置信息...");
			locatingDialog.setCancelable(false);
			locatingDialog.show();
			return locatingDialog;
		}
		return super.onCreateDialog(id);
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (location != null) {
			// 获取用户当前位置
			startLat = location.getLatitude();
			startLon = location.getLongitude();

//			startLat = 22.703945;
//			 startLon = 114.056696;

			myGeoPoint = new GeoPoint((int) (startLat * 1e6),
					(int) (startLon * 1e6));

			if (flag == 0) {
				// 查找用户当前所在的城市，检查是否有地铁服务，仅执行一次
				mkSearch.reverseGeocode(new GeoPoint((int) (startLat * 1e6),
						(int) (startLon * 1e6)));
				mapView.getController().animateTo(myGeoPoint);// 设置中心点
			}
			flag = 1;

		}

	}

	// 清除顶级图层，重新绘制地图
	public void clearTopOverLay() {

		List<Overlay> overlays = mapView.getOverlays();

		if (overlays.size() != 0) {
			overlays.remove(overlays.size() - 1);
		}
		if (popView != null) {
			// 清除所有popview
			for (int i = 0; i < popList.size(); i++) {
				popList.get(i).setVisibility(View.GONE);
			}
			popList.clear();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			setResult(Activity.RESULT_OK);
			SearchInMapActivity.this.finish();

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

}
