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
	 * ͨ����������ȡ���е�����Ϣ
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
	 * ��ȡ���ݿ������г�����
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

	// �����ݿ�
	public SQLiteDatabase getDataBase(Context context) {
		File file = new File(path);
		// �ж�·�����ļ��Ƿ����
		if (file.exists()) {
			System.out.println("you");
			// ������ֱ�Ӵ����ݿ�
			return SQLiteDatabase.openOrCreateDatabase(file, null);
		} else {
			System.out.println("û");
			try {
				// ���������asset�е����ݿ⵼�뵽ָ��·���У��״�����ʱ��ִ��
				// �õ���Դ
				AssetManager manager = context.getAssets();
				// �õ����ݿ��������
				InputStream inputStream = manager.open("metro.db");
				// �������д��SDcard��
				FileOutputStream outputStream = new FileOutputStream(file);
				// ��1kbдһ��
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
		// д����ɺ���ִ��һ�θú���
		return getDataBase(context);
	}

}
