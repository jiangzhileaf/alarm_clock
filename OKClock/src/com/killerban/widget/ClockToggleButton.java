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
		this.setBackgroundResource(R.drawable.bt_toggle_bg); // 设置背景
		this.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)); // 设置长、宽、比重
		this.setTextOff(null); // 设置开关关闭状态文字为空
		this.setTextOn(null); // 设置开关关启动态文字为空
		this.setTextKeepState(""); // 设置初始文字
		this.setShadowLayer(5, 0, 0, getResources().getColor(R.color.black)); // 设置阴影
		this.setTextColor(getResources().getColor(R.color.white)); // 设置字体颜色
		this.setTypeface(Typeface.DEFAULT_BOLD); // 设置字体
		return this;
	}
}
