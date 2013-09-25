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
	private BMapManager bMapManager = null;// ��ͼ����
	private MapView mapView = null;
	private MKSearch mkSearch = null;// �����ͼ������
	private MyLocationOverlay myLocationOverlay = null;// ϵͳĬ���ҵ�λ�ñ�־
	private String key = "7AC130722DF5C9838FA595A1152C3951DBD1EB48";
	private ImageButton backCenterBtn;
	GeoPoint myGeoPoint;
	private MKPlanNode startNode, endNode;// ������ʼ��
	private double startLat = 0;
	private double startLon = 0;
	private double endLat = 0;
	private double endLon = 0;

	private boolean navigation = true;// �Ƿ񵼺�
	private int flag = 0;// ��־λ����ֹ������·��
	private String model = "walk";// ��־�û���ǰ������ģʽ
	private String cityName = "";// ������ѯ��Ҫ�ĳ���������

	private TextView nearView, distanceView;
	private View popView = null;// ����������
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

		// ��ȡ�û�����
		preferences = this.getSharedPreferences("setting", MODE_PRIVATE);
		searchArea = Utils.getSearchArea(preferences);
		busArea = Utils.getBusArea(preferences);

		backCenterBtn = (ImageButton) findViewById(R.id.centerbtn);
		nearView = (TextView) findViewById(R.id.neartv);
		distanceView = (TextView) findViewById(R.id.distancetv);
		backCenterBtn.setOnClickListener(listener);

		// ��ʼ����ͼ
		initMap();

		// ʵ������ѯ��
		mkSearch = new MKSearch();
		mkSearch.init(bMapManager, new MySearchListener());

		if (Utils.getPromptGps(preferences)) {
			if (!Utils.checkGPS(RecommandActivity.this)) {
				showDialog(Utils.OPENGPS);
			}
		}

		// ��ʾ"���ڲ����������վ"����ʾ��
		showDialog(SEARCHING);

	}

	public OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (startLat != 0) {
				mapView.getController().animateTo(myGeoPoint);
			}
			navigation = true;// �ص�����״̬
		}
	};

	// ��д���Ҽ���
	public class MySearchListener implements MKSearchListener {

		public void onGetAddrResult(MKAddrInfo result, int error) {
			// TODO Auto-generated method stub
			if (error != 0) {
				return;
			}
			String[] citys = new String[] { "����", "�Ϻ�", "����", "����", "���", "���",
					"�Ͼ�", "����", "�ɶ�", "��ɽ", "����", "����", "����", "����", "����", "�人",
					"����", "����" };
			MKGeocoderAddressComponent address = result.addressComponents;
			boolean have = false;// ��¼�Ƿ�ͨ����

			// ���ֻ��Ϊ�˹�����ѯ�����ҳ����������ѯ����������ֱ�ӷ���
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
				nearView.setText("�Բ���" + searchArea + "�׷�Χ���޵���վ!");
			} else {
				nearView.setText("�����ڵĳ���Ϊ:" + address.city + ",���޵�������");
			}

			// ����ʾ����ʧ
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
				nearView.setText("��������ʧ�ܣ���������!");
				// ����ʾ����ʧ
				dismissDialog(SEARCHING);
				break;
			case MKEvent.ERROR_NETWORK_DATA:
				nearView.setText("�������ݴ���!");
				// ����ʾ����ʧ
				dismissDialog(SEARCHING);
				break;
			}

			// �Ҳ���ֱ�ӷ���
			if (result == null) {
				System.out.println("nofound");
				// �����û���ǰ���ڵĳ��У�����Ƿ��е�������
				mkSearch.reverseGeocode(new GeoPoint((int) (startLat * 1e6),
						(int) (startLon * 1e6)));
				return;
			}

			ArrayList<MKPoiInfo> mkPoiInfos = result.getAllPoi();
			// mkPoiInfo = result.getPoi(9);
			// endLat = mkPoiInfo.pt.getLatitudeE6() / 1e6;
			// endLon = mkPoiInfo.pt.getLongitudeE6() / 1e6;
			// flag=1;
			System.out.println("����ĵ���վ��" + mkPoiInfos.size());

			// ��¼���ǵ���վ��������
			// �ų������������������վ�����ľƵ�������
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < mkPoiInfos.size(); i++) {
				String nameString = mkPoiInfos.get(i).name;
				if (!isStation(nameString)) {
					list.add(i);
				}
			}

			// Ŀ�����վ������
			int target;

			if (mkPoiInfos.size() == 1) {// ֻ�ҵ�һ��ʱ

				if (!isStation(mkPoiInfos.get(0).name)) {
					System.out.println("ֻ�Ҹ�һ��ȴ���ǵ���վ");
					// �����û���ǰ���ڵĳ��У�����Ƿ��е�������
					mkSearch.reverseGeocode(new GeoPoint(
							(int) (startLat * 1e6), (int) (startLon * 1e6)));
					return;
				}

				target = 0;
			} else {// �ҵ��������бȽϣ�ѡ��ֱ�߾��������
				double[] distance = new double[mkPoiInfos.size()];
				for (int i = 0; i < mkPoiInfos.size(); i++) {
					// �������ǵ���վ
					if (list.contains(i)) {
						System.out.println("�ų�");
						// ��Ϊ�����в������ݣ�����Ĭ��Ϊ0������Ҫ����һ���ܴ����
						distance[i] = 99999;
						continue;
					}
					double latitude = mkPoiInfos.get(i).pt.getLatitudeE6() / 1e6;
					double longitude = mkPoiInfos.get(i).pt.getLongitudeE6() / 1e6;
					distance[i] = getDistance(startLat, startLon, latitude,
							longitude);
					System.out.println(mkPoiInfos.get(i).name);
				}

				// �����С�����������
				target = minDistance(distance);
			}
			// �������Ŀ�����е���
			endLat = mkPoiInfos.get(target).pt.getLatitudeE6() / 1e6;
			endLon = mkPoiInfos.get(target).pt.getLongitudeE6() / 1e6;
			System.out.println(mkPoiInfos.get(target).name);

			// ��������ʾ����վ������
			String targetName = mkPoiInfos.get(target).name;
			if (targetName.contains("վ")) {
				nearView.setText(mkPoiInfos.get(target).name);
			} else {
				nearView.setText(mkPoiInfos.get(target).name + "վ");
			}

			// ��ʾ�û����ڼ����������·��
			distanceView.setText("���ڼ������...");

			// ����ʾ����ʧ
			dismissDialog(SEARCHING);

			// ���ϻ���·�ߣ�����ִ��һ��
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
				Toast.makeText(RecommandActivity.this, "�Բ����޹�����·��",
						Toast.LENGTH_LONG).show();
				return;
			}
			clearTopOverLay();// ���ԭ��·��
			TransitOverlay transitOverlay = new TransitOverlay(
					RecommandActivity.this, mapView);
			transitOverlay.setData(arg0.getPlan(0));
			mapView.getOverlays().add(transitOverlay);
			mapView.invalidate();

			MKTransitRoutePlan mkTransitRoutePlan = arg0.getPlan(0);
			System.out.println("һ��" + mkTransitRoutePlan.getNumLines());

			int busLines = mkTransitRoutePlan.getNumLines();// ��ȡһ��Ҫ�������ٴι�����·
			MKLine mkLine = null;
			String popString;// ��Ҫ��popview����ʾ������
			int slice;// �ָ��

			if (busLines == 1) {// ������Ҫ����
				mkLine = mkTransitRoutePlan.getLine(0);
				slice = mkLine.getTitle().indexOf("(");
				// ��ʾ���
				popString = "��'" + mkLine.getGetOnStop().name + "'վ̨����'"
						+ mkLine.getTitle().substring(0, slice) + "'";
				MyPopView(mkLine.getGetOnStop().pt, popString);
				// ��ʾ�յ�
				popString = "��'" + mkLine.getGetOffStop().name + "'վ̨�³�";
				MyPopView(mkLine.getGetOffStop().pt, popString);
			} else {// ����Ҫ����

				for (int i = 0; i < mkTransitRoutePlan.getNumLines(); i++) {
					mkLine = mkTransitRoutePlan.getLine(i);
					slice = mkLine.getTitle().indexOf("(");

					if (i == 0) {// ��Ϊ��һ����ʱ
						popString = "��'" + mkLine.getGetOnStop().name
								+ "'վ̨����'"
								+ mkLine.getTitle().substring(0, slice) + "'";
					} else {// �м�����еĻ��˵�
						popString = "�ٵ�'" + mkLine.getGetOnStop().name
								+ "'վ̨�³�����'"
								+ mkLine.getTitle().substring(0, slice) + "'";
					}
					MyPopView(mkLine.getGetOnStop().pt, popString);
				}
				// �����ʾ�յ�
				popString = "��'" + mkLine.getGetOffStop().name + "'վ̨�³�";
				MyPopView(mkLine.getGetOffStop().pt, popString);
			}
			dismissDialog(CHANGINGBUS);
		}

		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub
			if (arg0 == null) {
				return;
			}
			// ����ʵ�ʾ���
			MKRoute mkRoute = arg0.getPlan(0).getRoute(0);
			distanceView.setText("���룺" + mkRoute.getDistance() + "��");

			// ��·��ֻ������Σ���2����Ϊ����ߵ�λ��׼ȷ��
			if (flag < 2) {
				if (flag == 1) {
					// �ڶ������·��ʱ���ԭ�е�·�����
					clearTopOverLay();
				}

				System.out.println("��");
				// ��ʾ����·��
				RouteOverlay routeOverlay = new RouteOverlay(
						RecommandActivity.this, mapView);
				routeOverlay.setData(arg0.getPlan(0).getRoute(0));
				mapView.getOverlays().add(routeOverlay);
				mapView.invalidate();// ˢ�µ�ͼ

				flag = flag + 1;

				// ������߾��볬��ָ����Χ����ʾ�û��Ƿ�Ҫ�л���������·
				if (mkRoute.getDistance() >= busArea && flag == 1
						&& busArea != 0) {
					model = "bus";// ���빫��ģʽ
					// ���ϲ�ѯ�û����ڳ��У�������һ�����빫��ģʽʱ���������
					mkSearch.reverseGeocode(new GeoPoint(
							(int) (startLat * 1e6), (int) (startLon * 1e6)));
					showDialog(CHANGE);

				}
			}

		}

	}

	// ͨ��������ʾ��ϸ��Ϣ
	public void MyPopView(GeoPoint geoPoint, String detail) {
		// ��ʼ������
		popView = super.getLayoutInflater().inflate(R.layout.popview, null);
		popBtn = (Button) popView.findViewById(R.id.pop);
		// popBtn.setOnClickListener(listener);
		// ���ڰٶȵ�ͼ��
		mapView.addView(popView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, null,
				MapView.LayoutParams.TOP_LEFT));

		// ��ȡ��������ݶ�����ʾ��λ��
		mapView.updateViewLayout(popView, new MapView.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, geoPoint,
				MapView.LayoutParams.BOTTOM_CENTER));
		// popView.setVisibility(View.VISIBLE);
		popBtn.setText(detail);
	}

	// �жϷ��ؽ���������Ƿ�Ϊһ������վ
	public boolean isStation(String name) {
		// �ж϶����������������վ�����ľƵ�������
		if (name.contains("�Ƶ�") || name.contains("(") || name.contains("�ư�")) {
			return false;
		}
		return true;

	}

	// �ҳ�������̵ģ�����������
	public int minDistance(double[] d) {
		double min = d[0];
		int j = 0;
		for (int i = 1; i < d.length; i++) {
			if (min > d[i]) {
				min = d[i];
				j = i;
			}
		}

		return j;// ������С�ľ����������

	}

	// �������
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

	// ��ʼ����ͼ
	public void initMap() {

		bMapManager = new BMapManager(this);
		bMapManager.init(key, null);
		initMapActivity(bMapManager);

		mapView = (MapView) findViewById(R.id.bmapView);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(18);

		// �ڵ�ͼ����ʾ�ҵ�λ��
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);

	}

	// ��ͼ���ҵ�λ�ö���������
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (bMapManager != null) {
			// bMapManager.getLocationManager().removeUpdates(this);
			// �ر�GPS��λ
			bMapManager.getLocationManager().enableProvider(
					(int) MKLocationManager.MK_GPS_PROVIDER);
			myLocationOverlay.disableMyLocation();// ֹͣλ�ø���
			myLocationOverlay.disableCompass();// �ر�ָ�������
			bMapManager.stop();
		}

		// ��ȡ�û��Ƿ���Ҫ������Ļ����
		if (Utils.getKeepScreen(preferences)) {
			// �ͷ���Ļ����
			Utils.keepScreenOn(RecommandActivity.this, false);
		}
		super.onPause();
	}

	// �ظ���ͼ���ҵ�λ�õ���ʾ
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (bMapManager != null) {
			bMapManager.getLocationManager().requestLocationUpdates(this);
			// ʹ��GPS��λ
			bMapManager.getLocationManager().enableProvider(
					(int) MKLocationManager.MK_GPS_PROVIDER);

			myLocationOverlay.enableMyLocation();
			myLocationOverlay.enableCompass();
			bMapManager.start();
			// �ƶ��ٶȵ�ͼ�¼�����
			mapView.regMapViewListener(bMapManager, this);
		}

		// ��ȡ�û��Ƿ���Ҫ������Ļ����
		if (Utils.getKeepScreen(preferences)) {
			// ������Ļ����
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
			builder.setTitle("��ܰ��ʾ");
			builder.setMessage("�õ���վ�����Զ���Ƿ�ĳ˹�����");
			builder.setPositiveButton("��",
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
			builder.setNegativeButton("��,�����·",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							model = "walk";// �ص�����ģʽ
						}
					});

			return builder.create();

		case Utils.OPENGPS:
			builder.setTitle("��ߡ��ҵ�λ�á��ľ�ȷ��");
			builder.setMessage("��û�д�GPS����,��GPS���ṩ��׼ȷ�Ķ�λ��");
			final CheckBox checkBox=new CheckBox(this);
			builder.setView(checkBox);
			checkBox.setText("������ʾ");
			builder.setPositiveButton("����",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
							Utils.setPromptGps(preferences, !checkBox.isChecked());
						}
					});
			builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					Utils.setPromptGps(preferences, !checkBox.isChecked());
				}
			});
			return builder.create();

		case CHANGINGBUS:
			busSearchingDialog = new ProgressDialog(RecommandActivity.this);
			busSearchingDialog.setMessage("���ڲ�ѯ����·��...");
			busSearchingDialog.setCancelable(false);
			busSearchingDialog.setButton("ȡ��",
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
			searchingDialog.setMessage("����������������ĵ���վ...");
			searchingDialog.setCancelable(false);
			searchingDialog.show();
			return searchingDialog;

		}
		return super.onCreateDialog(id);
	}

	// ����MyLocationOverlay���õ�����ȷ���Ƿ��ڵ�ͼ����ʾ��ǰλ��
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
			// ��ȡ�û���ǰλ��
			// myGeoPoint = new GeoPoint((int) (location.getLatitude() * 1e6),
			// (int) (location.getLongitude() * 1e6));
			startLat = location.getLatitude();
			startLon = location.getLongitude();

			// startLat = 31.249162;
			// startLon = 121.487899;

			myGeoPoint = new GeoPoint((int) (startLat * 1e6),
					(int) (startLon * 1e6));

			if (navigation) {
				mapView.getController().animateTo(myGeoPoint);// ʵʩ����
			}

			// �����һ������ʱ���в�������ĵ���վ
			if (flag == 0) {
				mapView.getController().animateTo(myGeoPoint);// �������ĵ�
				mkSearch.poiSearchNearBy("����%վ", new GeoPoint(
						(int) (startLat * 1e6), (int) (startLon * 1e6)),
						searchArea);
			}

			// ���ߵ���ʱ����Ҫ����ˢ�¾���
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

	// �������ͼ�㣬���»��Ƶ�ͼ
	public void clearTopOverLay() {

		List<Overlay> overlays = mapView.getOverlays();
		if (overlays.size() != 0) {
			overlays.remove(overlays.size() - 1);
		}
	}

	public void onMapMoveFinish() {
		// TODO Auto-generated method stub
		navigation = false;// �û��ƶ���ͼ�󣬲����뵼��״̬

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
