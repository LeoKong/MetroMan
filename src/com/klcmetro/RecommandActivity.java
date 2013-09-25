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
import com.baidu.mapapi.MKMapViewListener;
import com.baidu.mapapi.MKPlanNode;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKRoute;
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
import com.baidu.mapapi.RouteOverlay;
import com.baidu.mapapi.TransitOverlay;
import com.klcmetro.R;
import com.klcutil.Utils;

import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecommandActivity extends MapActivity implements LocationListener,
		MKMapViewListener {
	private static final int CHANGE = 1;
	private static final int CHANGINGBUS = 2;
	private static final int SEARCHING = 3;
	private BMapManager bMapManager = null;// 地图引擎
	private MapView mapView = null;
	private MKSearch mkSearch = null;// 定义地图查找类
	private MyLocationOverlay myLocationOverlay = null;// 系统默认我的位置标志
	private String key = "7AC130722DF5C9838FA595A1152C3951DBD1EB48";
	private ImageButton backCenterBtn;
	GeoPoint myGeoPoint;
	private MKPlanNode startNode, endNode;// 定义起始点
	private double startLat = 0;
	private double startLon = 0;
	private double endLat = 0;
	private double endLon = 0;

	private boolean navigation = true;// 是否导航
	private int flag = 0;// 标志位，防止描绘多条路线
	private String model = "walk";// 标志用户当前导航的模式
	private String cityName = "";// 公车查询需要的城市名参数

	private TextView nearView, distanceView;
	private View popView = null;// 弹出的气泡
	private Button popBtn;

	SharedPreferences preferences;
	private int searchArea = 0;
	private int busArea = 0;

	ProgressDialog busSearchingDialog;
	ProgressDialog searchingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recommand);

		// 读取用户设置
		preferences = this.getSharedPreferences("setting", MODE_PRIVATE);
		searchArea = Utils.getSearchArea(preferences);
		busArea = Utils.getBusArea(preferences);

		backCenterBtn = (ImageButton) findViewById(R.id.centerbtn);
		nearView = (TextView) findViewById(R.id.neartv);
		distanceView = (TextView) findViewById(R.id.distancetv);
		backCenterBtn.setOnClickListener(listener);

		// 初始化地图
		initMap();

		// 实例化查询类
		mkSearch = new MKSearch();
		mkSearch.init(bMapManager, new MySearchListener());

		if (Utils.getPromptGps(preferences)) {
			if (!Utils.checkGPS(RecommandActivity.this)) {
				showDialog(Utils.OPENGPS);
			}
		}

		// 显示"正在查找最近地铁站"的提示框
		showDialog(SEARCHING);

	}

	public OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (startLat != 0) {
				mapView.getController().animateTo(myGeoPoint);
			}
			navigation = true;// 回到导航状态
		}
	};

	// 重写查找监听
	public class MySearchListener implements MKSearchListener {

		public void onGetAddrResult(MKAddrInfo result, int error) {
			// TODO Auto-generated method stub
			if (error != 0) {
				return;
			}
			String[] citys = new String[] { "北京", "上海", "广州", "深圳", "香港", "天津",
					"南京", "沈阳", "成都", "佛山", "重庆", "西安", "苏州", "杭州", "昆明", "武汉",
					"大连", "长春" };
			MKGeocoderAddressComponent address = result.addressComponents;
			boolean have = false;// 记录是否开通地铁

			// 如果只是为了公车查询而查找城市名，则查询到城市名后直接返回
			if (model.equals("bus")) {
				cityName = address.city;
				return;
			}

			for (int i = 0; i < citys.length; i++) {
				if (address.city.contains(citys[i])) {
					have = true;
					break;
				}
			}

			if (have) {
				nearView.setText("对不起，" + searchArea + "米范围内无地铁站!");
			} else {
				nearView.setText("你所在的城市为:" + address.city + ",暂无地铁服务");
			}

			// 让提示框消失
			dismissDialog(SEARCHING);

			System.out.println("" + have);

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

			switch (error) {
			case MKEvent.ERROR_NETWORK_CONNECT:
				nearView.setText("网络连接失败，请检查网络!");
				// 让提示框消失
				dismissDialog(SEARCHING);
				break;
			case MKEvent.ERROR_NETWORK_DATA:
				nearView.setText("网络数据错误!");
				// 让提示框消失
				dismissDialog(SEARCHING);
				break;
			}

			// 找不到直接返回
			if (result == null) {
				System.out.println("nofound");
				// 查找用户当前所在的城市，检查是否有地铁服务
				mkSearch.reverseGeocode(new GeoPoint((int) (startLat * 1e6),
						(int) (startLon * 1e6)));
				return;
			}

			ArrayList<MKPoiInfo> mkPoiInfos = result.getAllPoi();
			// mkPoiInfo = result.getPoi(9);
			// endLat = mkPoiInfo.pt.getLatitudeE6() / 1e6;
			// endLon = mkPoiInfo.pt.getLongitudeE6() / 1e6;
			// flag=1;
			System.out.println("最近的地铁站数" + mkPoiInfos.size());

			// 记录不是地铁站的索引号
			// 排除对象包括搜索出地铁站附近的酒店或便利店
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < mkPoiInfos.size(); i++) {
				String nameString = mkPoiInfos.get(i).name;
				if (!isStation(nameString)) {
					list.add(i);
				}
			}

			// 目标地铁站索引号
			int target;

			if (mkPoiInfos.size() == 1) {// 只找到一个时

				if (!isStation(mkPoiInfos.get(0).name)) {
					System.out.println("只找个一个却不是地铁站");
					// 查找用户当前所在的城市，检查是否有地铁服务
					mkSearch.reverseGeocode(new GeoPoint(
							(int) (startLat * 1e6), (int) (startLon * 1e6)));
					return;
				}

				target = 0;
			} else {// 找到多个则进行比较，选出直线距离最近的
				double[] distance = new double[mkPoiInfos.size()];
				for (int i = 0; i < mkPoiInfos.size(); i++) {
					// 跳过不是地铁站
					if (list.contains(i)) {
						System.out.println("排除");
						// 因为不进行插入数据，所以默认为0，必须要插入一个很大的数
						distance[i] = 99999;
						continue;
					}
					double latitude = mkPoiInfos.get(i).pt.getLatitudeE6() / 1e6;
					double longitude = mkPoiInfos.get(i).pt.getLongitudeE6() / 1e6;
					distance[i] = getDistance(startLat, startLon, latitude,
							longitude);
					System.out.println(mkPoiInfos.get(i).name);
				}

				// 获得最小距离的索引号
				target = minDistance(distance);
			}
			// 将其设成目标点进行导航
			endLat = mkPoiInfos.get(target).pt.getLatitudeE6() / 1e6;
			endLon = mkPoiInfos.get(target).pt.getLongitudeE6() / 1e6;
			System.out.println(mkPoiInfos.get(target).name);

			// 标题栏显示地铁站的名称
			String targetName = mkPoiInfos.get(target).name;
			if (targetName.contains("站")) {
				nearView.setText(mkPoiInfos.get(target).name);
			} else {
				nearView.setText(mkPoiInfos.get(target).name + "站");
			}

			// 提示用户正在计算距离和描绘路线
			distanceView.setText("正在计算距离...");

			// 让提示框消失
			dismissDialog(SEARCHING);

			// 马上画出路线，仅仅执行一次
			startNode = new MKPlanNode();
			startNode.pt = new GeoPoint((int) (startLat * 1e6),
					(int) (startLon * 1e6));

			endNode = new MKPlanNode();
			endNode.pt = new GeoPoint((int) (endLat * 1e6),
					(int) (endLon * 1e6));
			mkSearch.walkingSearch(null, startNode, null, endNode);

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
				dismissDialog(CHANGE);
				Toast.makeText(RecommandActivity.this, "对不起，无公交线路！",
						Toast.LENGTH_LONG).show();
				return;
			}
			clearTopOverLay();// 清除原有路线
			TransitOverlay transitOverlay = new TransitOverlay(
					RecommandActivity.this, mapView);
			transitOverlay.setData(arg0.getPlan(0));
			mapView.getOverlays().add(transitOverlay);
			mapView.invalidate();

			MKTransitRoutePlan mkTransitRoutePlan = arg0.getPlan(0);
			System.out.println("一共" + mkTransitRoutePlan.getNumLines());

			int busLines = mkTransitRoutePlan.getNumLines();// 获取一共要乘坐多少次公车线路
			MKLine mkLine = null;
			String popString;// 需要在popview上显示的内容
			int slice;// 分割点

			if (busLines == 1) {// 若不需要换乘
				mkLine = mkTransitRoutePlan.getLine(0);
				slice = mkLine.getTitle().indexOf("(");
				// 显示起点
				popString = "往'" + mkLine.getGetOnStop().name + "'站台乘坐'"
						+ mkLine.getTitle().substring(0, slice) + "'";
				MyPopView(mkLine.getGetOnStop().pt, popString);
				// 显示终点
				popString = "到'" + mkLine.getGetOffStop().name + "'站台下车";
				MyPopView(mkLine.getGetOffStop().pt, popString);
			} else {// 若需要换乘

				for (int i = 0; i < mkTransitRoutePlan.getNumLines(); i++) {
					mkLine = mkTransitRoutePlan.getLine(i);
					slice = mkLine.getTitle().indexOf("(");

					if (i == 0) {// 当为第一条线时
						popString = "往'" + mkLine.getGetOnStop().name
								+ "'站台乘坐'"
								+ mkLine.getTitle().substring(0, slice) + "'";
					} else {// 中间的所有的换乘点
						popString = "再到'" + mkLine.getGetOnStop().name
								+ "'站台下车换乘'"
								+ mkLine.getTitle().substring(0, slice) + "'";
					}
					MyPopView(mkLine.getGetOnStop().pt, popString);
				}
				// 最后显示终点
				popString = "到'" + mkLine.getGetOffStop().name + "'站台下车";
				MyPopView(mkLine.getGetOffStop().pt, popString);
			}
			dismissDialog(CHANGINGBUS);
		}

		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			if (arg0 == null) {
				return;
			}
			// 计算实际距离
			MKRoute mkRoute = arg0.getPlan(0).getRoute(0);
			distanceView.setText("距离：" + mkRoute.getDistance() + "米");

			// 让路线只描绘两次，第2次是为了提高地位的准确度
			if (flag < 2) {
				if (flag == 1) {
					// 第二次描绘路线时因把原有的路线清除
					clearTopOverLay();
				}

				System.out.println("画");
				// 显示行走路线
				RouteOverlay routeOverlay = new RouteOverlay(
						RecommandActivity.this, mapView);
				routeOverlay.setData(arg0.getPlan(0).getRoute(0));
				mapView.getOverlays().add(routeOverlay);
				mapView.invalidate();// 刷新地图

				flag = flag + 1;

				// 如果行走距离超过指定范围则提示用户是否要切换到公车线路
				if (mkRoute.getDistance() >= busArea && flag == 1
						&& busArea != 0) {
					model = "bus";// 进入公交模式
					// 马上查询用户所在城市，方便下一步进入公车模式时传入城市名
					mkSearch.reverseGeocode(new GeoPoint(
							(int) (startLat * 1e6), (int) (startLon * 1e6)));
					showDialog(CHANGE);

				}
			}

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
	}

	// 判断返回结果的名字是否为一个地铁站
	public boolean isStation(String name) {
		// 判断对象包括搜索出地铁站附近的酒店或便利店
		if (name.contains("酒店") || name.contains("(") || name.contains("酒吧")) {
			return false;
		}
		return true;

	}

	// 找出距离最短的，返回索引号
	public int minDistance(double[] d) {
		double min = d[0];
		int j = 0;
		for (int i = 1; i < d.length; i++) {
			if (min > d[i]) {
				min = d[i];
				j = i;
			}
		}

		return j;// 返回最小的距离的索引号

	}

	// 计算距离
	public double getDistance(double lat1, double lng1, double lat2, double lng2) {
		double pk = (double) (180 / Math.PI);

		double a1 = lat1 / pk;
		double a2 = lng1 / pk;
		double b1 = lat2 / pk;
		double b2 = lng2 / pk;

		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);

		return 6366000 * tt;
	}

	// 初始化地图
	public void initMap() {

		bMapManager = new BMapManager(this);
		bMapManager.init(key, null);
		initMapActivity(bMapManager);

		mapView = (MapView) findViewById(R.id.bmapView);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(18);

		// 在地图上显示我的位置
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);

	}

	// 地图和我的位置都不可以用
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (bMapManager != null) {
			// bMapManager.getLocationManager().removeUpdates(this);
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
			Utils.keepScreenOn(RecommandActivity.this, false);
		}
		super.onPause();
	}

	// 回复地图和我的位置的显示
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
			// 移动百度地图事件监听
			mapView.regMapViewListener(bMapManager, this);
		}

		// 读取用户是否需要保持屏幕常亮
		if (Utils.getKeepScreen(preferences)) {
			// 开启屏幕常亮
			Utils.keepScreenOn(RecommandActivity.this, true);
		}

		super.onResume();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(
				RecommandActivity.this);
		builder.setCancelable(false);
		switch (id) {
		case CHANGE:
			builder.setTitle("温馨提示");
			builder.setMessage("该地铁站离你较远，是否改乘公交？");
			builder.setPositiveButton("是",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

							startNode = new MKPlanNode();
							startNode.pt = new GeoPoint((int) (startLat * 1e6),
									(int) (startLon * 1e6));

							endNode = new MKPlanNode();
							endNode.pt = new GeoPoint((int) (endLat * 1e6),
									(int) (endLon * 1e6));
							showDialog(CHANGINGBUS);
							mkSearch.transitSearch(cityName, startNode, endNode);
						}
					});
			builder.setNegativeButton("不,坚持走路",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							model = "walk";// 回到步行模式
						}
					});

			return builder.create();

		case Utils.OPENGPS:
			builder.setTitle("提高“我的位置”的精确度");
			builder.setMessage("你没有打开GPS设置,打开GPS能提供更准确的定位！");
			final CheckBox checkBox=new CheckBox(this);
			builder.setView(checkBox);
			checkBox.setText("不再提示");
			builder.setPositiveButton("设置",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
							Utils.setPromptGps(preferences, !checkBox.isChecked());
						}
					});
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Utils.setPromptGps(preferences, !checkBox.isChecked());
				}
			});
			return builder.create();

		case CHANGINGBUS:
			busSearchingDialog = new ProgressDialog(RecommandActivity.this);
			busSearchingDialog.setMessage("正在查询公交路线...");
			busSearchingDialog.setCancelable(false);
			busSearchingDialog.setButton("取消",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							busSearchingDialog.dismiss();
						}
					});
			busSearchingDialog.show();
			return busSearchingDialog;

		case SEARCHING:
			searchingDialog = new ProgressDialog(RecommandActivity.this);
			searchingDialog.setMessage("正在搜索离您最近的地铁站...");
			searchingDialog.setCancelable(false);
			searchingDialog.show();
			return searchingDialog;

		}
		return super.onCreateDialog(id);
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
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (location != null) {
			// 获取用户当前位置
			// myGeoPoint = new GeoPoint((int) (location.getLatitude() * 1e6),
			// (int) (location.getLongitude() * 1e6));
			startLat = location.getLatitude();
			startLon = location.getLongitude();

			// startLat = 31.249162;
			// startLon = 121.487899;

			myGeoPoint = new GeoPoint((int) (startLat * 1e6),
					(int) (startLon * 1e6));

			if (navigation) {
				mapView.getController().animateTo(myGeoPoint);// 实施导航
			}

			// 程序第一次运行时进行查找最近的地铁站
			if (flag == 0) {
				mapView.getController().animateTo(myGeoPoint);// 设置中心点
				mkSearch.poiSearchNearBy("地铁%站", new GeoPoint(
						(int) (startLat * 1e6), (int) (startLon * 1e6)),
						searchArea);
			}

			// 行走导航时，需要不断刷新距离
			if (flag != 0 && model.equals("walk")) {
				startNode = new MKPlanNode();
				// startNode.pt = new GeoPoint((int) (location.getLatitude() *
				// 1e6),
				// (int) (location.getLongitude() * 1e6));
				startNode.pt = new GeoPoint((int) (startLat * 1e6),
						(int) (startLon * 1e6));

				endNode = new MKPlanNode();
				endNode.pt = new GeoPoint((int) (endLat * 1e6),
						(int) (endLon * 1e6));
				mkSearch.walkingSearch(null, startNode, null, endNode);
			}
		}
	}

	// 清除顶级图层，重新绘制地图
	public void clearTopOverLay() {

		List<Overlay> overlays = mapView.getOverlays();
		if (overlays.size() != 0) {
			overlays.remove(overlays.size() - 1);
		}
	}

	public void onMapMoveFinish() {
		// TODO Auto-generated method stub
		navigation = false;// 用户移动地图后，不进入导航状态

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			setResult(Activity.RESULT_OK);
			RecommandActivity.this.finish();

		}
		return super.onKeyDown(keyCode, event);
	}

}
