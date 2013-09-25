package com.klcmetro;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import com.klcdao.MetroDao;
import com.klcmodel.MetroMapInfo;
import com.klcutil.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MetroMapActivity extends Activity {
	protected static final int DOWMLOAD = 1;
	protected static final int PROGRESS = 2;
	private ListView mapListView;
	private List<String> mapList;
	private ArrayAdapter<String> adapter;
	private String path = Environment.getExternalStorageDirectory().getPath()
			+ "/Metros";// ����ͼ��ŵ��ļ���·��
	private String filePath = "";// ����ͼ��ŵľ���·��

	private boolean flag = false;// ��־�Ƿ����سɹ�
	private String source = "";// ���ص���ͼ��url

	MetroDao metroDao;
	ProgressDialog progressDialog;
	
	RelativeLayout mAdContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_list);
		mapListView = (ListView) findViewById(R.id.maplist);

		// ��ȡ���ݵ�arraylist��
		metroDao = new MetroDao(MetroMapActivity.this);
		mapList = metroDao.getAllMap();
		// ��listview����ʾ
		adapter = new ArrayAdapter<String>(MetroMapActivity.this,
				R.layout.list_item, mapList);
		mapListView.setAdapter(adapter);

		mapListView.setOnItemClickListener(listener);
		
		//�����������
		if (!Utils.checkNet(MetroMapActivity.this)) {
			showDialog(Utils.OPENNET);
		}
		
	}

	public OnItemClickListener listener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			// ��ȡ�û�����ĳ��еĵ�����Ϣ
			MetroMapInfo info = new MetroMapInfo();
			info = metroDao.getMapInfo(parent.getItemAtPosition(position)
					.toString());

			// ��ȡ�û�ѡ��ĵ���ͼ
			filePath = path + "/" + info.getFileName();
			File file = new File(filePath);
			// ����ͼ�Ƿ���sd�д���
			if (file.exists()) {
				// �����������ʾ
				Intent intent = new Intent();
				intent.setClass(MetroMapActivity.this, ShowMapActivity.class);
				intent.putExtra("filepath", filePath);
				startActivity(intent);
			} else {
				source = info.getUrl();
				// ����������ʾ�û��Ƿ���Ҫ����
				showDialog(DOWMLOAD);

			}

		}

	};

	// �첽�߳�
	// ���������ֱ���URL��pressִ�еİٷֱȣ����յķ��ؽ��
	private class MyProgressTask extends AsyncTask<String, Integer, Integer> {

		// �÷�����������ʼǰ���ã�һ��������ʾdialog
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// ��ʾ������
			showDialog(PROGRESS);
		}

		// ��һ��������������
		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			URL url = null;
			HttpURLConnection urlConnection = null;
			InputStream inputStream = null;
			FileOutputStream fileOutputStream = null;

			try {

				// ����URL����
				url = new URL(params[0]);
				if (url != null) {
					// ����һ������
					urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setConnectTimeout(5000);// �������ӳ�ʱʱ��
					urlConnection.setRequestMethod("GET");// ����ʽ
					int response_code = urlConnection.getResponseCode();
					if (response_code == 200) {
						// ��ȡ����������
						inputStream = urlConnection.getInputStream();
					} else {
						Toast.makeText(MetroMapActivity.this, "���ӷ�����ʧ��!",
								Toast.LENGTH_LONG).show();
						return null;
					}

					if (inputStream != null) {

						// �״�����Ϊ����ר�ŵ��ļ��д��ͼƬ
						File myfile = new File(path);
						if (!myfile.exists()) {
							myfile.mkdir();
						}

						System.out.println(filePath);
						// ��SD���ϴ����ļ�
						File file = new File(filePath);
						// ����һ���ļ����������
						fileOutputStream = new FileOutputStream(file);
						// ����һ���ֽ�����
						byte buf[] = new byte[1024];

						// ��ȡ�ļ��������������
						int length = urlConnection.getContentLength();
						System.out.println("" + length);
						int numread;
						int total = 0;
						while ((numread = inputStream.read(buf)) != -1) {
							fileOutputStream.write(buf, 0, numread);
							total += numread;
							System.out.println(""
									+ (int) (total * 100 / length));
							// ����onprogressupdate�������������ֵ
							publishProgress((int) (total * 100 / length));
						}
						flag = true;
					}

				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				flag = false;
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}
					if (urlConnection != null) {
						urlConnection.disconnect();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(MetroMapActivity.this, "�ļ������쳣��",
							Toast.LENGTH_LONG).show();
				}

			}
			return null;
		}

		// �ڶ����������뵽����
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			System.out.println(values[0] + "");
			progressDialog.setProgress(values[0]);
		}

		// ������������������
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			dismissDialog(PROGRESS);
			if (flag) {
				Toast.makeText(MetroMapActivity.this, "���سɹ�",
						Toast.LENGTH_SHORT).show();
				// ���سɹ���Ϊ�û��򿪵�ͼ
				Intent intent = new Intent();
				intent.setClass(MetroMapActivity.this, ShowMapActivity.class);
				intent.putExtra("filepath", filePath);
				startActivity(intent);
				System.out.println("ok");
			} else {
				Toast.makeText(MetroMapActivity.this, "��������ʧ�ܣ�",
						Toast.LENGTH_SHORT).show();
			}
		}

	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MetroMapActivity.this);
		builder.setCancelable(false);
		switch (id) {
		case DOWMLOAD:
			builder.setMessage("�Ƿ����ظó��еĵ�����·ͼ");
			builder.setPositiveButton("��",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							// ��ʼ�첽���ص���ͼ
							new MyProgressTask().execute(source);
						}
					});
			builder.setNegativeButton("��", null);

			return builder.create();

		case PROGRESS:
			progressDialog = new ProgressDialog(MetroMapActivity.this);
			progressDialog.setMessage("��������......");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setCancelable(true);
			progressDialog.show();
			return progressDialog;
			
		case Utils.OPENNET:
			builder.setTitle("�޿��õ���������");
			builder.setMessage("�뿪��GPRS��WIFI��������!");
			builder.setPositiveButton("������", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					startActivity(new Intent(Settings.ACTION_SETTINGS));
				}
			});
			builder.setNegativeButton("ȡ��", null);
			return builder.create();

		}
		return null;
	}

}
