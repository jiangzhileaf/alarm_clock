package com.killerban.okclock;

import java.util.Calendar;

import com.killerban.database.GetUpDatabaseHelper;
import com.killerban.model.ClockParameter;
import com.killerban.model.GetUpInfo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AlarmRingActivity extends Activity {

	private Button getupButton; // �𴲰�ť
	private Button napButton; // ������ť
	public MediaPlayer mediaPlayer; // ������������
	private Vibrator vibrator; // ��
	private PowerManager.WakeLock wl; // ������Ļ����
	private KeyguardLock kl; // ��������Ļ
	private long allTime; // ���ܹ����ѵ�ʱ��
	private ClockParameter param; // ���������Ϣ�Ķ���
	public int answer; // ��������

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_ring);
		getupButton = (Button) findViewById(R.id.getUp);
		napButton = (Button) findViewById(R.id.nap);

		getupButton.setOnClickListener(listener);
		napButton.setOnClickListener(listener);

		param = (ClockParameter) getIntent().getExtras().getSerializable(
				"clock");
		System.out.println("oncreate" + param.getHour()+" "+param.getMinute());
		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		unlockScreen(); // ������Ļ ������Ļ ��������
		// startVibrate(vibrator); // ��ʼ��
		// playMusic(); // ��ʼ����
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
		showDialog(question, id);	//���������
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
					Toast.makeText(AlarmRingActivity.this, "���ܽ��׾�~����~~", 3000)
							.show();
					showQuestionDialog(id); // ���³���
				} else if (!isInteger(userAnswer)) // �������ݲ������� ,������ʾ
				{
					Toast.makeText(AlarmRingActivity.this, "��ֻ������������~����~~",
							3000).show();
					showQuestionDialog(id); // ���³���
				} else {
					if (Integer.parseInt(editText.getText().toString()) == answer) { // ����ȷ
						if (id == R.id.getUp) {
							// timeCalculate ����ʱ��ĺ��� ��ʾ������ʱ��
							Toast.makeText(AlarmRingActivity.this,
									timeCalculate(), 5000).show();
							writeGetUpInfo(); // ����ʱ��д�����ݿ�
						} else {
							Toast.makeText(AlarmRingActivity.this,
									"�ðɣ�������ٶ�˯�����~~", 5000).show(); // ̰˯���ã�5���Ӻ���������
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

	String timeCalculate() {
		Calendar c = Calendar.getInstance(); // ȡ������ʱ��
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, param.getHour());
		c.set(Calendar.MINUTE, param.getMinute());
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		allTime = System.currentTimeMillis() - c.getTimeInMillis(); // ��ʱ���������ʱ��-����ʱ��

		System.out.println("test time="+allTime);
		
		String result = "Tips ����������ʱ�䣺";
		if (allTime / AlarmManager.INTERVAL_HOUR > 0) // ���ʱ
			result += (allTime / AlarmManager.INTERVAL_HOUR + " Сʱ ");
		if (allTime / 1000 / 60 % 60 > 0) // �����
			result += (allTime % AlarmManager.INTERVAL_HOUR / 1000 / 60 + " �� ");
		result += (allTime % AlarmManager.INTERVAL_HOUR / 1000 % 60 + " ��"); // �����
		System.out.println(result);
		return result;
	}

	void unlockScreen() {
		KeyguardManager km = (KeyguardManager) getSystemService(Service.KEYGUARD_SERVICE);
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

	void startVibrate(Vibrator vibrator) { // ������
		long[] pattern = new long[] { 300, 1000, 1500, 2000 };
		vibrator.vibrate(pattern, 2);
	}

	private void playMusic() { // ������������
		mediaPlayer = MediaPlayer.create(this, R.raw.onepiece);
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}

	void stopAlarm() // �ر����� ��¼����Ϣ�����ݿ�
	{
		// if (mediaPlayer.isPlaying())
		// mediaPlayer.stop(); // ֹͣ����
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
		info.setTime(allTime / 1000);
		info.setLevel(param.getLevel());
		info.setSuccess(true);
		dbHelper.insertGetUp(info);
		System.out.println("�����ݿ��Ѹ���~");
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
