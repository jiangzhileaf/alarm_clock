package com.killerban.okclock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.killerban.database.DatabaseHelper;
import com.killerban.model.ClockParameter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	private TextView weekTextView;
	private TextView dateTextView;
	private Button editColckButton;
	private Button editColckButton2;
	private ToggleButton clockSwitchButton;
	private ToggleButton clockSwitchButton2;
	private Button settingsButton;
	private Intent intent;
	private Button addClockButton;
	private ArrayList<ClockParameter> list = new ArrayList<ClockParameter>();
	private ArrayList<Button> buttonList = new ArrayList<Button>();
	public final static int REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editColckButton = (Button) findViewById(R.id.clockEdit);
		editColckButton2 = (Button) findViewById(R.id.clockEdit2);
		clockSwitchButton = (ToggleButton) findViewById(R.id.clockSwitch);
		clockSwitchButton2 = (ToggleButton) findViewById(R.id.clockSwitch2);
		settingsButton = (Button) findViewById(R.id.settings);
		addClockButton = (Button) findViewById(R.id.addClock);
		weekTextView = (TextView) findViewById(R.id.week);
		dateTextView = (TextView) findViewById(R.id.date);
		
		editColckButton.setOnClickListener(listener);
		editColckButton2.setOnClickListener(listener);
		clockSwitchButton.setOnClickListener(listener);
		clockSwitchButton2.setOnClickListener(listener);
		settingsButton.setOnClickListener(listener);
		addClockButton.setOnClickListener(listener);
		
		getDateAndWeek(); // 获取当前星期几和日期
		getClockData(); // 获取数据库的闹钟的数据
	}

	/**
	 * 为Button绑定监听器 ，根据Id不同触发不同的事件 其中大多数为调用EditClockActivity
	 * 所以新建了sentIntent()函数统一调用
	 */
	View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			intent = new Intent();
			Bundle bundle = new Bundle();
			switch (v.getId()) {
			case R.id.clockEdit:
				bundle.putSerializable("clock", list.get(0));
				intent.putExtras(bundle);
				sentIntent(intent);
				break;
			case R.id.clockEdit2:
				bundle.putSerializable("clock", list.get(1));
				intent.putExtras(bundle);
				sentIntent(intent);
				break;
			case R.id.clockSwitch: // 启动或者关闭闹钟
				changeStatus(list.get(0));
				break;
			case R.id.clockSwitch2:
				changeStatus(list.get(1));
				break;
			case R.id.settings: // 闹铃
				intent.setClass(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.addClock: // 新建闹钟
				ClockParameter param = new ClockParameter();
				param.setIsnew(true); // 设置标志
				bundle.putSerializable("clock", param);
				intent.putExtras(bundle);
				sentIntent(intent);
				break;
			}
		}
	};

	// Test
	void changeStatus(ClockParameter param) {
		if (!param.isIsopen()) {
			setClockOn(param);
			param.setIsopen(true);
			System.out.println(param.getId() + " On");
		} else {
			setClockOff(param.getId());
			param.setIsopen(false);
			System.out.println(param.getId() + " Off");
		}
		DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
				DatabaseHelper.DATABASE_NAME);
		dbHelper.updateOKColock(param.getId()+"", param);
	}

	// Test
	void setClockOn(ClockParameter param) {
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		Intent intent = new Intent(MainActivity.this, AlarmRingActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("clock", param);
		intent.putExtras(bundle);
		PendingIntent pi = PendingIntent.getActivity(MainActivity.this,
				param.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP,
				System.currentTimeMillis() + 1000, pi);

	}

	// Test
	void setClockOff(int id) {
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		Intent intent = new Intent(MainActivity.this, AlarmRingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(MainActivity.this, id,
				intent, 0);
		am.cancel(pi);
	}

	void sentIntent(Intent intent) { // 调用EditClockActivity
		intent.setClass(MainActivity.this, EditClockActivity.class);
		startActivityForResult(intent, REQUEST_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 从EditClockActivity 返回的参数
		if (requestCode == REQUEST_CODE
				&& resultCode == EditClockActivity.RESULT_CODE) {
			ClockParameter param = (ClockParameter) data
					.getSerializableExtra("clock");
			DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
					DatabaseHelper.DATABASE_NAME);
			if (param.isIsnew())
			{
				param.setIsnew(false);		//更改状态
				dbHelper.insertOKColock(param);
			}
			else
				dbHelper.updateOKColock(param.getId() + "", param);
			String info = param.getHour() + ":";
			String minute = param.getMinute() + "";
			if (minute.length() == 1)
				info += "0";
			info += minute;
			info += ("   " + param.getName() + "\n");
			info += getRepeatInfo(param.getRepeat());
			if (param.getId() == 11)
				editColckButton.setText(info);
			else
				editColckButton2.setText(info);
		}
	}

	// 从数据库中获取各个闹钟的简单状态信息 并在Button中显示
	void getClockData() {
		DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
				DatabaseHelper.DATABASE_NAME);
		ClockParameter param;
		Cursor cursor = dbHelper.selectOKColock();
		boolean[] repeat = new boolean[7];
		int index=1;
		while (cursor.moveToNext()&&index<=2) {
			param = new ClockParameter();
			param.setHour(Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.HOUR))));
			param.setMinute(Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.MINUTE))));
			param.setId(Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.ID))));
			param.setLevel(Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.LEVEL))));
			param.setName(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.NAME)));
			param.setIsvabrate(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.ISVIBRATE)).equals("1"));
			param.setIsopen(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.ISOPEN)).equals("1"));
			param.setIsnew(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.ISNEW)).equals("1"));
			for (int i = 0; i <= 6; i++) {
				/*
				 * repeat[i] = Boolean.parseBoolean(cursor.getString(cursor
				 * .getColumnIndex((DatabaseHelper.REPEAT + i)))); 用此方法无效 其只有01值
				 */
				if ((cursor.getString(cursor
						.getColumnIndex((DatabaseHelper.REPEAT + i))))
						.equals("1")) {
					repeat[i] = true;
				} else
					repeat[i] = false;
			}
			param.setRepeat(repeat);
			list.add(param);

			// 初始化闹钟的状态信息
			String s = "";
			String m = "";
			s += param.getHour() + ":";
			m = param.getMinute() + "";
			if (m.length() == 1) {
				s += "0";
			}
			s += m;
			s += ("   " + param.getName() + "\n");
			s += getRepeatInfo(repeat);
			if (index== 1)
			{
				editColckButton.setText(s);
				System.out.println(param.isIsopen());
				clockSwitchButton.setChecked(param.isIsopen());
			}
			else
			{	
				editColckButton2.setText(s);
				System.out.println(param.isIsopen());
				clockSwitchButton.setChecked(param.isIsopen());
			
			}
			index++;
		}
		cursor.close();
		dbHelper.close();
	}

	// 获取当前是星期几 以及几月几号 为weekTextView 和dateTextView写入信息
	void getDateAndWeek() {
		String[] week = { "日 ", "一 ", "二 ", "三 ", "四 ", "五 ", "六 " };
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(System.currentTimeMillis()));
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		for (int i = 0; i <= 6; i++)
			if (dayOfWeek == i) {
				weekTextView.setText("星期" + week[i]);
			}
		dateTextView
				.setText((c.get(Calendar.YEAR) + "年"
						+ (c.get(Calendar.MONTH) + 1) + "月"
						+ c.get(Calendar.DATE) + "日"));// 获取年月日

	}

	static String getRepeatInfo(boolean[] repeat) {
		String info = "周";
		String[] week = { "日 ", "一 ", "二 ", "三 ", "四 ", "五 ", "六 " };
		if (repeat[0] && repeat[6] && !repeat[1] && !repeat[2] && !repeat[3]
				&& !repeat[4] && !repeat[5]) {
			return "周末";
		}
		if (!repeat[0] && !repeat[6] && repeat[1] && repeat[2] && repeat[3]
				&& repeat[4] && repeat[5]) {
			return "周一到五";
		}
		if (repeat[0] && repeat[6] && repeat[1] && repeat[2] && repeat[3]
				&& repeat[4] && repeat[5]) {
			return "每天响铃";
		}
		if (!repeat[0] && !repeat[6] && !repeat[1] && !repeat[2] && !repeat[3]
				&& !repeat[4] && !repeat[5]) {
			return "只响一次";
		}
		for (int i = 0; i <= 6; i++) {
			if (repeat[i])
				info += week[i];
		}
		return info;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
