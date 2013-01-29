package com.killerban.okclock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.killerban.settings.SettingsActivity;
import com.killerban.widget.ClockLinearLayout;
import com.killerban.database.DatabaseHelper;
import com.killerban.editclock.EditClockActivity;
import com.killerban.model.ClockParameter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView weekTextView; // 显示今天是星期几的文本框
	private TextView dateTextView; // 显示日期的文本框
	private LinearLayout mainLayout; // 全局layout 包含多个闹钟layout
	private Button settingsButton; // 设置按钮
	private Button addClockButton; // 添加闹钟按钮
	private ArrayList<ClockLinearLayout> layoutList = new ArrayList<ClockLinearLayout>(); // 闹钟layout
	private ArrayList<ClockParameter> clockList = new ArrayList<ClockParameter>(); // 全部闹钟信息
	public final static int REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		settingsButton = (Button) findViewById(R.id.settings);
		addClockButton = (Button) findViewById(R.id.addClock);
		weekTextView = (TextView) findViewById(R.id.week);
		dateTextView = (TextView) findViewById(R.id.date);

		mainLayout = (LinearLayout) findViewById(R.id.clockList);

		settingsButton.setOnClickListener(listener);
		addClockButton.setOnClickListener(listener);

		getDateAndWeek(); // 获取当前星期几和日期，显示在该Activity正上方
		getClockData(); // 获取数据库的闹钟的数据
	}

	// 闹钟详细信息的按钮触发器，点击可修改和查看闹钟
	class EditButtonOnclickedListener implements OnClickListener {
		ClockParameter param= new ClockParameter();

		public EditButtonOnclickedListener(ClockParameter param) {
			this.param = param;
			System.out.println("set clock "+ClockParameter.getFormatTime(param.getCalendar().getTimeInMillis()));
		}

		@Override
		public void onClick(View v) {
			sentIntent(param);
		}
	}

	// 闹钟的开关控制按钮
	class ToggleButtonOnclickedListener implements OnClickListener {
		ClockParameter param=new ClockParameter();

		public ToggleButtonOnclickedListener(ClockParameter param) {
			this.param = param;
		}

		@Override
		public void onClick(View v) {
			changeStatus(param);
		}
	}

	/**
	 * 为新建闹钟和软件设置Button绑定监听器 ，根据Id不同触发不同的事件 其中大多数为调用EditClockActivity
	 * 所以新建了sentIntent()函数统一调用EditClockActivity
	 */
	View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.settings: // 软件设置
				Intent intent = new Intent(MainActivity.this,
						SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.addClock: // 新建闹钟
				ClockParameter param = new ClockParameter();
				param.setIsnew(true); // 设置标志，视为新闹钟
				sentIntent(param);
				break;
			}
		}
	};

	// 更改闹钟的状态，若现在开关为开启状态，则关闭，若为关闭状态，则开启。
	// 每次更改需更新数据库
	void changeStatus(ClockParameter param) {
		if (!param.isIsopen()) {
			setClockOn(param);
			param.setIsopen(true);
		} else {
			setClockOff(param.getId());
			param.setIsopen(false);
		}
		DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
				DatabaseHelper.DATABASE_NAME);
		dbHelper.updateOKColock(param.getId() + "", param);
	}

	// 开启闹钟，设置闹铃时间，若闹钟不重复，则设置单次响铃，否则，设置重复响铃
	void setClockOn(ClockParameter param) {
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		Intent intent = new Intent(MainActivity.this, AlarmRingActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("clock", param);
		intent.putExtras(bundle);
		PendingIntent pi = PendingIntent.getActivity(MainActivity.this,
				param.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
		if (param.noRepeating()) {
			am.set(AlarmManager.RTC_WAKEUP,
					param.getRecentlyAlarmTime(), pi);
		} else {
			am.setRepeating(AlarmManager.RTC_WAKEUP,
					param.getRecentlyAlarmTime(), AlarmManager.INTERVAL_DAY, pi);
		}
		Toast.makeText(this, ClockParameter.getFormatTime((param.getRecentlyAlarmTime()-System.currentTimeMillis())) + "后提醒",
				Toast.LENGTH_SHORT).show();
	}

	// 关闭闹钟
	void setClockOff(int id) {
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		Intent intent = new Intent(MainActivity.this, AlarmRingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(MainActivity.this, id,
				intent, 0);
		am.cancel(pi);
	}

	// 用startActivityForResult调用EditClockActivity---查看或者修改闹钟
	void sentIntent(ClockParameter param) {
		Intent intent = new Intent(MainActivity.this, EditClockActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("clock", param);
		intent.putExtras(bundle);
		startActivityForResult(intent, REQUEST_CODE);
	}

	// 接收从EditClockActivity 返回的参数，为之响应不同事件
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE
				&& resultCode == EditClockActivity.RESULT_CODE) {

			ClockParameter param = (ClockParameter) data
					.getSerializableExtra("clock");
			DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
					DatabaseHelper.DATABASE_NAME);

			// 若返回的是删除闹钟的操作则 删除对应的Button
			// 否则 判断返回的闹钟是否为新建闹钟 ：若是，则插入到数据库 ；不是，则更新其在数据库的信息(因为有可能有修改)
			if (!data.getExtras().getBoolean("save")) {
				// 删除对应的闹钟控件
				modifyClockButton(param, "delete");
			} else {
				if (param.isIsnew()) {
					param.setIsnew(false); // 更改状态，插入到数据库的闹钟不再是新闹钟
					param.setIsopen(true);
					dbHelper.insertOKColock(param);

					// 为新闹钟绑定Button 插入到layoutList中，并提示距离响铃还有长时间
					clockList.add(param);
					initWidget(param);

				} else {
					param.setIsopen(true);
					dbHelper.updateOKColock(param.getId() + "", param);
					// 更新闹钟Button绑定的数据
					modifyClockButton(param, "update");
				}
				
				setClockOn(param);
			}
		}
	}

	// 用于修改闹钟Button的信息 包括删除和更新
	void modifyClockButton(ClockParameter param, String operate) {
		int index = -1;
		// 找出需要修改的闹钟Button所在layoutList的位置
		for (int i = 0; i < layoutList.size(); i++) {
			if (layoutList.get(i).getIdOfClock() == param.getId()) {
				index = i;
				break;
			}
		}
		// operate 为delete 进行删除操作 先取消之前的闹铃再删除！
		if (operate.equals("delete")) {
			AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
			Intent intent = new Intent(MainActivity.this, AlarmRingActivity.class);
			PendingIntent pi = PendingIntent.getActivity(MainActivity.this, layoutList.get(index).getIdOfClock(),
					intent, 0);
			am.cancel(pi);
			mainLayout.removeView(layoutList.get(index));
			layoutList.remove(index);
			clockList.remove(index);
		}
		// operate 为 update 进行更新操作
		if (operate.equals("update")) {
			System.out.println(param.isIsopen() + " Open ");
			clockList.set(index, param);
			// 更新显示内容
			setButtonText(param, layoutList.get(index));
			// 更新绑定clock的Button的监听器信息
			layoutList.get(index).getEditButton()
					.setOnClickListener(new EditButtonOnclickedListener(param));
			layoutList
					.get(index)
					.getToggleButton()
					.setOnClickListener(
							new ToggleButtonOnclickedListener(param));
		}
	}

	// 为Button设置文本内容
	void setButtonText(ClockParameter param, ClockLinearLayout widget) {
		String info = param.getHour() + ":";
		String minute = param.getMinute() + "";
		if (minute.length() == 1)
			info += "0";
		info += minute;
		info += ("   " + param.getName() + "\n");
		info += param.getRepeatInfo(param.getRepeat());
		widget.getEditButton().setText(info);
		widget.getToggleButton().setChecked(param.isIsopen());
	}

	// 从数据库中获取各个闹钟的简单状态信息 并在Button中显示
	void getClockData() {
		DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
				DatabaseHelper.DATABASE_NAME);
		ClockParameter param;
		Cursor cursor = dbHelper.selectOKColock();

		// 将查询结果一一对于地赋值到 ClockParamter 类的属性当中
		while (cursor.moveToNext()) {
			param = new ClockParameter();
			param.translateFromDatabase(cursor);
			clockList.add(param); // 添加到List

			System.out.println(ClockParameter.getFormatTime((param.getRecentlyAlarmTime()-System.currentTimeMillis())));
			
			// 初始化闹钟按钮
			initWidget(param);
		}
		cursor.close();
		dbHelper.close();
	}

	// 初始化闹钟按钮，将闹钟信息绑定到button中，并为button设置监听器
	void initWidget(ClockParameter param) {
		ClockLinearLayout layout = new ClockLinearLayout(this);

		// button上显示闹钟的简略信息
		setButtonText(param, layout);
		// 为layout绑定id,id与闹钟id一致，用于分辨相应操所对应的闹钟
		layout.setIdOfClock(param.getId());
		layout.getEditButton().setOnClickListener(
				new EditButtonOnclickedListener(param));
		layout.getToggleButton().setOnClickListener(
				new ToggleButtonOnclickedListener(param));
		layoutList.add(layout);
		mainLayout.addView(layout);
	}

	// 获取当前是星期几 以及几月几号 为weekTextView 和dateTextView写入信息
	void getDateAndWeek() {
		String[] week = { "日 ", "一 ", "二 ", "三 ", "四 ", "五 ", "六 " };
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(System.currentTimeMillis()));
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		for (int i = 0; i <= 6; i++)
			if (dayOfWeek == i+1) {
				weekTextView.setText("星期" + week[i]);
			}
		dateTextView
				.setText((c.get(Calendar.YEAR) + "年"
						+ (c.get(Calendar.MONTH) + 1) + "月"
						+ c.get(Calendar.DATE) + "日"));// 获取年月日
	}
}
