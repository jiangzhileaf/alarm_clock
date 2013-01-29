package com.killerban.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.killerban.okclock.R;

public class ClockToggleButton extends ToggleButton {

	public ClockToggleButton(Context context) {
		super(context);
		this.format();
	}

	public Button format() {
		this.setBackgroundResource(R.drawable.bt_toggle_bg); // ���ñ���
		this.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)); // ���ó���������
		this.setTextOff(null); // ���ÿ��عر�״̬����Ϊ��
		this.setTextOn(null); // ���ÿ��ع�����̬����Ϊ��
		this.setTextKeepState(""); // ���ó�ʼ����
		this.setShadowLayer(5, 0, 0, getResources().getColor(R.color.black)); // ������Ӱ
		this.setTextColor(getResources().getColor(R.color.white)); // ����������ɫ
		this.setTypeface(Typeface.DEFAULT_BOLD); // ��������
		return this;
	}
}
