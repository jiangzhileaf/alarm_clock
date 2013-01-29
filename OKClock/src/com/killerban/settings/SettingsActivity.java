package com.killerban.settings;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.killerban.okclock.R;
import com.killerban.web.GetPostUtil;
import com.killerban.web.SentDataService;

/**
 * @author Ban 设置Activity 有以下功能，预设所有闹铃音量 登录，上传数据，起床时间PK ，意见反馈，查看本地起床时间数据
 */
public class SettingsActivity extends Activity {
	private final static String TAG = "SettingActivity";
	private final static int REQUEST_CODE = 1;

	private Button volumeUpButton; // 预设闹铃音量，增高
	private Button volumeDownButton; // 预设闹铃音量，降低
	private Button showGetUpDataButton; // 展示起床信息
	private Button uploadButton; // 上传数据
	private Button rankListButton; // 排行榜 最近一次起床时间的排行榜
	private Button freebackButton; // 意见反馈
	private Button loginButton; // 登录
	private Button upgradeButton;	//检测更新
	private String[] registeredInfo = new String[] { "", "", "", "" };

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
		loginButton = (Button) findViewById(R.id.login);
		upgradeButton = (Button)findViewById(R.id.update);

		showGetUpDataButton.setOnClickListener(listener);
		rankListButton.setOnClickListener(listener);
		uploadButton.setOnClickListener(listener);
		freebackButton.setOnClickListener(listener);
		volumeUpButton.setOnClickListener(listener);
		volumeDownButton.setOnClickListener(listener);
		loginButton.setOnClickListener(listener);
		upgradeButton.setOnClickListener(listener);
	}

	// 各个按钮的监听器 对于需要连接网络才能用的功能，先调用网络检测的Activity
	// 连接成功后再调用对应的功能
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
				Intent intent = new Intent(SettingsActivity.this,
						DataShowActivity.class);
				startActivity(intent);
				break;
			case R.id.upload: // 上传数据
				Log.i(TAG, "上传数据");
				sentWebIntent(R.id.upload);		
				break;
			case R.id.rankList: // PK
				sentWebIntent(R.id.rankList);
				break;
			case R.id.feedback: // 意见反馈
				sentWebIntent(R.id.feedback);
				break;
			case R.id.login:
				sentWebIntent(R.id.login); // 登录
				break;
			case R.id.update:		//检测更新
				sentWebIntent(R.id.update);
				break;
			}
		}
	};

	// 测试网络连接状态
	// 在测试完后执行相应的操作，根据操作的Id而定
	public void sentWebIntent(int id) {
		Intent intent = new Intent(SettingsActivity.this,
				WebTranslateActivity.class);
		intent.putExtra("id", id);
		startActivityForResult(intent, REQUEST_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE
				&& resultCode == WebTranslateActivity.RESULT_CODE) {
			if (data.getExtras().getInt("status") == 1) {
				
				doOperation(data.getExtras().getInt("id"));
			} else
				Toast.makeText(SettingsActivity.this, "未检测到可用网络，请先联网~",
						Toast.LENGTH_SHORT).show();
		}
	}

	// 测试网络状态可用时，执行以下操作
	public void doOperation(int id) {
		switch (id) {
		case R.id.upload:
			System.out.println("upload");
			uploadData();
			break;
		case R.id.login:
			System.out.println("login");
			showLoginDialog();
			break;
		case R.id.feedback:
			System.out.println("feedback");
			showFeedbackDialog();
			break;
		case R.id.rankList:
			pkWithOther();
			break;
		case R.id.update:
			upDateSoftware();
			break;
		}
	}
	
	//根据版本号判断是否为最新版，若是则不用下载，否，则启动更新服务,从后台下载
	void upDateSoftware()
	{
		String url="http://172.16.41.161/Mobile/getNewestVersion";
		String param="";
		String appVersion="";
		String newVersion=GetPostUtil.sendPost(url, param);
		PackageManager manager = this.getPackageManager();
		try {
		        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
		        appVersion = info.versionName; // 版本名，versionCode同理
		} catch (NameNotFoundException e) {
		        e.printStackTrace();
		}
		
		if(newVersion.equals(appVersion))	//版本号相同
		{
			Toast.makeText(SettingsActivity.this, "你当前使用的是最新版本，无需更新~", Toast.LENGTH_SHORT).show();
		}
		else
		{
			AlertDialog.Builder builder = new Builder(SettingsActivity.this);
			builder.setTitle("你当前版本为 "+appVersion+" ,最新版本为 "+newVersion+",是否下载最新版？");
			builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 发送信息到服务器
					Intent intent = new Intent(SettingsActivity.this,UpgradeService.class);
					startService(intent);
					Toast.makeText(SettingsActivity.this, "已切换至后台下载", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("取消", null);
			builder.create().show();
			
		}
	}

	// 意见反馈Dialog
	void showFeedbackDialog() {
		final EditText editText = new EditText(this);
		editText.setTextSize(20);
		AlertDialog.Builder builder = new Builder(SettingsActivity.this);
		builder.setTitle("有什么不满尽管说~~");
		builder.setView(editText);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 发送信息到服务器
				sentFeedback(editText.getText().toString());
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	// 发送意见反馈到服务器
	void sentFeedback(String info) {
		String url = "http://172.16.41.161/Mobile/receiveAdvice";
		String param = "advice=" + info;
		String response = GetPostUtil.sendPost(url, param); // 发送数据至服务器
															// 并将返回信息传入至response中
		showMessageDiolog(response);
	}

	// 取最近一次起床时间和比人进行PK
	void pkWithOther() {
		GetUpDatabaseHelper db = new GetUpDatabaseHelper(SettingsActivity.this,
				GetUpDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectGetUp();
		if (cursor.getCount() != 0) // 数据库有起床时间的记录
		{
			cursor.moveToLast(); // 取最后一次记录
			String time = cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.TIME));

			// 向服务器发送请求，并将返回信息传入至response中
			String url = "http://172.16.41.161/Mobile/getOrder";
			String param = "duration=" + time;
			String response = GetPostUtil.sendPost(url, param);

			showMessageDiolog(response);
		} else // 数据库为空，无法
		{
			showMessageDiolog("数据库为空，无记录可与比人PK~~"); // 弹出提示
		}
		cursor.close();
		db.close();
	}

	// 发送用户的起床信息到服务器，在发送之前先判断用户是否已经登录 否则弹出注册新用户的对话框
	// 若已经注册了账号后 登录 判断密码是否正确 是则可以发送用户的起床信息到服务器 ，否，则弹出登录框
	void uploadData() {
		UserDatabaseHelper db = new UserDatabaseHelper(SettingsActivity.this,
				UserDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectUser();
		if (cursor.getCount() == 0) // 无用户信息 先注册
		{

			Toast.makeText(SettingsActivity.this, "你还没注册，请先注册吧~",
					Toast.LENGTH_SHORT).show();
			cursor.close();
			db.close();
			showRegisteredDiolag();
		} else {
			cursor.moveToLast();
			String loginId = cursor.getString(cursor
					.getColumnIndex(UserDatabaseHelper.USERID));
			String loginPSW = cursor.getString(cursor
					.getColumnIndex(UserDatabaseHelper.PASSWORD));
			cursor.close();
			db.close();
			if (login(loginId, loginPSW)) // 登录成功 用户名和密码匹配
			{
				showMessageDiolog("OK，后台正在为你上传数据到服务器~~");
				Intent intent = new Intent(this, SentDataService.class);
				startService(intent);
			} else {
				showLoginDialog(); // 跳至登录Dialog
			}
		}

	}

	// 登录的对话框
	void showLoginDialog() {

		final UserDatabaseHelper db = new UserDatabaseHelper(
				SettingsActivity.this, UserDatabaseHelper.DATABASE_NAME);
		final Cursor cursor = db.selectUser();
		if (cursor.getCount() == 0) // 未注册 跳到注册页面
		{
			cursor.close();
			db.close();
			showRegisteredDiolag();
		} else {
			cursor.moveToLast(); // 已经注册，取得数据库的用户信息
			final String loginId = cursor.getString(cursor
					.getColumnIndex(UserDatabaseHelper.USERID));
			final String loginPSW = cursor.getString(cursor
					.getColumnIndex(UserDatabaseHelper.PASSWORD));
			final String userName = cursor.getString(cursor
					.getColumnIndex(UserDatabaseHelper.USERNAME));

			final EditText passwordET = new EditText(this);
			// 设置输入字符为密码类型 即输入后会变*
			passwordET.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			passwordET.setText(loginPSW);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("登录：");
			builder.setView(passwordET);
			builder.setMessage("用户名:" + loginId)
					.setCancelable(false)
					.setPositiveButton("确认",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 密码有变更，代表用户修改过密码，更新数据库
									if (loginPSW != passwordET.getText()
											.toString()) {
										db.updateUser(loginId, new User(
												loginId, passwordET.getText()
														.toString(), userName));
									}
									// 向服务器发送登录请求
									login(loginId, passwordET.getText()
											.toString());
									dialog.dismiss();
									db.close();
								}
							});
			builder.setNegativeButton("取消", null);
			builder.create().show();
		}
		cursor.close();
		db.close();
	}

	// 向服务器发送登录请求
	boolean login(String id, String password) {
		String url = "http://172.16.41.161/Mobile/login";
		String param = "loginName=" + id + "&password=" + password;
		String response = GetPostUtil.sendPost(url, param);
		Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
		if (!response.equals("登录成功")) // 密码不对 ，重新登录
		{
			showLoginDialog();
		}
		return response.equals("登录成功");
	}

	// 向服务器发送注册请求，若成功则写入数据库，否则 弹出错误提示,并重新弹出注册Dialog让用户重新注册
	void sentRegisteredInfo() {
		String url = "http://172.16.41.161/Mobile/signUp";
		String param = "user.loginName=" + registeredInfo[0]
				+ "&user.password=" + registeredInfo[1] + "&user.name="
				+ registeredInfo[3];
		String response = GetPostUtil.sendPost(url, param); // 发送数据至服务器
		Toast.makeText(this,
				"注册" + (Boolean.parseBoolean(response) ? "成功" : "失败"),
				Toast.LENGTH_LONG).show();
		// 注册成功 写入数据库
		if (Boolean.parseBoolean(response)) {
			User user = new User();
			user.setUserid(registeredInfo[0]);
			user.setPassword(registeredInfo[1]);
			user.setUsername(registeredInfo[3]);
			System.out.println("id " + registeredInfo[0] + " passwd "
					+ registeredInfo[1] + " name " + registeredInfo[3]);
			UserDatabaseHelper db = new UserDatabaseHelper(
					SettingsActivity.this, UserDatabaseHelper.DATABASE_NAME);
			db.insertUser(user);
			uploadData(); // 可以登录发送数据了
		} else {
			showRegisteredDiolag(); // 重新注册
		}

	}

	// 弹出注册Dialog 根据输入信息发送注册请求
	@SuppressLint("ShowToast")
	void showRegisteredDiolag() {

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
				System.out.println("registeredInfo[0]" + registeredInfo[0]);
				registeredInfo[0] = (userid.getText().toString().trim());
				registeredInfo[1] = (password.getText().toString().trim());
				registeredInfo[2] = (pswagain.getText().toString().trim());
				registeredInfo[3] = (username.getText().toString().trim());
				if (registeredInfo[0].equals("")) {
					Toast.makeText(SettingsActivity.this, "用户账号不能为空",
							Toast.LENGTH_SHORT).show();
					userid.selectAll();
				}
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
				if (ok)
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
				try {
					registeredInfo[0] = (userid.getText().toString());
					registeredInfo[1] = (password.getText().toString());
					registeredInfo[2] = (pswagain.getText().toString());
					registeredInfo[3] = (username.getText().toString());

					// 发送注册信息至服务器
					sentRegisteredInfo();
					dialog.dismiss();
				} catch (Exception e) {
					Toast.makeText(SettingsActivity.this, "没检测过不要按确认啊，测试一下吧~",
							Toast.LENGTH_LONG).show();
					keepDialog(dialog);
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	// 弹出提示框 用于显示其他函数执行过程中返回的提示信息
	void showMessageDiolog(String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示~");
		builder.setMessage(info).setCancelable(false)
				.setPositiveButton("确认", null);
		builder.create().show();
	}

	private void keepDialog(DialogInterface dialog) {
		try {
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
