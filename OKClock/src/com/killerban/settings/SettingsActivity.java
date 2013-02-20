package com.killerban.settings;

import java.util.ArrayList;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
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

	private Button volumeUpButton; // 预设闹铃音量，增高
	private Button volumeDownButton; // 预设闹铃音量，降低
	private Button showGetUpDataButton; // 展示起床信息
	private Button uploadButton; // 上传数据
	private Button rankListButton; // 排行榜 最近一次起床时间的排行榜
	private Button freebackButton; // 意见反馈
	private Button registerButton; // 注册
	private Button loginButton; // 登录
	private Button upgradeButton; // 检测更新
	private String[] registeredInfo = new String[] { "", "", "", "" }; // 注册信息
	private ArrayList<User> list = new ArrayList<User>();
	private String[] session = new String[] { "", "" }; // 当前登录的账号信息,分别为账号和密码

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
		registerButton = (Button) findViewById(R.id.register);
		upgradeButton = (Button) findViewById(R.id.update);

		showGetUpDataButton.setOnClickListener(listener);
		rankListButton.setOnClickListener(listener);
		uploadButton.setOnClickListener(listener);
		freebackButton.setOnClickListener(listener);
		volumeUpButton.setOnClickListener(listener);
		volumeDownButton.setOnClickListener(listener);
		loginButton.setOnClickListener(listener);
		upgradeButton.setOnClickListener(listener);
		registerButton.setOnClickListener(listener);

		// 因为此Activity需要经常读取数据库，所以先将其读取至Arraylist中
		getUserDatabaseInfo();
	}

	// 获取数据库信息，保存至Arraylist中，为以后方便读取
	public void getUserDatabaseInfo() {

		UserDatabaseHelper db = new UserDatabaseHelper(SettingsActivity.this,
				UserDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectUser();
		User user = null;
		while (cursor.moveToNext()) {
			user = new User();
			// 调用User对象的方法，将数据库信息转赋值给对应的对象属性
			user.translateFromDB(cursor);
			list.add(user);
		}
		cursor.close();
		db.close();
	}

	// 各个按钮的监听器
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
			case R.id.upload: // 上传起床时间数据
				uploadData();
				break;
			case R.id.login: // 登录
				showLoginDialog();
				break;
			case R.id.register: // 注册
				showRegisteredDiolag();
				break;
			case R.id.feedback: // 意见反馈
				showFeedbackDialog();
				break;
			case R.id.rankList: // 起床时间pk
				pkWithOther();
				break;
			case R.id.update: // 更新软件
				upDateSoftware();
				break;
			}
		}
	};

	// 根据版本号判断是否为最新版，若是则不用下载，否，则启动更新服务,从后台下载
	void upDateSoftware() {
		String url = "http://172.16.41.161/Mobile/getNewestVersion";
		String param = "";
		String appVersion = "";

		LinkToWeb task = new LinkToWeb();
		String newVersion = task.doInBackground(url, param);

		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			appVersion = info.versionName; // 版本名，versionCode同理
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		// 能连接到网络，获取得到版本号
		if (!newVersion.equals("false") && !newVersion.equals("出错了")) {
			if (newVersion.equals(appVersion)) // 版本号相同
			{
				Toast.makeText(SettingsActivity.this, "你当前使用的是最新版本，无需更新~",
						Toast.LENGTH_SHORT).show();
			} else {
				AlertDialog.Builder builder = new Builder(SettingsActivity.this);
				builder.setTitle("你当前版本为 " + appVersion + " ,最新版本为 "
						+ newVersion + ",是否下载最新版？");
				builder.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 发送信息到服务器
								Intent intent = new Intent(
										SettingsActivity.this,
										UpgradeService.class);
								startService(intent);
								Toast.makeText(SettingsActivity.this,
										"已切换至后台下载,完成后会弹出提醒~",
										Toast.LENGTH_SHORT).show();
								dialog.dismiss();
							}
						});
				builder.setNegativeButton("取消", null);
				builder.create().show();
			}
		} else {
			Toast.makeText(SettingsActivity.this, "出错了", Toast.LENGTH_SHORT)
					.show();
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

		LinkToWeb task = new LinkToWeb();
		String response = task.doInBackground(url, param); // 发送数据至服务器
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

			LinkToWeb task = new LinkToWeb();
			String response = task.doInBackground(url, param);

			showMessageDiolog(response);
		} else // 数据库为空，无法
		{
			showMessageDiolog("数据库为空，无记录可与比人PK~~"); // 弹出提示
		}
		cursor.close();
		db.close();
	}

	// 发送用户的起床信息到服务器，在发送之前搜索数据库是否存在用户账号，
	// 若存在，则选择已经登录的账号登录，若登录成功，后台上传数据，失败则弹出提示
	void uploadData() {
		if (list.size() == 0) // 无用户信息 先注册
		{
			Toast.makeText(SettingsActivity.this, "你还没注册，请先注册吧~",
					Toast.LENGTH_SHORT).show();
		} else {
			if (session[0].equals("")) {
				Toast.makeText(SettingsActivity.this, "你还没有登录，请先登录~",
						Toast.LENGTH_SHORT).show();
			} else {
				String loginId = session[0];
				String loginPSW = session[1];
				if (login(loginId, loginPSW)) // 登录成功 用户名和密码匹配
				{
					showMessageDiolog("OK，后台正在为你上传数据到服务器~~");
					Intent intent = new Intent(this, SentDataService.class);
					intent.putExtra("id", loginId);
					intent.putExtra("password", loginPSW);
					startService(intent);
				} else {
					Toast.makeText(SettingsActivity.this, "登录信息有错误，请重新登录~",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	// 登录的对话框 先在数据库取得首个账号，自动填充
	void showLoginDialog() {

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.login_layout,
				(ViewGroup) findViewById(R.id.login_dialog));
		final EditText userIdET = (EditText) layout
				.findViewById(R.id.login_userid_eidt);
		final EditText passwordET = (EditText) layout
				.findViewById(R.id.login_password_edit);
		// 设置输入字符为密码类型 即输入后会变*
		passwordET.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);

		// 若数据库有数据，则根据数据填充文本输入框
		if (list.size() != 0) {
			userIdET.setText(list.get(0).getUserid());
			passwordET.setText(list.get(0).getPassword());
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("登 录：");
		builder.setView(layout);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				// 发送登录请求
				if (!userIdET.getText().toString().trim().equals("")
						&& !passwordET.getText().toString().trim().equals("")) {
					boolean info = login(userIdET.getText().toString().trim(),
							passwordET.getText().toString().trim());
					Toast.makeText(SettingsActivity.this,
							info ? "登录成功" : "登录失败", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(SettingsActivity.this, "输入有误，请重试",
							Toast.LENGTH_SHORT).show();
				}
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	// 向服务器发送登录请求,返回登录是否成功
	// 其次，判断该用户是否为新用户和是否更改了密码，是则更新数据库，
	boolean login(String id, String password) {
		String url = "http://172.16.41.161/Mobile/login";
		String param = "loginName=" + id + "&password=" + password;
		
		LinkToWeb task = new LinkToWeb();
		String response = task.doInBackground(url, param);

		if (response.equals("登录成功")) {
			session[0] = id;
			session[1] = password;
			// 该用户是否为新用户，是则放入数据库
			updateDatabase(id, password);
		} else // 密码不对 ，重新登录
		{
			Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
		}
		return response.equals("登录成功");
	}

	// 根据登录信息判断是否更新数据库（针对新用户和更改了密码的情况）
	void updateDatabase(String id, String password) {
		//标记是否新用户
		boolean flag = true;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUserid().equals(id)) {
				flag = false;
				// 若密码有更改，更新数据库
				if (!list.get(i).getPassword().equals(password)) {
					UserDatabaseHelper db = new UserDatabaseHelper(this,
							UserDatabaseHelper.DATABASE_NAME);
					//更改密码
					list.get(i).setPassword(password);
					db.updateUser(id, list.get(i));
					db.close();
				}
				break;
			}
		}
		if (flag) {
			UserDatabaseHelper db = new UserDatabaseHelper(this,
					UserDatabaseHelper.DATABASE_NAME);
			User user = new User();
			user.setUserid(id);
			user.setPassword(password);
			user.setUsername("");
			db.insertUser(user);
			db.close();
		}
	}

	// 向服务器发送注册请求，若成功则写入数据库，否则 弹出错误提示,并重新弹出注册Dialog让用户重新注册
	void sentRegisteredInfo() {
		String url = "http://172.16.41.161/Mobile/signUp";
		String param = "user.loginName=" + registeredInfo[0]
				+ "&user.password=" + registeredInfo[1] + "&user.name="
				+ registeredInfo[3];

		LinkToWeb task = new LinkToWeb();
		String response = task.doInBackground(url, param);

		Toast.makeText( this, "注册" + (Boolean.parseBoolean(response) ? "成功" : "失败"
								+ response), Toast.LENGTH_LONG).show();
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
			db.close();
		}
	}

	// 弹出注册Dialog 根据输入信息发送注册请求
	@SuppressLint("ShowToast")
	void showRegisteredDiolag() {

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.register_layout,
				(ViewGroup) findViewById(R.id.register_dialog));

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
				}
			}
		});
		builder.setNegativeButton("取消", null);
		builder.create().show();
	}

	// 后台连接服务器的类,先检测网络状态，然后在发送数据
	class LinkToWeb extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... allUrl) {
			String result = null;
			ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cwjManager.getActiveNetworkInfo();
			if (info == null || !info.isAvailable()) {
				Toast.makeText(SettingsActivity.this, "没检测到网络，请先联网~",
						Toast.LENGTH_LONG).show();
				return "false";
			}
			System.out.println(allUrl[0] + "  " + allUrl[1]);
			result = GetPostUtil.sendPost(allUrl[0], allUrl[1]); // 发送数据至服务器
			return result;
		}
	};

	// 弹出提示框 用于显示其他函数执行过程中返回的提示信息
	void showMessageDiolog(String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("提示~");
		builder.setMessage(info).setCancelable(false)
				.setPositiveButton("确认", null);
		builder.create().show();
	}
}
