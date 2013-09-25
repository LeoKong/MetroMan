package com.klcutil;


import com.klcmetro.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

public class Selectpopwindow extends PopupWindow {
	private Button pop1, pop2, pop3,pop4;
	private View menuView;

	public Selectpopwindow(Activity context,
			OnClickListener itemsOnClickListener,String s1,String s2,String s3,String s4) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		menuView = inflater.inflate(R.layout.popup_layout, null);

		pop1 = (Button) menuView.findViewById(R.id.popitem1);
		pop2 = (Button) menuView.findViewById(R.id.popitem2);
		pop3 = (Button) menuView.findViewById(R.id.popitem3);
		pop4 = (Button) menuView.findViewById(R.id.popitem4);
		
		pop1.setText(s1);
		pop2.setText(s2);
		pop3.setText(s3);
		pop4.setText(s4);

		// ��ť�ļ���
		pop1.setOnClickListener(itemsOnClickListener);
		pop2.setOnClickListener(itemsOnClickListener);
		pop3.setOnClickListener(itemsOnClickListener);
		pop4.setOnClickListener(itemsOnClickListener);
		// ����view
		this.setContentView(menuView);

		// ���õ�������Ŀ�͸�
		this.setWidth(LayoutParams.FILL_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		// ���õ������ڿɵ��
		this.setFocusable(true);
		// ���õ�������Ķ���
		this.setAnimationStyle(R.style.AnimPopUp);
		// ʵ����һ����ɫΪ͸��
		ColorDrawable dw = new ColorDrawable(0);
		// ���õ�������ı���
		this.setBackgroundDrawable(dw);

		// ���һ���������ڴ�������λ�õ����������
		menuView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int height = menuView.findViewById(R.id.popwindow).getTop();
				int y = (int) event.getY();
				// ��ָ�뿪��Ļʱ
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});
	}

}
