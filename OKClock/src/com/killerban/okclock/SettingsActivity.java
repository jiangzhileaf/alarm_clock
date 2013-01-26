package com.killerban.okclock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.killerban.database.GetUpDatabaseHelper;
import com.killerban.database.UserDatabaseHelper;
import com.killerban.model.User;
import com.killerban.web.GetPostUtil;

public class SettingsActivity extends Activity {
	private Button volumeUpButton; // 预设闹铃音量，增高
	private Button volumeDownButton; // 预设闹铃音量，降低
	private Button showGetUpDataButton; // 展示起床信息
	private Button uploadButton; // 上传数据
	private Button rankListButton; // 排行榜 最近一次起床时间的排行榜
	private Button freebackButton; // 意见反馈
	private String[] registeredInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		volumeUpButton = (Button) findViewById(R.id.volumeup);
		volumeDownButton = (Button) findViewById(R.id.volumedown);
		showGetUpDataButton = (Button) findViewById(R.id.statistics);
		rankListButton = (Button) findViewById(R.id.rankList);
		uploadButton = (Button) findViewById(R.id.upload);
		freebackButton = (Button) findViewById(R.id.feedback);

		showGetUpDataButton.setOnClickListener(listener);
		rankListButton.setOnClickListener(listener);
		uploadButton.setOnClickListener(listener);
		freebackButton.setOnClickListener(listener);
		volumeUpButton.setOnClickListener(listener);
		volumeDownButton.setOnClickListener(listener);
	}

	View.OnClickListener listener = new View.OnClickListener() {
		public void onClick(View v) {
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			switch (v.getId()) {
			case R.id.volumeup:// 增大音量
				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
								| AudioManager.FLAG_PLAY_SOUND);
				break;
			case R.id.volumedown:// 减少音量
				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
								| AudioManager.FLAG_PLAY_SOUND);// 调低声音
				break;
			case R.id.statistics: // 展示数据
				Intent intent = new Intent(SettingsActivity.this,DataShowActivity.class);
				startActivity(intent);
				break;
			case R.id.upload: // 上传数据
				System.out.println("我点击了！！ ");
				uploadData();
				break;
			case R.id.rankList: // PK
				pkWithOther();
				break;
			case R.id.feedback: // 意见反馈
				showFeedbackDialog();
				break;
			}
		}
	};

	void showFeedbackDialog() {
		final EditText editText = new EditText(this);
		editText.setTextSize(20);
		AlertDialog.Builder builder = new Builder(SettingsActivity.this);
		builder.setTitle("有什么不满尽管说~~");
		builder.setView(editText);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sentFeedback(editText.getText().toString());
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	void sentFeedback(String info) {
		String url = "http://172.16.41.161:9000/Mobile/receiveAdvice";
		String param = "advice=" + info;
		String response = GetPostUtil.sendPost(url, param); // 发送数据至服务器
															// 并将返回信息传入至response中
		Toast.makeText(SettingsActivity.this, response, Toast.LENGTH_LONG)
				.show();
	}

	// 取最近一次起床时间和比人进行PK
	void pkWithOther() {
		GetUpDatabaseHelper db = new GetUpDatabaseHelper(SettingsActivity.this,
				GetUpDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectGetUp();
		cursor.moveToLast(); // 取最后一次记录
		String time = cursor.getString(cursor
				.getColumnIndex(GetUpDatabaseHelper.TIME));
		System.out.println("PKTime=" + time);

		// 向服务器发送请求，并将返回信息传入至response中
		String url = "http://172.16.41.161:9000/Mobile/getOrder";
		String param = "duration=" + time;
		String response = GetPostUtil.sendPost(url, param);

		Toast.makeText(SettingsActivity.this, response, Toast.LENGTH_LONG)
				.show();
		cursor.close();
		db.close();
	}

	// 发送用户的起床信息到服务器，在发送之前先判断用户是否已经登录 否则弹出注册新用户的对话框
	void uploadData() {
		UserDatabaseHelper db = new UserDatabaseHelper(SettingsActivity.this,
				UserDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectUser();
		if (cursor.getCount() == 0) // 无用户信息 新注册
		{
			registeredInfo = new String[] { "", "", "", "" };
			registered();
			System.out.println("注册新用户");
		} else {

		}
		cursor.close();
		db.close();
	}

	// 向服务器发送注册信息，若成功则写入数据库，否则 弹出错误提示,并重新弹出注册Dialog让用户重新注册
	void sentRegisteredInfo() {
		String url = "http://172.16.41.161:9000/Mobile/signin";
		String param = "loginName=" + registeredInfo[0] + "&password1="
				+ registeredInfo[1] + "&password2=" + registeredInfo[2]
				+ "&name=" + registeredInfo[3];
		System.out.println(param);
		String response = GetPostUtil.sendPost(url, param); // 发送数据至服务器
		Toast.makeText(SettingsActivity.this, response, Toast.LENGTH_LONG)
				.show();

		System.out.println(response + "\n" + response == "注册成功");
		// 注册成功 写入数据库
		if (response.equals("注册成功")) {
			User user = new User();
			user.setUserid(registeredInfo[0]);
			user.setPassword(registeredInfo[1]);
			user.setUsername(registeredInfo[3]);
			UserDatabaseHelper db = new UserDatabaseHelper(
					SettingsActivity.this, UserDatabaseHelper.DATABASE_NAME);
			db.insertUser(user);
		} else {
			registered(); // 重新注册
		}

	}

	// 弹出自定义的注册Dialog 根据输入信息发送注册请求
	@SuppressLint("ShowToast")
	void registered() {

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.registered_layout,
				(ViewGroup) findViewById(R.id.registed_dialog));

		// 分别为 账号，密码，密码确认，昵称 的文本输入框 和 检测输入信息是否有误的按钮
		final EditText userid = (EditText) layout
				.findViewById(R.id.userid_eidt);
		final EditText password = (EditText) layout
				.findViewById(R.id.password_edit);
		final EditText pswagain = (EditText) layout
				.findViewById(R.id.password_again_edit);
		final EditText username = (EditText) layout
				.findViewById(R.id.username_edit);
		final Button check = (Button) layout.findViewById(R.id.check_info);

		// 检测注册信息是否符合规范
		check.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				boolean ok = true;
				registeredInfo[0] = (userid.getText().toString());
				registeredInfo[1] = (password.getText().toString());
				registeredInfo[2] = (pswagain.getText().toString());
				registeredInfo[3] = (username.getText().toString());
				if (registeredInfo[1].length() < 6) {
					ok = false;
					Toast.makeText(SettingsActivity.this, "密码长度少于 6 位，请重新输入",
							Toast.LENGTH_SHORT).show();
					password.selectAll();
				}
				if (!registeredInfo[1].equals(registeredInfo[2])) {
					ok = false;
					Toast.makeText(SettingsActivity.this, "两次输入的密码不一致，请重新输入",
							Toast.LENGTH_SHORT).show();
					pswagain.selectAll();
				}
				if (registeredInfo[3].length() < 2) {
					ok = false;
					Toast.makeText(SettingsActivity.this, "用户昵称长度小于 2 位，请重新输入",
							Toast.LENGTH_SHORT).show();
					username.selectAll();
				}
				if(ok)
					Toast.makeText(SettingsActivity.this, "信息填写正确，可以点击确认了~",
							Toast.LENGTH_SHORT).show();
			}
		});
		AlertDialog.Builder builder = new Builder(SettingsActivity.this);
		builder.setTitle("       新用户注册   ");
		builder.setView(layout);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				registeredInfo[0] = (userid.getText().toString());
				registeredInfo[1] = (password.getText().toString());
				registeredInfo[2] = (pswagain.getText().toString());
				registeredInfo[3] = (username.getText().toString());

				// 发送注册信息至服务器
				sentRegisteredInfo();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	// 展示用户的所有起床信息
	void showData() {

		GetUpDatabaseHelper dbHelper = new GetUpDatabaseHelper(
				SettingsActivity.this, GetUpDatabaseHelper.DATABASE_NAME);
		Cursor cursor = dbHelper.selectGetUp();
		while (cursor.moveToNext()) {
			String s = "";
			s += Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.YEAR))) + "年";
			s += Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.MONTH))) + "月";
			s += Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.DAY))) + "日 \n";
			s += "等级："
					+ Integer.parseInt(cursor.getString(cursor
							.getColumnIndex(GetUpDatabaseHelper.LEVEL)));
			s += "\n耗时："
					+ Integer.parseInt(cursor.getString(cursor
							.getColumnIndex(GetUpDatabaseHelper.TIME))) + "秒\n";
			Toast.makeText(SettingsActivity.this, s, Toast.LENGTH_LONG).show();
		}
		cursor.close();
		dbHelper.close();
	}
}
