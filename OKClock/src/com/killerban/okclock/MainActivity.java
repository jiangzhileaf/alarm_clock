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

	private TextView weekTextView; // ��ʾ���������ڼ����ı���
	private TextView dateTextView; // ��ʾ���ڵ��ı���
	private LinearLayout mainLayout; // ȫ��layout �����������layout
	private Button settingsButton; // ���ð�ť
	private Button addClockButton; // ������Ӱ�ť
	private ArrayList<ClockLinearLayout> layoutList = new ArrayList<ClockLinearLayout>(); // ����layout
	private ArrayList<ClockParameter> clockList = new ArrayList<ClockParameter>(); // ȫ��������Ϣ
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

		getDateAndWeek(); // ��ȡ��ǰ���ڼ������ڣ���ʾ�ڸ�Activity���Ϸ�
		getClockData(); // ��ȡ���ݿ�����ӵ�����
	}

	// ������ϸ��Ϣ�İ�ť��������������޸ĺͲ鿴����
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

	// ���ӵĿ��ؿ��ư�ť
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
	 * Ϊ�½����Ӻ��������Button�󶨼����� ������Id��ͬ������ͬ���¼� ���д����Ϊ����EditClockActivity
	 * �����½���sentIntent()����ͳһ����EditClockActivity
	 */
	View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.settings: // �������
				Intent intent = new Intent(MainActivity.this,
						SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.addClock: // �½�����
				ClockParameter param = new ClockParameter();
				param.setIsnew(true); // ���ñ�־����Ϊ������
				sentIntent(param);
				break;
			}
		}
	};

	// �������ӵ�״̬�������ڿ���Ϊ����״̬����رգ���Ϊ�ر�״̬��������
	// ÿ�θ�����������ݿ�
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

	// �������ӣ���������ʱ�䣬�����Ӳ��ظ��������õ������壬���������ظ�����
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
		Toast.makeText(this, ClockParameter.getFormatTime((param.getRecentlyAlarmTime()-System.currentTimeMillis())) + "������",
				Toast.LENGTH_SHORT).show();
	}

	// �ر�����
	void setClockOff(int id) {
		AlarmManager am = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
		Intent intent = new Intent(MainActivity.this, AlarmRingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(MainActivity.this, id,
				intent, 0);
		am.cancel(pi);
	}

	// ��startActivityForResult����EditClockActivity---�鿴�����޸�����
	void sentIntent(ClockParameter param) {
		Intent intent = new Intent(MainActivity.this, EditClockActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("clock", param);
		intent.putExtras(bundle);
		startActivityForResult(intent, REQUEST_CODE);
	}

	// ���մ�EditClockActivity ���صĲ�����Ϊ֮��Ӧ��ͬ�¼�
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE
				&& resultCode == EditClockActivity.RESULT_CODE) {

			ClockParameter param = (ClockParameter) data
					.getSerializableExtra("clock");
			DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
					DatabaseHelper.DATABASE_NAME);

			// �����ص���ɾ�����ӵĲ����� ɾ����Ӧ��Button
			// ���� �жϷ��ص������Ƿ�Ϊ�½����� �����ǣ�����뵽���ݿ� �����ǣ�������������ݿ����Ϣ(��Ϊ�п������޸�)
			if (!data.getExtras().getBoolean("save")) {
				// ɾ����Ӧ�����ӿؼ�
				modifyClockButton(param, "delete");
			} else {
				if (param.isIsnew()) {
					param.setIsnew(false); // ����״̬�����뵽���ݿ�����Ӳ�����������
					param.setIsopen(true);
					dbHelper.insertOKColock(param);

					// Ϊ�����Ӱ�Button ���뵽layoutList�У�����ʾ�������廹�г�ʱ��
					clockList.add(param);
					initWidget(param);

				} else {
					param.setIsopen(true);
					dbHelper.updateOKColock(param.getId() + "", param);
					// ��������Button�󶨵�����
					modifyClockButton(param, "update");
				}
				
				setClockOn(param);
			}
		}
	}

	// �����޸�����Button����Ϣ ����ɾ���͸���
	void modifyClockButton(ClockParameter param, String operate) {
		int index = -1;
		// �ҳ���Ҫ�޸ĵ�����Button����layoutList��λ��
		for (int i = 0; i < layoutList.size(); i++) {
			if (layoutList.get(i).getIdOfClock() == param.getId()) {
				index = i;
				break;
			}
		}
		// operate Ϊdelete ����ɾ������ ��ȡ��֮ǰ��������ɾ����
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
		// operate Ϊ update ���и��²���
		if (operate.equals("update")) {
			System.out.println(param.isIsopen() + " Open ");
			clockList.set(index, param);
			// ������ʾ����
			setButtonText(param, layoutList.get(index));
			// ���°�clock��Button�ļ�������Ϣ
			layoutList.get(index).getEditButton()
					.setOnClickListener(new EditButtonOnclickedListener(param));
			layoutList
					.get(index)
					.getToggleButton()
					.setOnClickListener(
							new ToggleButtonOnclickedListener(param));
		}
	}

	// ΪButton�����ı�����
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

	// �����ݿ��л�ȡ�������ӵļ�״̬��Ϣ ����Button����ʾ
	void getClockData() {
		DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this,
				DatabaseHelper.DATABASE_NAME);
		ClockParameter param;
		Cursor cursor = dbHelper.selectOKColock();

		// ����ѯ���һһ���ڵظ�ֵ�� ClockParamter ������Ե���
		while (cursor.moveToNext()) {
			param = new ClockParameter();
			param.translateFromDatabase(cursor);
			clockList.add(param); // ��ӵ�List

			System.out.println(ClockParameter.getFormatTime((param.getRecentlyAlarmTime()-System.currentTimeMillis())));
			
			// ��ʼ�����Ӱ�ť
			initWidget(param);
		}
		cursor.close();
		dbHelper.close();
	}

	// ��ʼ�����Ӱ�ť����������Ϣ�󶨵�button�У���Ϊbutton���ü�����
	void initWidget(ClockParameter param) {
		ClockLinearLayout layout = new ClockLinearLayout(this);

		// button����ʾ���ӵļ�����Ϣ
		setButtonText(param, layout);
		// Ϊlayout��id,id������idһ�£����ڷֱ���Ӧ������Ӧ������
		layout.setIdOfClock(param.getId());
		layout.getEditButton().setOnClickListener(
				new EditButtonOnclickedListener(param));
		layout.getToggleButton().setOnClickListener(
				new ToggleButtonOnclickedListener(param));
		layoutList.add(layout);
		mainLayout.addView(layout);
	}

	// ��ȡ��ǰ�����ڼ� �Լ����¼��� ΪweekTextView ��dateTextViewд����Ϣ
	void getDateAndWeek() {
		String[] week = { "�� ", "һ ", "�� ", "�� ", "�� ", "�� ", "�� " };
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(System.currentTimeMillis()));
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		for (int i = 0; i <= 6; i++)
			if (dayOfWeek == i+1) {
				weekTextView.setText("����" + week[i]);
			}
		dateTextView
				.setText((c.get(Calendar.YEAR) + "��"
						+ (c.get(Calendar.MONTH) + 1) + "��"
						+ c.get(Calendar.DATE) + "��"));// ��ȡ������
	}
}
