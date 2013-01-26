package com.killerban.okclock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.killerban.database.DatabaseHelper;
import com.killerban.model.ClockParameter;

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
		settingsButton = (Button) findViewById(R.id.settings);
		addClockButton = (Button) findViewById(R.id.addClock);
		weekTextView = (TextView) findViewById(R.id.week);
		dateTextView = (TextView) findViewById(R.id.date);

		
		settingsButton.setOnClickListener(listener);
		addClockButton.setOnClickListener(listener);

		getDateAndWeek(); // ��ȡ��ǰ���ڼ�������
		getClockData(); // ��ȡ���ݿ�����ӵ�����
	}

	/**
	 * ΪButton�󶨼����� ������Id��ͬ������ͬ���¼� ���д����Ϊ����EditClockActivity
	 * �����½���sentIntent()����ͳһ����
	 */

	View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			intent = new Intent();
			Bundle bundle = new Bundle();
			switch (v.getId()) {
			
			case R.id.settings: // ����
				intent.setClass(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.addClock: // �½�����
				ClockParameter param = new ClockParameter();
				param.setIsnew(true); // ���ñ�־
				bundle.putSerializable("clock", param);
				intent.putExtras(bundle);
				sentIntent(intent);
				break;
			}
		}
	};

	void init() {
		// ��ȡ���ݿ�����

	}

	void linearLayoutFormat(LinearLayout linearLayout){
		
	}
	
	void buttonEditFormat(Button btClockEdit) {
		btClockEdit.setBackgroundResource(R.drawable.bt_clock_edit);                             // ���ñ���
		btClockEdit.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT,
				1.0f));                                                                          // ���ó���������
		btClockEdit.setShadowLayer(5, 0, 0, getResources().getColor(R.color.black));             // ������Ӱ
		btClockEdit.setTextColor(getResources().getColor(R.color.white));                        // ����������ɫ
		btClockEdit.setTypeface(Typeface.DEFAULT_BOLD);                                          // ��������
	}

	void buttonSwitchFormat(ToggleButton btClockToggle) {
		btClockToggle.setBackgroundResource(R.drawable.bt_toggle_bg);                            // ���ñ���
		btClockToggle.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));                                                     // ���ó���������
		btClockToggle.setTextOff(null);                                                          // ���ÿ��عر�״̬����Ϊ��
		btClockToggle.setTextOn(null);                                                           // ���ÿ��ع�����̬����Ϊ��
		btClockToggle.setShadowLayer(5, 0, 0, getResources().getColor(R.color.black));           // ������Ӱ
		btClockToggle.setTextColor(getResources().getColor(R.color.white));                      // ����������ɫ
		btClockToggle.setTypeface(Typeface.DEFAULT_BOLD);                                        // ��������
	}

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
		dbHelper.updateOKColock(param.getId() + "", param);
	}

	// Test
	void setClockOn(ClockParameter param) {
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		Intent intent = new Intent(MainActivity.this, AlarmRingActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("clock", param);
		intent.putExtras(bundle);
		PendingIntent pi = PendingIntent.getActivity(MainActivity.this, param.getId(), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pi);

	}

	// Test
	void setClockOff(int id) {
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		Intent intent = new Intent(MainActivity.this, AlarmRingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(MainActivity.this, id, intent, 0);
		am.cancel(pi);
	}

	void sentIntent(Intent intent) { // ����EditClockActivity
		intent.setClass(MainActivity.this, EditClockActivity.class);
		startActivityForResult(intent, REQUEST_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// ��EditClockActivity ���صĲ���
		if (requestCode == REQUEST_CODE && resultCode == EditClockActivity.RESULT_CODE) {
			ClockParameter param = (ClockParameter) data.getSerializableExtra("clock");
			DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
					DatabaseHelper.DATABASE_NAME);

			// �����ص���ɾ�����ӵ�״̬�� ɾ����Ӧ��Button
			// ���� �жϷ��ص������Ƿ�Ϊ�½����� ������뵽���ݿ� ������������������ݿ����Ϣ(��Ϊ�п������޸�)
			if (!data.getExtras().getBoolean("save")) {
				System.out.println("�Ѿ�ɾ������");
				getClockData();
			} else {
				if (param.isIsnew()) {
					param.setIsnew(false); // ����״̬
					dbHelper.insertOKColock(param);
				} else {
					dbHelper.updateOKColock(param.getId() + "", param);
				}

				String info = param.getHour() + ":";
				String minute = param.getMinute() + "";
				if (minute.length() == 1)
					info += "0";
				info += minute;
				info += ("   " + param.getName() + "\n");
				info += getRepeatInfo(param.getRepeat());
				if (param.getId() == 1)
					editColckButton.setText(info);
				else
					editColckButton2.setText(info);
			}
		}
	}

	// �����ݿ��л�ȡ�������ӵļ�״̬��Ϣ ����Button����ʾ
	void getClockData() {
		DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
				DatabaseHelper.DATABASE_NAME);
		ClockParameter param;
		Cursor cursor = dbHelper.selectOKColock();
		boolean[] repeat = new boolean[7];
		int index = 1;
		while (cursor.moveToNext() && index <= 2) {
			param = new ClockParameter();
			param.setHour(Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.HOUR))));
			param.setMinute(Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.MINUTE))));
			param.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ID))));
			param.setLevel(Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(DatabaseHelper.LEVEL))));
			param.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME)));
			param.setIsvabrate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ISVIBRATE))
					.equals("1"));
			param.setIsopen(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ISOPEN)).equals(
					"1"));
			param.setIsnew(cursor.getString(cursor.getColumnIndex(DatabaseHelper.ISNEW))
					.equals("1"));
			for (int i = 0; i <= 6; i++) {
				/*
				 * repeat[i] = Boolean.parseBoolean(cursor.getString(cursor
				 * .getColumnIndex((DatabaseHelper.REPEAT + i)))); �ô˷�����Ч ��ֻ��01ֵ
				 */
				if ((cursor.getString(cursor.getColumnIndex((DatabaseHelper.REPEAT + i))))
						.equals("1")) {
					repeat[i] = true;
				} else
					repeat[i] = false;
			}
			param.setRepeat(repeat);
			list.add(param);

			// ��ʼ�����ӵ�״̬��Ϣ
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
			if (index == 1) {
				editColckButton.setText(s);
				System.out.println(param.isIsopen());
				clockSwitchButton.setChecked(param.isIsopen());
			} else {
				editColckButton2.setText(s);
				System.out.println(param.isIsopen());
				clockSwitchButton2.setChecked(param.isIsopen());

			}
			index++;
		}
		cursor.close();
		dbHelper.close();
	}

	// ��ȡ��ǰ�����ڼ� �Լ����¼��� ΪweekTextView ��dateTextViewд����Ϣ
	void getDateAndWeek() {
		String[] week = { "�� ", "һ ", "�� ", "�� ", "�� ", "�� ", "�� " };
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(System.currentTimeMillis()));
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		for (int i = 0; i <= 6; i++)
			if (dayOfWeek == i) {
				weekTextView.setText("����" + week[i]);
			}
		dateTextView.setText((c.get(Calendar.YEAR) + "��" + (c.get(Calendar.MONTH) + 1) + "��"
				+ c.get(Calendar.DATE) + "��"));// ��ȡ������

	}

	static String getRepeatInfo(boolean[] repeat) {
		String info = "��";
		String[] week = { "�� ", "һ ", "�� ", "�� ", "�� ", "�� ", "�� " };
		if (repeat[0] && repeat[6] && !repeat[1] && !repeat[2] && !repeat[3] && !repeat[4]
				&& !repeat[5]) {
			return "��ĩ";
		}
		if (!repeat[0] && !repeat[6] && repeat[1] && repeat[2] && repeat[3] && repeat[4]
				&& repeat[5]) {
			return "��һ����";
		}
		if (repeat[0] && repeat[6] && repeat[1] && repeat[2] && repeat[3] && repeat[4] && repeat[5]) {
			return "ÿ������";
		}
		if (!repeat[0] && !repeat[6] && !repeat[1] && !repeat[2] && !repeat[3] && !repeat[4]
				&& !repeat[5]) {
			return "ֻ��һ��";
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
