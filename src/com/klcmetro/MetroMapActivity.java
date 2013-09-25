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
			+ "/Metros";// 地铁图存放的文件夹路径
	private String filePath = "";// 地铁图存放的具体路径

	private boolean flag = false;// 标志是否下载成功
	private String source = "";// 下载地铁图的url

	MetroDao metroDao;
	ProgressDialog progressDialog;
	
	RelativeLayout mAdContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_list);
		mapListView = (ListView) findViewById(R.id.maplist);

		// 获取数据到arraylist中
		metroDao = new MetroDao(MetroMapActivity.this);
		mapList = metroDao.getAllMap();
		// 在listview中显示
		adapter = new ArrayAdapter<String>(MetroMapActivity.this,
				R.layout.list_item, mapList);
		mapListView.setAdapter(adapter);

		mapListView.setOnItemClickListener(listener);
		
		//检查网络连接
		if (!Utils.checkNet(MetroMapActivity.this)) {
			showDialog(Utils.OPENNET);
		}
		
	}

	public OnItemClickListener listener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			// 获取用户点击的城市的地铁信息
			MetroMapInfo info = new MetroMapInfo();
			info = metroDao.getMapInfo(parent.getItemAtPosition(position)
					.toString());

			// 读取用户选择的地铁图
			filePath = path + "/" + info.getFileName();
			File file = new File(filePath);
			// 地铁图是否在sd中存在
			if (file.exists()) {
				// 存在则进行显示
				Intent intent = new Intent();
				intent.setClass(MetroMapActivity.this, ShowMapActivity.class);
				intent.putExtra("filepath", filePath);
				startActivity(intent);
			} else {
				source = info.getUrl();
				// 不存在则提示用户是否需要下载
				showDialog(DOWMLOAD);

			}

		}

	};

	// 异步线程
	// 三个参数分别是URL，press执行的百分比，最终的返回结果
	private class MyProgressTask extends AsyncTask<String, Integer, Integer> {

		// 该方法会在任务开始前调用，一般用于显示dialog
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// 显示进度条
			showDialog(PROGRESS);
		}

		// 第一个参数传到这里
		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			URL url = null;
			HttpURLConnection urlConnection = null;
			InputStream inputStream = null;
			FileOutputStream fileOutputStream = null;

			try {

				// 创建URL对象
				url = new URL(params[0]);
				if (url != null) {
					// 创建一个连接
					urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setConnectTimeout(5000);// 设置连接超时时间
					urlConnection.setRequestMethod("GET");// 请求方式
					int response_code = urlConnection.getResponseCode();
					if (response_code == 200) {
						// 获取输入流对象
						inputStream = urlConnection.getInputStream();
					} else {
						Toast.makeText(MetroMapActivity.this, "连接服务器失败!",
								Toast.LENGTH_LONG).show();
						return null;
					}

					if (inputStream != null) {

						// 首次运行为创建专门的文件夹存放图片
						File myfile = new File(path);
						if (!myfile.exists()) {
							myfile.mkdir();
						}

						System.out.println(filePath);
						// 在SD卡上创建文件
						File file = new File(filePath);
						// 创建一个文件输出流对象
						fileOutputStream = new FileOutputStream(file);
						// 创建一个字节数组
						byte buf[] = new byte[1024];

						// 读取文件到输出流对象中
						int length = urlConnection.getContentLength();
						System.out.println("" + length);
						int numread;
						int total = 0;
						while ((numread = inputStream.read(buf)) != -1) {
							fileOutputStream.write(buf, 0, numread);
							total += numread;
							System.out.println(""
									+ (int) (total * 100 / length));
							// 调用onprogressupdate方法，传入进度值
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
					Toast.makeText(MetroMapActivity.this, "文件保存异常！",
							Toast.LENGTH_LONG).show();
				}

			}
			return null;
		}

		// 第二个参数传入到这里
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			System.out.println(values[0] + "");
			progressDialog.setProgress(values[0]);
		}

		// 第三个参数传入这里
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			dismissDialog(PROGRESS);
			if (flag) {
				Toast.makeText(MetroMapActivity.this, "下载成功",
						Toast.LENGTH_SHORT).show();
				// 下载成功后为用户打开地图
				Intent intent = new Intent();
				intent.setClass(MetroMapActivity.this, ShowMapActivity.class);
				intent.putExtra("filepath", filePath);
				startActivity(intent);
				System.out.println("ok");
			} else {
				Toast.makeText(MetroMapActivity.this, "网络连接失败！",
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
			builder.setMessage("是否下载该城市的地铁线路图");
			builder.setPositiveButton("是",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							// 开始异步下载地铁图
							new MyProgressTask().execute(source);
						}
					});
			builder.setNegativeButton("否", null);

			return builder.create();

		case PROGRESS:
			progressDialog = new ProgressDialog(MetroMapActivity.this);
			progressDialog.setMessage("正在下载......");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setCancelable(true);
			progressDialog.show();
			return progressDialog;
			
		case Utils.OPENNET:
			builder.setTitle("无可用的网络连接");
			builder.setMessage("请开启GPRS或WIFI网络连接!");
			builder.setPositiveButton("打开设置", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					startActivity(new Intent(Settings.ACTION_SETTINGS));
				}
			});
			builder.setNegativeButton("取消", null);
			return builder.create();

		}
		return null;
	}

}
