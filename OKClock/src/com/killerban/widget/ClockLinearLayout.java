package com.killerban.widget;

import android.content.Context;
import android.widget.LinearLayout;

public class ClockLinearLayout extends LinearLayout {

	// id 用于标志其组件存放的闹钟id
	private int idOfClock;		
	private ClockEditButton editButton;
	private ClockToggleButton toggleButton;

	public ClockLinearLayout(Context context) {
		super(context);
		setStyle();
	}


	public int getIdOfClock() {
		return idOfClock;
	}


	public void setIdOfClock(int idOfClock) {
		this.idOfClock = idOfClock;
	}


	public void setStyle() {
		this.setOrientation(LinearLayout.HORIZONTAL);
		this.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		this.setPadding(0, 0, 0, 10);
		editButton = new ClockEditButton(getContext());
		toggleButton = new ClockToggleButton(getContext());
		this.addView(editButton);
		this.addView(toggleButton);
	}

	public ClockEditButton getEditButton() {
		return editButton;
	}

	public void setEditButton(ClockEditButton editButton) {
		this.editButton = editButton;
	}

	public ClockToggleButton getToggleButton() {
		return toggleButton;
	}

	public void setToggleButton(ClockToggleButton toggleButton) {
		this.toggleButton = toggleButton;
	}

}
