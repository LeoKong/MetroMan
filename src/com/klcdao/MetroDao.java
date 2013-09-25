package com.klcdao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.klcmodel.MetroMapInfo;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MetroDao {
	String path = "data/data/com.klcmetro/metro.db";

	SQLiteDatabase sqLiteDatabase;

	public MetroDao(Context context) {
		super();
		// TODO Auto-generated constructor stub
		this.sqLiteDatabase = getDataBase(context);
	}

	/**
	 * 通过城市名获取城市地铁信息
	 * 
	 * @param name
	 * @return
	 */
	public MetroMapInfo getMapInfo(String name) {
		MetroMapInfo mapInfo = new MetroMapInfo();
		Cursor cursor;
		cursor = sqLiteDatabase
				.query("info", new String[] { "name", "url", "filename" },
						"name=?", new String[] { name }, null, null, null);
		if (cursor.moveToNext()) {
			mapInfo.setCityName(cursor.getString(0));
			mapInfo.setUrl(cursor.getString(1));
			mapInfo.setFileName(cursor.getString(2));
		}
		return mapInfo;

	}

	/**
	 * 获取数据库中所有城市名
	 * 
	 * @return
	 */
	public List<String> getAllMap() {
		List<String> list = new ArrayList<String>();
		Cursor cursor;
		cursor = sqLiteDatabase.query("info", new String[] { "name" }, null,
				null, null, null, null);
		while (cursor.moveToNext()) {
			list.add(cursor.getString(0));
		}
		cursor.close();
		return list;

	}

	// 打开数据库
	public SQLiteDatabase getDataBase(Context context) {
		File file = new File(path);
		// 判断路径下文件是否存在
		if (file.exists()) {
			System.out.println("you");
			// 存在则直接带数据库
			return SQLiteDatabase.openOrCreateDatabase(file, null);
		} else {
			System.out.println("没");
			try {
				// 不存在则把asset中的数据库导入到指定路径中，首次运行时会执行
				// 得到资源
				AssetManager manager = context.getAssets();
				// 得到数据库的输入流
				InputStream inputStream = manager.open("metro.db");
				// 用输出流写到SDcard上
				FileOutputStream outputStream = new FileOutputStream(file);
				// 用1kb写一次
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = inputStream.read(buffer)) > 0) {
					outputStream.write(buffer, 0, count);

				}
				outputStream.flush();
				outputStream.close();
				inputStream.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				return null;
			}
		}
		// 写入完成后再执行一次该函数
		return getDataBase(context);
	}

}
