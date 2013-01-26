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

	private Button getupButton; // 起床按钮
	private Button napButton; // 赖床按钮
	public MediaPlayer mediaPlayer; // 播放闹钟铃声
	private Vibrator vibrator; // 震动
	private PowerManager.WakeLock wl; // 控制屏幕亮度
	private KeyguardLock kl; // 控制锁屏幕
	private long allTime; // 起床总共花费的时间
	private ClockParameter param; // 存放闹铃信息的对象
	public int answer; // 存放问题答案

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
		unlockScreen(); // 解锁屏幕 点亮屏幕 调大音量
		// startVibrate(vibrator); // 开始震动
		// playMusic(); // 开始响铃
	}

	View.OnClickListener listener = new View.OnClickListener() { // 监听按键
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.getUp:
				showQuestionDialog(R.id.getUp); // 起床按钮，弹出起床问题
				break;
			case R.id.nap:
				showQuestionDialog(R.id.nap); // 赖床按钮，弹出赖床问题
				break;
			}
		}
	};

	void showQuestionDialog(int id) { // 根据难度和所触发的按钮的id出问题
		String question;
		if (param.getLevel() == 1 && id == R.id.getUp) // 等级为1并且id被视为起床按钮激发 加法
		{
			int a = (int) (Math.random() * 100) + 1;
			int b = (int) (Math.random() * 100) + 1;
			question = ("   " + a + "+" + b + "=     ");
			answer = a + b;
		} else { // 乘法
			int a = (int) (Math.random() * 30) + 1;
			int b = (int) (Math.random() * 30) + 1;
			question = ("   " + a + "*" + b + "=     ");
			answer = a * b;
		}
		showDialog(question, id);	//弹出问题框
	}

	boolean isInteger(String value) { // 判断字符串是否为整数
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * 根据参数question设置Dialog的内容， 点击确认后，获取输入框的内容 一、若结果为空、不是数字 或结果错误，
	 * 弹出提示并更新问题并弹出新的Dialog 二、若结果正确，根据参数 id 判断 ，如果id为R.id.getup即该次弹出由起床按钮触发
	 * 则停止闹铃，关闭震动，显示起床时间等 如果 id 为 R.id.nap 即该次弹出由赖床按钮触发 暂时停止响铃 并设置五分钟后再次响铃
	 */
	void showDialog(String question, final int id) {
		final EditText editText = new EditText(this);

		AlertDialog.Builder builder = new Builder(AlarmRingActivity.this);
		builder.setMessage("醒神问题:   " + question);
		builder.setTitle("输对答案就可以了~");
		builder.setView(editText);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String userAnswer = editText.getText().toString();
				if (userAnswer == null || userAnswer.isEmpty()) { // 输入的内容为空,弹出提示
					Toast.makeText(AlarmRingActivity.this, "不能交白卷啊~再来~~", 3000)
							.show();
					showQuestionDialog(id); // 重新出题
				} else if (!isInteger(userAnswer)) // 输入内容不是整数 ,弹出提示
				{
					Toast.makeText(AlarmRingActivity.this, "答案只能是正整数啊~再来~~",
							3000).show();
					showQuestionDialog(id); // 重新出题
				} else {
					if (Integer.parseInt(editText.getText().toString()) == answer) { // 答案正确
						if (id == R.id.getUp) {
							// timeCalculate 计算时间的函数 显示起床所用时间
							Toast.makeText(AlarmRingActivity.this,
									timeCalculate(), 5000).show();
							writeGetUpInfo(); // 将起床时间写入数据库
						} else {
							Toast.makeText(AlarmRingActivity.this,
									"好吧，你可以再多睡五分钟~~", 5000).show(); // 贪睡设置，5分钟后重新响铃
							Intent intent = new Intent(AlarmRingActivity.this,
									AlarmRingActivity.class);
							Bundle bundle = new Bundle();
							bundle.putSerializable("clock", param);
							intent.putExtras(bundle);
							// PendingIntent.FLAG_UPDATE_CURRENT！！不设置的话 相当于无更新
							PendingIntent pi = PendingIntent.getActivity(
									AlarmRingActivity.this, 0, intent,
									PendingIntent.FLAG_UPDATE_CURRENT);
							AlarmManager manager = (AlarmManager) getSystemService(Service.ALARM_SERVICE);
							manager.set(AlarmManager.RTC_WAKEUP,
									System.currentTimeMillis() + 1000 * 2, pi);
							finish();
						}
						dialog.dismiss();
						stopAlarm(); // 停止响铃
					} else { // 答案错误
						Toast.makeText(AlarmRingActivity.this, "   答错了~再来~~",
								2000).show();
						dialog.dismiss();
						showQuestionDialog(id); // 重新出题
					}
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	String timeCalculate() {
		Calendar c = Calendar.getInstance(); // 取得闹铃时间
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.HOUR_OF_DAY, param.getHour());
		c.set(Calendar.MINUTE, param.getMinute());
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		allTime = System.currentTimeMillis() - c.getTimeInMillis(); // 起床时间等于现在时间-闹铃时间

		System.out.println("test time="+allTime);
		
		String result = "Tips 本次起床所耗时间：";
		if (allTime / AlarmManager.INTERVAL_HOUR > 0) // 求出时
			result += (allTime / AlarmManager.INTERVAL_HOUR + " 小时 ");
		if (allTime / 1000 / 60 % 60 > 0) // 求出分
			result += (allTime % AlarmManager.INTERVAL_HOUR / 1000 / 60 + " 分 ");
		result += (allTime % AlarmManager.INTERVAL_HOUR / 1000 % 60 + " 秒"); // 求出秒
		System.out.println(result);
		return result;
	}

	void unlockScreen() {
		KeyguardManager km = (KeyguardManager) getSystemService(Service.KEYGUARD_SERVICE);
		kl = km.newKeyguardLock("unLock");
		kl.disableKeyguard();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);// 获取电源管理器对象
		wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.FULL_WAKE_LOCK, "bright");
		// 获取PowerManager.WakeLock对象，后面的参数|表示同时传入两个值，最后的是LogCat里用的Tag
		wl.acquire();// 点亮屏幕
	}

	void startVibrate(Vibrator vibrator) { // 震动设置
		long[] pattern = new long[] { 300, 1000, 1500, 2000 };
		vibrator.vibrate(pattern, 2);
	}

	private void playMusic() { // 播放铃声设置
		mediaPlayer = MediaPlayer.create(this, R.raw.onepiece);
		mediaPlayer.setLooping(true);
		mediaPlayer.start();
	}

	void stopAlarm() // 关闭闹钟 记录起床信息到数据库
	{
		// if (mediaPlayer.isPlaying())
		// mediaPlayer.stop(); // 停止播放
		vibrator.cancel(); // 停止震动
		wl.release(); // 关闭屏幕
		kl.reenableKeyguard(); // 锁屏
		finish(); // 结束程序
	}

	void writeGetUpInfo() // 记录起床信息到数据库
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
		System.out.println("起床数据库已更新~");
	}

	/*
	 * public boolean onKeyDown(int keyCode, KeyEvent event) { // 屏蔽实体键 switch
	 * (keyCode) { case KeyEvent.KEYCODE_BACK: return true; case
	 * KeyEvent.KEYCODE_CALL: return true; case KeyEvent.KEYCODE_VOLUME_DOWN:
	 * return true; case KeyEvent.KEYCODE_HOME: return true; case
	 * KeyEvent.KEYCODE_POWER: return true; } return super.onKeyDown(keyCode,
	 * event); }
	 * 
	 * public void onAttachedToWindow() { // 屏蔽键 this.getWindow().setType(
	 * WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
	 * super.onAttachedToWindow(); }
	 */

}
