package com.yzj.resources;

import com.killerban.okclock.R;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

public class ClockToggleButton extends ToggleButton {

	public ClockToggleButton(Context context) {
		super(context);
	}
	
	public Button format() {
		this.setBackgroundResource(R.drawable.bt_toggle_bg);                            // ���ñ���
		this.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));                                            // ���ó���������
		this.setTextOff(null);                                                          // ���ÿ��عر�״̬����Ϊ��
		this.setTextOn(null);                                                           // ���ÿ��ع�����̬����Ϊ��
		this.setShadowLayer(5, 0, 0, getResources().getColor(R.color.black));           // ������Ӱ
		this.setTextColor(getResources().getColor(R.color.white));                      // ����������ɫ
		this.setTypeface(Typeface.DEFAULT_BOLD);                                        // ��������
		return this;
	}
	
}
