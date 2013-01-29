package com.killerban.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.killerban.okclock.R;

public class ClockEditButton extends Button {
	public ClockEditButton(Context context) {
		super(context);
		this.format();
	}

	public Button format() {
		this.setBackgroundResource(R.drawable.bt_clock_edit); // ���ñ���
		this.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1.0f)); // ���ó���������
		this.setShadowLayer(5, 0, 0, getResources().getColor(R.color.black)); // ������Ӱ
		this.setTextColor(getResources().getColor(R.color.white)); // ����������ɫ
		this.setTypeface(Typeface.DEFAULT_BOLD); // ��������
		return this;
	}
}
