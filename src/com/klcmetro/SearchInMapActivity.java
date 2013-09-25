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

	private BMapManager bMapManager = null;// ��ͼ����
	private MapView mapView = null;
	private MKSearch mkSearch = null;// �����ͼ������
	private MyLocationOverlay myLocationOverlay = null;
	private String key = "7AC130722DF5C9838FA595A1152C3951DBD1EB48";
	GeoPoint myGeoPoint;
	private double startLat = 0;
	private double startLon = 0;
	private double endLat = 0;
	private double endLon = 0;
	private MKPlanNode startNode, endNode;// ������ʼ��
	private String myCity = "";

	private ImageButton searchButton1, searchButton2;
	private RelativeLayout layout1, layout2;
	private EditText searchEditText1, searchEditText2;
	private View popView = null;// ����������
	private List<View> popList = new ArrayList<View>();// ��¼�����е�popview�������
	private Button popBtn;
	private TextView searchingTextView;

	SharedPreferences preferences;
	ProgressDialog searchingDialog;
	ProgressDialog locatingDialog;

	private int flag = 0;// ��־onlocationchange�ǲ��ǵ�һ������
	private int whichEdit = 1;// ��־��һ��edittext��������

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_station);

		// ��ȡ�û�����
		preferences = this.getSharedPreferences("setting", MODE_PRIVATE);

		searchingTextView = (TextView) findViewById(R.id.show_searching);
		searchButton1 = (ImageButton) findViewById(R.id.search_btn1);
		searchButton2 = (ImageButton) findViewById(R.id.search_btn2);
		searchEditText1 = (EditText) findViewById(R.id.editsearch1);
		searchEditText2 = (EditText) findViewById(R.id.editsearch2);

		layout1 = (RelativeLayout) findViewById(R.id.first_relativelayout);// ����ʾ����1����ʾ�û����ڻ�ȡλ����Ϣ
		layout2 = (RelativeLayout) findViewById(R.id.second_relativelayout);

		searchButton1.setOnClickListener(this);
		searchButton2.setOnClickListener(this);

		// ��ʼ����ͼ
		initMap();
		// ʵ������ѯ��
		mkSearch = new MKSearch();
		mkSearch.init(bMapManager, new MySearchListener());

		// ��ʾ"���ڻ�ȡλ����Ϣ"����ʾ��
		showDialog(LOCATING);

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.search_btn1:
			// ���������ʧ
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(SearchInMapActivity.this
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

			String startStation = searchEditText1.getText().toString();
			if (startStation.equals("")) {
				searchEditText1.setError("���������վ");
				searchEditText1.requestFocus();
				return;
			}
			if (!startStation.contains("����վ")) {
				startStation = startStation + "����վ";
			}

			// ��ʾ��ʾ��
			showDialog(SEARCHING);

			// ���ҳ�������վ�ľ�γ��
			mkSearch.poiSearchInCity(myCity, startStation);
			whichEdit = 1;

			break;

		case R.id.search_btn2:
			layout2.setVisibility(View.VISIBLE);// ��������
			break;
		}

	}

	// ��д���Ҽ���
	public class MySearchListener implements MKSearchListener {

		public void onGetAddrResult(MKAddrInfo result, int error) {
			// TODO Auto-generated method stub
			// ����ʾ����ʧ
			dismissDialog(LOCATING);
			if (error != 0) {
				if (error == MKEvent.ERROR_NETWORK_CONNECT) {
					searchingTextView.setText("��������ʧ�ܣ���������!");
				}
				if (error == MKEvent.ERROR_NETWORK_DATA) {
					searchingTextView.setText("�������ݴ���!");
				}
				return;
			}
			String[] citys = new String[] { "����", "�Ϻ�", "����", "����", "���", "���",
					"�Ͼ�", "����", "�ɶ�", "��ɽ", "����", "����", "����", "����", "����", "�人",
					"����", "����" };
			MKGeocoderAddressComponent address = result.addressComponents;
			boolean have = false;// ��¼�Ƿ�ͨ����

			for (int i = 0; i < citys.length; i++) {
				if (address.city.contains(citys[i])) {
					have = true;
					break;
				}
			}

			if (have) {
				myCity = address.city;
				// ��ȡ�û����ڳ���֮����ʾ����
				layout2.setVisibility(View.VISIBLE);
			} else {
				searchingTextView.setText("�����ڵĳ���Ϊ:" + address.city
						+ ",��δ��ͨ��������");
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
			// �Ҳ���ֱ�ӷ���
			if (result == null || error != 0) {
				// ����ʾ����ʧ
				dismissDialog(SEARCHING);
				// �ж����������Ƿ�����
				if (error == MKEvent.ERROR_NETWORK_CONNECT) {
					Toast.makeText(SearchInMapActivity.this, "��������ʧ��!",
							Toast.LENGTH_LONG).show();
					return;
				} else if (error == MKEvent.ERROR_NETWORK_DATA) {
					Toast.makeText(SearchInMapActivity.this, "�������ݴ���!",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (whichEdit == 1) {
					searchEditText1.setError("�Ҳ����õ���վ");
				} else {
					searchEditText2.setError("�Ҳ����õ���վ");
				}
				System.out.println("nofound" + ";error=" + error);
				return;
			}

			// �ҵ����ǲ��ǵ���վҲ����
			if (result.getPoi(0) == null) {
				// ����ʾ����ʧ
				dismissDialog(SEARCHING);
				if (whichEdit == 1) {
					searchEditText1.setError("�Ҳ����õ���վ");
				} else {
					searchEditText2.setError("�Ҳ����õ���վ");
				}
				System.out.println("nofound2");
				return;
			}

			int bestStation = 0;// ��¼���ҽ����׼ȷ��
			MKPoiInfo mkPoiInfo = null;
			System.out.println("��������" + myCity);
			for (int i = 0; i < result.getAllPoi().size(); i++) {
				System.out.println("���صĵ�:" + result.getPoi(i).name);
			}
			switch (whichEdit) {
			case 1:
				// ��߲��ҵľ�ȷ��
				for (int i = 0; i < result.getAllPoi().size(); i++) {
					if (result.getPoi(i).name.contains(searchEditText1
							.getText().toString())) {
						bestStation = i;
						break;
					}
					System.out.println("���صĵ�:" + result.getPoi(i).name);
				}
				mkPoiInfo = result.getPoi(bestStation);
				mapView.getController().animateTo(mkPoiInfo.pt);// �����Ϊ���ĵ�
				// �������
				startNode = new MKPlanNode();
				startNode.pt = mkPoiInfo.pt;
				System.out.println("��㣺" + mkPoiInfo.name);
				whichEdit = 2;

				// ��ѯ�յ�վ�ľ�γ��
				String endStation = searchEditText2.getText().toString();
				if (endStation.equals("")) {
					// ����ʾ����ʧ
					dismissDialog(SEARCHING);
					searchEditText2.setError("�������յ�վ");
					searchEditText2.requestFocus();
					return;
				}
				if (!endStation.contains("����վ")) {
					endStation = endStation + "����վ";
				}
				// �ٵ���һ���������
				mkSearch.poiSearchInCity(myCity, endStation);

				break;

			case 2:
				// ��߲��ҵľ�ȷ��
				for (int i = 0; i < result.getAllPoi().size(); i++) {
					if (result.getPoi(i).name.contains(searchEditText2
							.getText().toString())) {
						bestStation = i;
						break;
					}
					System.out.println("���صĵ�:" + result.getPoi(i).name);
				}
				mkPoiInfo = result.getPoi(bestStation);
				// �����յ�
				endNode = new MKPlanNode();
				endNode.pt = mkPoiInfo.pt;
				System.out.println("�յ㣺" + mkPoiInfo.name);
				// �õ��յ����վ֮�󣬽���·�߲���
				mkSearch.setTransitPolicy(MKSearch.EBUS_TIME_FIRST);
				mkSearch.transitSearch(myCity, startNode, endNode);
				break;
			}

			// ��֪���˵���վ֮�󣬽��빫������

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
				Toast.makeText(SearchInMapActivity.this, "�Բ����޵���·��",
						Toast.LENGTH_LONG).show();
				return;
			}

			System.out.println("������" + arg0.getNumPlan());

			int best = 0;
			int mlines = 0;// ��¼i�����ж��ٵ�����·
			int blines = 0;// Ŀǰ���ĵ�����·

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

				System.out.println("��" + (i + 1) + "���Ĺ���·�߰�����" + mlines);
			}

			System.out.println("best:" + (best + 1));

			// �ڱ�������ʾվ�����ʹ�Լ��Ҫ��ʱ��
			layout2.setVisibility(View.GONE);
			searchButton2.setVisibility(View.VISIBLE);

			clearTopOverLay();// ���ԭ��·��
			System.out.println("���뻭��");
			TransitOverlay transitOverlay = new TransitOverlay(
					SearchInMapActivity.this, mapView);
			transitOverlay.setData(arg0.getPlan(best));
			mapView.getOverlays().add(transitOverlay);
			mapView.invalidate();

			MKTransitRoutePlan mkTransitRoutePlan = arg0.getPlan(best);
			// System.out.println("һ��" + mkTransitRoutePlan.getNumLines());

			int busLines = mkTransitRoutePlan.getNumLines();// ��ȡһ��Ҫ�������ٴε�����·
			int busStations = 0;// һ���ж��ٸ�վ
			MKLine mkLine = null;
			String popString;// ��Ҫ��popview����ʾ������
			int slice;// �ָ��

			if (busLines == 1) {// ������Ҫ����
				mkLine = mkTransitRoutePlan.getLine(0);
				slice = mkLine.getTitle().indexOf("(");
				// ��ʾ���
				popString = "��'" + mkLine.getGetOnStop().name + "'վ����'"
						+ mkLine.getTitle().substring(0, slice) + "'";
				MyPopView(mkLine.getGetOnStop().pt, popString);
				// ��ʾ�յ�
				popString = "��'" + mkLine.getGetOffStop().name + "'վ�³�";
				MyPopView(mkLine.getGetOffStop().pt, popString);

				// ����һ���ж��ٸ�վ
				busStations = mkLine.getNumViaStops();
			} else {// ����Ҫ����

				for (int i = 0; i < mkTransitRoutePlan.getNumLines(); i++) {
					mkLine = mkTransitRoutePlan.getLine(i);
					slice = mkLine.getTitle().indexOf("(");

					if (i == 0) {// ��Ϊ��һ����ʱ
						popString = "��'" + mkLine.getGetOnStop().name + "'վ����'"
								+ mkLine.getTitle().substring(0, slice) + "'";
					} else {// �м�����еĻ��˵�
						popString = "�ٵ�'" + mkLine.getGetOnStop().name
								+ "'վ�³�����'"
								+ mkLine.getTitle().substring(0, slice) + "'";
					}
					MyPopView(mkLine.getGetOnStop().pt, popString);

					// ����һ���ж��ٸ�վ
					busStations = busStations + mkLine.getNumViaStops();
				}
				// �����ʾ�յ�
				popString = "��'" + mkLine.getGetOffStop().name + "'վ�³�";
				MyPopView(mkLine.getGetOffStop().pt, popString);
			}

			searchingTextView.setText("һ��" + busStations + "վ��Ԥ����Ҫ"
					+ busStations * 3 + "����");
			// �������ʾ����ʧ
			dismissDialog(SEARCHING);

		}

		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

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
		popList.add(popView);
	}

	// ��ʼ����ͼ
	public void initMap() {

		bMapManager = new BMapManager(this);
		bMapManager.init(key, null);
		initMapActivity(bMapManager);

		mapView = (MapView) findViewById(R.id.bmapView);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(15);

		myLocationOverlay = new MyLocationOverlay(this, mapView);
		// �ڵ�ͼ����ʾ�ҵ�λ��
		mapView.getOverlays().add(myLocationOverlay);

	}

	// ��ͼ���ҵ�λ�ö���������
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (bMapManager != null) {
			bMapManager.getLocationManager().removeUpdates(this);
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
			Utils.keepScreenOn(SearchInMapActivity.this, false);
		}
		super.onPause();
	}

	// �ָ���ͼ���ҵ�λ�õ���ʾ
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
			System.out.println("resume");
		}

		// ��ȡ�û��Ƿ���Ҫ������Ļ����
		if (Utils.getKeepScreen(preferences)) {
			// ������Ļ����
			Utils.keepScreenOn(SearchInMapActivity.this, true);
		}
		super.onResume();
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
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case SEARCHING:
			searchingDialog = new ProgressDialog(SearchInMapActivity.this);
			searchingDialog.setMessage("���ڲ�ѯ...");
			searchingDialog.setCancelable(false);
			searchingDialog.setButton("ȡ��",
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
			locatingDialog.setMessage("���ڻ�ȡ����λ����Ϣ...");
			locatingDialog.setCancelable(false);
			locatingDialog.show();
			return locatingDialog;
		}
		return super.onCreateDialog(id);
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if (location != null) {
			// ��ȡ�û���ǰλ��
			startLat = location.getLatitude();
			startLon = location.getLongitude();

//			startLat = 22.703945;
//			 startLon = 114.056696;

			myGeoPoint = new GeoPoint((int) (startLat * 1e6),
					(int) (startLon * 1e6));

			if (flag == 0) {
				// �����û���ǰ���ڵĳ��У�����Ƿ��е������񣬽�ִ��һ��
				mkSearch.reverseGeocode(new GeoPoint((int) (startLat * 1e6),
						(int) (startLon * 1e6)));
				mapView.getController().animateTo(myGeoPoint);// �������ĵ�
			}
			flag = 1;

		}

	}

	// �������ͼ�㣬���»��Ƶ�ͼ
	public void clearTopOverLay() {

		List<Overlay> overlays = mapView.getOverlays();

		if (overlays.size() != 0) {
			overlays.remove(overlays.size() - 1);
		}
		if (popView != null) {
			// �������popview
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
