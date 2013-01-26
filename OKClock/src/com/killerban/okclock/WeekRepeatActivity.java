package com.killerban.okclock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * 选择闹铃日子 分别分周一至周日 用多选框显示，选择完后按确认设定，否则修改无效 该Activity被EditActivity调用，并返回
 * boolean[] ---响铃日选择结果 给EditActivity
 */
public class WeekRepeatActivity extends Activity {
	private CheckBox mondayBox;
	private CheckBox tuesdayBox;
	private CheckBox wednesdayBox;
	private CheckBox thursdayBox;
	private CheckBox fridayBox;
	private CheckBox saturdayBox;
	private CheckBox sundayBox;
	private Button okButton;
	final static int RESULT_CODE = 1;
	private boolean[] select = new boolean[7];

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weekrepeat);
		mondayBox = (CheckBox) findViewById(R.id.monday);
		tuesdayBox = (CheckBox) findViewById(R.id.tuesday);
		wednesdayBox = (CheckBox) findViewById(R.id.wednesday);
		thursdayBox = (CheckBox) findViewById(R.id.thursday);
		fridayBox = (CheckBox) findViewById(R.id.friday);
		saturdayBox = (CheckBox) findViewById(R.id.saturday);
		sundayBox = (CheckBox) findViewById(R.id.sunday);
		okButton = (Button) findViewById(R.id.ok);

		Intent intent = getIntent();
		select = intent.getBooleanArrayExtra("repeat");
		if(select[0])
			sundayBox.setChecked(true);
		if(select[1])
			mondayBox.setChecked(true);
		if(select[2])
			tuesdayBox.setChecked(true);
		if(select[3])
			wednesdayBox.setChecked(true);
		if(select[4])
			thursdayBox.setChecked(true);
		if(select[5])
			fridayBox.setChecked(true);
		if(select[6])
			saturdayBox.setChecked(true);
		
		mondayBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							select[1] = true;
						} else {
							select[1] = false;
						}
					}
				});
		tuesdayBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							select[2] = true;
						} else {
							select[2] = false;
						}
					}
				});
		wednesdayBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							select[3] = true;
						} else {
							select[3] = false;
						}
					}
				});
		thursdayBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							select[4] = true;
						} else {
							select[4] = false;
						}
					}
				});
		fridayBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							select[5] = true;
						} else {
							select[5] = false;
						}
					}
				});
		saturdayBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							select[6] = true;
						} else {
							select[6] = false;
						}
					}
				});
		sundayBox
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							select[0] = true;
						} else {
							select[0] = false;
						}
					}
				});
		okButton.setOnClickListener(new OKButtonListener());
	};

	class OKButtonListener implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.putExtra("repeat", select);
			setResult(RESULT_CODE, intent);
			finish();
		}
	}
}
