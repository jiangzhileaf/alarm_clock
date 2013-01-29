package com.killerban.okclock;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.killerban.database.DatabaseHelper;
import com.killerban.database.GetUpDatabaseHelper;
import com.killerban.model.ClockParameter;
import com.killerban.model.GetUpInfo;

@SuppressLint("ShowToast")
public class AlarmRingActivity extends Activity {

	private Button getupButton; // �𴲰�ť
	private Button napButton; // ������ť
	public MediaPlayer mediaPlayer; // ������������
	private Vibrator vibrator; // ��
	private PowerManager.WakeLock wl; // ������Ļ����
	private KeyguardLock kl; // ��������Ļ
	private KeyguardManager km; // ��Ļ���Ĺ�����
	private long allTime; // ���ܹ����ѵ�ʱ��
	private ClockParameter param; // ���������Ϣ�Ķ���
	public int answer; // ��������

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// ���жϽ��������ڼ����Ƿ���������ڣ������ǣ�������
		// ����ֻ����һ�ε�������һ�����ն������������ڣ�������Ȼ������
		param = (ClockParameter) getIntent().getExtras().getSerializable(
				"clock");
		System.out.println(ClockParameter.getFormatTime(param.getCalendar()
				.getTimeInMillis()));
		System.out.println(ClockParameter.getFormatTime(System
				.currentTimeMillis()));
		if (!param.noRepeating()) {
			if (!param.getRepeat()[getWeek() - 1]) {
				finish();
				onDestroy();
				return;
			}
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_ring);
		getupButton = (Button) findViewById(R.id.getUp);
		napButton = (Button) findViewById(R.id.nap);
		getupButton.setOnClickListener(listener);
		napButton.setOnClickListener(listener);

		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		unlockScreen(); // ������Ļ ������Ļ ��������
		startVibrate(vibrator); // ��ʼ��
		playMusic(); // ��ʼ����
	}

	int getWeek() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(System.currentTimeMillis()));
		return c.get(Calendar.DAY_OF_WEEK);
	}

	View.OnClickListener listener = new View.OnClickListener() { // ��������
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.getUp:
				showQuestionDialog(R.id.getUp); // �𴲰�ť������������
				break;
			case R.id.nap:
				showQuestionDialog(R.id.nap); // ������ť��������������
				break;
			}
		}
	};

	void showQuestionDialog(int id) { // �����ѶȺ��������İ�ť��id������
		String question;
		if (param.getLevel() == 1 && id == R.id.getUp) // �ȼ�Ϊ1����id����Ϊ�𴲰�ť���� �ӷ�
		{
			int a = (int) (Math.random() * 100) + 1;
			int b = (int) (Math.random() * 100) + 1;
			question = ("   " + a + "+" + b + "=     ");
			answer = a + b;
		} else { // �˷�
			int a = (int) (Math.random() * 30) + 1;
			int b = (int) (Math.random() * 30) + 1;
			question = ("   " + a + "*" + b + "=     ");
			answer = a * b;
		}
		showDialog(question, id); // ���������
	}

	boolean isInteger(String value) { // �ж��ַ����Ƿ�Ϊ����
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * ���ݲ���question����Dialog�����ݣ� ���ȷ�Ϻ󣬻�ȡ���������� һ�������Ϊ�ա��������� ��������
	 * ������ʾ���������Ⲣ�����µ�Dialog �����������ȷ�����ݲ��� id �ж� �����idΪR.id.getup���ôε������𴲰�ť����
	 * ��ֹͣ���壬�ر��𶯣���ʾ��ʱ��� ��� id Ϊ R.id.nap ���ôε�����������ť���� ��ʱֹͣ���� ����������Ӻ��ٴ�����
	 */
	void showDialog(String question, final int id) {
		final EditText editText = new EditText(this);

		AlertDialog.Builder builder = new Builder(AlarmRingActivity.this);
		builder.setMessage("��������:   " + question);
		builder.setTitle("��Դ𰸾Ϳ�����~");
		builder.setView(editText);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String userAnswer = editText.getText().toString();
				if (userAnswer == null || userAnswer.isEmpty()) { // ���������Ϊ��,������ʾ
					Toast.makeText(AlarmRingActivity.this, "���ܽ��׾�~����~~",
							Toast.LENGTH_SHORT).show();
					showQuestionDialog(id); // ���³���
				} else if (!isInteger(userAnswer)) // �������ݲ������� ,������ʾ
				{
					Toast.makeText(AlarmRingActivity.this, "��ֻ������������~����~~",
							Toast.LENGTH_SHORT).show();
					showQuestionDialog(id); // ���³���
				} else {
					if (Integer.parseInt(editText.getText().toString()) == answer) { // ����ȷ
						if (id == R.id.getUp) {
							// timeCalculate ����ʱ��ĺ��� ��ʾ������ʱ��
							Toast.makeText(AlarmRingActivity.this,
									timeCalculate(), Toast.LENGTH_LONG).show();
							writeGetUpInfo(); // ����ʱ��д�����ݿ�
						} else {
							Toast.makeText(AlarmRingActivity.this,
									"�ðɣ�������ٶ�˯�����~~", Toast.LENGTH_SHORT)
									.show(); // ̰˯���ã�5���Ӻ���������
							Intent intent = new Intent(AlarmRingActivity.this,
									AlarmRingActivity.class);
							Bundle bundle = new Bundle();
							bundle.putSerializable("clock", param);
							intent.putExtras(bundle);
							// PendingIntent.FLAG_UPDATE_CURRENT���������õĻ� �൱���޸���
							PendingIntent pi = PendingIntent.getActivity(
									AlarmRingActivity.this, 0, intent,
									PendingIntent.FLAG_UPDATE_CURRENT);
							AlarmManager manager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
							manager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + 1000 * 2, pi);
							finish();
						}
						dialog.dismiss();
						stopAlarm(); // ֹͣ����
					} else { // �𰸴���
						Toast.makeText(AlarmRingActivity.this, "   �����~����~~",
								2000).show();
						dialog.dismiss();
						showQuestionDialog(id); // ���³���
					}
				}
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.create().show();
	}

	// ������ʱ��ʱ��
	String timeCalculate() {
		Calendar c = param.getCalendar(); // ȡ������ʱ��
		allTime = System.currentTimeMillis() - c.getTimeInMillis(); // ��ʱ���������ʱ��-����ʱ��
		// ����allTime�ĸ�ʽ���ַ���
		return param.getFormatTime(allTime);
	}

	// ���ֻ���Ļ����������Ļ
	void unlockScreen() {
		km = (KeyguardManager) getSystemService(Service.KEYGUARD_SERVICE);
		kl = km.newKeyguardLock("unLock");
		kl.disableKeyguard();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);// ��ȡ��Դ����������
		wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.FULL_WAKE_LOCK, "bright");
		// ��ȡPowerManager.WakeLock���󣬺���Ĳ���|��ʾͬʱ��������ֵ��������LogCat���õ�Tag
		wl.acquire();// ������Ļ
	}

	// ������
	void startVibrate(Vibrator vibrator) {
		long[] pattern = new long[] { 300, 1000, 1500, 2000 };
		vibrator.vibrate(pattern, 2);
	}

	// ������������
	private void playMusic() {
		if (param.getAudiotype().equals("default")) {
			mediaPlayer = MediaPlayer.create(this, R.raw.drifting);
		} else {
			System.out.println("other ring" + param.getAudiotype());
			mediaPlayer = new MediaPlayer();
			try {
				mediaPlayer.setDataSource(param.getAudiotype());
				mediaPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}

	// �ر��������壬 ��¼����Ϣ�����ݿ�
	void stopAlarm() {
		// ����ô�����Ϊ�������� ��ֻ����һ�� û���ظ����������״̬Ϊ�ر�
		if (param.noRepeating()) {
			param.setIsopen(false);
			DatabaseHelper db = new DatabaseHelper(this,
					DatabaseHelper.DATABASE_NAME);
			db.updateOKColock(param.getId() + "", param);
		}

		if (mediaPlayer.isPlaying()||mediaPlayer!=null)
		{
			mediaPlayer.stop(); // ֹͣ����
			mediaPlayer.release();
		}
		vibrator.cancel(); // ֹͣ��
		wl.release(); // �ر���Ļ
		kl.reenableKeyguard(); // ����
		finish(); // ��������
	}

	void writeGetUpInfo() // ��¼����Ϣ�����ݿ�
	{
		GetUpDatabaseHelper dbHelper = new GetUpDatabaseHelper(
				AlarmRingActivity.this, GetUpDatabaseHelper.DATABASE_NAME);
		GetUpInfo info = new GetUpInfo();
		Calendar c = Calendar.getInstance();
		info.setYear(c.get(Calendar.YEAR));
		info.setMonth(c.get(Calendar.MONTH) + 1);
		info.setDay(c.get(Calendar.DATE));
		info.setHour(param.getHour());
		info.setMinute(param.getMinute());
		info.setTime(allTime / 1000);
		info.setLevel(param.getLevel());
		info.setSuccess(true);
		dbHelper.insertGetUp(info);
	}

	/*
	 * public boolean onKeyDown(int keyCode, KeyEvent event) { // ����ʵ��� switch
	 * (keyCode) { case KeyEvent.KEYCODE_BACK: return true; case
	 * KeyEvent.KEYCODE_CALL: return true; case KeyEvent.KEYCODE_VOLUME_DOWN:
	 * return true; case KeyEvent.KEYCODE_HOME: return true; case
	 * KeyEvent.KEYCODE_POWER: return true; } return super.onKeyDown(keyCode,
	 * event); }
	 * 
	 * public void onAttachedToWindow() { // ���μ� this.getWindow().setType(
	 * WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
	 * super.onAttachedToWindow(); }
	 */

}
