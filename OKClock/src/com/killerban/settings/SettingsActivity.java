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
 * @author Ban ����Activity �����¹��ܣ�Ԥ�������������� ��¼���ϴ����ݣ���ʱ��PK ������������鿴������ʱ������
 */
public class SettingsActivity extends Activity {
	private final static String TAG = "SettingActivity";

	private Button volumeUpButton; // Ԥ����������������
	private Button volumeDownButton; // Ԥ����������������
	private Button showGetUpDataButton; // չʾ����Ϣ
	private Button uploadButton; // �ϴ�����
	private Button rankListButton; // ���а� ���һ����ʱ������а�
	private Button freebackButton; // �������
	private Button registerButton; // ע��
	private Button loginButton; // ��¼
	private Button upgradeButton; // ������
	private String[] registeredInfo = new String[] { "", "", "", "" }; // ע����Ϣ
	private ArrayList<User> list = new ArrayList<User>();
	private String[] session = new String[] { "", "" }; // ��ǰ��¼���˺���Ϣ,�ֱ�Ϊ�˺ź�����

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

		// ��Ϊ��Activity��Ҫ������ȡ���ݿ⣬�����Ƚ����ȡ��Arraylist��
		getUserDatabaseInfo();
	}

	// ��ȡ���ݿ���Ϣ��������Arraylist�У�Ϊ�Ժ󷽱��ȡ
	public void getUserDatabaseInfo() {

		UserDatabaseHelper db = new UserDatabaseHelper(SettingsActivity.this,
				UserDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectUser();
		User user = null;
		while (cursor.moveToNext()) {
			user = new User();
			// ����User����ķ����������ݿ���Ϣת��ֵ����Ӧ�Ķ�������
			user.translateFromDB(cursor);
			list.add(user);
		}
		cursor.close();
		db.close();
	}

	// ������ť�ļ�����
	View.OnClickListener listener = new View.OnClickListener() {
		public void onClick(View v) {
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			switch (v.getId()) {
			case R.id.volumeup:// ��������
				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI
								| AudioManager.FLAG_PLAY_SOUND);
				break;
			case R.id.volumedown:// ��������
				am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI
								| AudioManager.FLAG_PLAY_SOUND);// ��������
				break;
			case R.id.statistics: // չʾ����
				Intent intent = new Intent(SettingsActivity.this,
						DataShowActivity.class);
				startActivity(intent);
				break;
			case R.id.upload: // �ϴ���ʱ������
				uploadData();
				break;
			case R.id.login: // ��¼
				showLoginDialog();
				break;
			case R.id.register: // ע��
				showRegisteredDiolag();
				break;
			case R.id.feedback: // �������
				showFeedbackDialog();
				break;
			case R.id.rankList: // ��ʱ��pk
				pkWithOther();
				break;
			case R.id.update: // �������
				upDateSoftware();
				break;
			}
		}
	};

	// ���ݰ汾���ж��Ƿ�Ϊ���°棬�����������أ������������·���,�Ӻ�̨����
	void upDateSoftware() {
		String url = "http://172.16.41.161/Mobile/getNewestVersion";
		String param = "";
		String appVersion = "";

		LinkToWeb task = new LinkToWeb();
		String newVersion = task.doInBackground(url, param);

		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			appVersion = info.versionName; // �汾����versionCodeͬ��
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		// �����ӵ����磬��ȡ�õ��汾��
		if (!newVersion.equals("false") && !newVersion.equals("������")) {
			if (newVersion.equals(appVersion)) // �汾����ͬ
			{
				Toast.makeText(SettingsActivity.this, "�㵱ǰʹ�õ������°汾���������~",
						Toast.LENGTH_SHORT).show();
			} else {
				AlertDialog.Builder builder = new Builder(SettingsActivity.this);
				builder.setTitle("�㵱ǰ�汾Ϊ " + appVersion + " ,���°汾Ϊ "
						+ newVersion + ",�Ƿ��������°棿");
				builder.setPositiveButton("ȷ��",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// ������Ϣ��������
								Intent intent = new Intent(
										SettingsActivity.this,
										UpgradeService.class);
								startService(intent);
								Toast.makeText(SettingsActivity.this,
										"���л�����̨����,��ɺ�ᵯ������~",
										Toast.LENGTH_SHORT).show();
								dialog.dismiss();
							}
						});
				builder.setNegativeButton("ȡ��", null);
				builder.create().show();
			}
		} else {
			Toast.makeText(SettingsActivity.this, "������", Toast.LENGTH_SHORT)
					.show();
		}
	}

	// �������Dialog
	void showFeedbackDialog() {
		final EditText editText = new EditText(this);
		editText.setTextSize(20);
		AlertDialog.Builder builder = new Builder(SettingsActivity.this);
		builder.setTitle("��ʲô��������˵~~");
		builder.setView(editText);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ������Ϣ��������
				sentFeedback(editText.getText().toString());
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.create().show();
	}

	// �������������������
	void sentFeedback(String info) {
		String url = "http://172.16.41.161/Mobile/receiveAdvice";
		String param = "advice=" + info;

		LinkToWeb task = new LinkToWeb();
		String response = task.doInBackground(url, param); // ����������������
															// ����������Ϣ������response��
		showMessageDiolog(response);
	}

	// ȡ���һ����ʱ��ͱ��˽���PK
	void pkWithOther() {
		GetUpDatabaseHelper db = new GetUpDatabaseHelper(SettingsActivity.this,
				GetUpDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectGetUp();
		if (cursor.getCount() != 0) // ���ݿ�����ʱ��ļ�¼
		{
			cursor.moveToLast(); // ȡ���һ�μ�¼
			String time = cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.TIME));

			// ��������������󣬲���������Ϣ������response��
			String url = "http://172.16.41.161/Mobile/getOrder";
			String param = "duration=" + time;

			LinkToWeb task = new LinkToWeb();
			String response = task.doInBackground(url, param);

			showMessageDiolog(response);
		} else // ���ݿ�Ϊ�գ��޷�
		{
			showMessageDiolog("���ݿ�Ϊ�գ��޼�¼�������PK~~"); // ������ʾ
		}
		cursor.close();
		db.close();
	}

	// �����û�������Ϣ�����������ڷ���֮ǰ�������ݿ��Ƿ�����û��˺ţ�
	// �����ڣ���ѡ���Ѿ���¼���˺ŵ�¼������¼�ɹ�����̨�ϴ����ݣ�ʧ���򵯳���ʾ
	void uploadData() {
		if (list.size() == 0) // ���û���Ϣ ��ע��
		{
			Toast.makeText(SettingsActivity.this, "�㻹ûע�ᣬ����ע���~",
					Toast.LENGTH_SHORT).show();
		} else {
			if (session[0].equals("")) {
				Toast.makeText(SettingsActivity.this, "�㻹û�е�¼�����ȵ�¼~",
						Toast.LENGTH_SHORT).show();
			} else {
				String loginId = session[0];
				String loginPSW = session[1];
				if (login(loginId, loginPSW)) // ��¼�ɹ� �û���������ƥ��
				{
					showMessageDiolog("OK����̨����Ϊ���ϴ����ݵ�������~~");
					Intent intent = new Intent(this, SentDataService.class);
					intent.putExtra("id", loginId);
					intent.putExtra("password", loginPSW);
					startService(intent);
				} else {
					Toast.makeText(SettingsActivity.this, "��¼��Ϣ�д��������µ�¼~",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	// ��¼�ĶԻ��� �������ݿ�ȡ���׸��˺ţ��Զ����
	void showLoginDialog() {

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.login_layout,
				(ViewGroup) findViewById(R.id.login_dialog));
		final EditText userIdET = (EditText) layout
				.findViewById(R.id.login_userid_eidt);
		final EditText passwordET = (EditText) layout
				.findViewById(R.id.login_password_edit);
		// ���������ַ�Ϊ�������� ���������*
		passwordET.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);

		// �����ݿ������ݣ��������������ı������
		if (list.size() != 0) {
			userIdET.setText(list.get(0).getUserid());
			passwordET.setText(list.get(0).getPassword());
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("�� ¼��");
		builder.setView(layout);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				// ���͵�¼����
				if (!userIdET.getText().toString().trim().equals("")
						&& !passwordET.getText().toString().trim().equals("")) {
					boolean info = login(userIdET.getText().toString().trim(),
							passwordET.getText().toString().trim());
					Toast.makeText(SettingsActivity.this,
							info ? "��¼�ɹ�" : "��¼ʧ��", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(SettingsActivity.this, "��������������",
							Toast.LENGTH_SHORT).show();
				}
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.create().show();
	}

	// ����������͵�¼����,���ص�¼�Ƿ�ɹ�
	// ��Σ��жϸ��û��Ƿ�Ϊ���û����Ƿ���������룬����������ݿ⣬
	boolean login(String id, String password) {
		String url = "http://172.16.41.161/Mobile/login";
		String param = "loginName=" + id + "&password=" + password;
		
		LinkToWeb task = new LinkToWeb();
		String response = task.doInBackground(url, param);

		if (response.equals("��¼�ɹ�")) {
			session[0] = id;
			session[1] = password;
			// ���û��Ƿ�Ϊ���û�������������ݿ�
			updateDatabase(id, password);
		} else // ���벻�� �����µ�¼
		{
			Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
		}
		return response.equals("��¼�ɹ�");
	}

	// ���ݵ�¼��Ϣ�ж��Ƿ�������ݿ⣨������û��͸���������������
	void updateDatabase(String id, String password) {
		//����Ƿ����û�
		boolean flag = true;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getUserid().equals(id)) {
				flag = false;
				// �������и��ģ��������ݿ�
				if (!list.get(i).getPassword().equals(password)) {
					UserDatabaseHelper db = new UserDatabaseHelper(this,
							UserDatabaseHelper.DATABASE_NAME);
					//��������
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

	// �����������ע���������ɹ���д�����ݿ⣬���� ����������ʾ,�����µ���ע��Dialog���û�����ע��
	void sentRegisteredInfo() {
		String url = "http://172.16.41.161/Mobile/signUp";
		String param = "user.loginName=" + registeredInfo[0]
				+ "&user.password=" + registeredInfo[1] + "&user.name="
				+ registeredInfo[3];

		LinkToWeb task = new LinkToWeb();
		String response = task.doInBackground(url, param);

		Toast.makeText( this, "ע��" + (Boolean.parseBoolean(response) ? "�ɹ�" : "ʧ��"
								+ response), Toast.LENGTH_LONG).show();
		// ע��ɹ� д�����ݿ�
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

	// ����ע��Dialog ����������Ϣ����ע������
	@SuppressLint("ShowToast")
	void showRegisteredDiolag() {

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.register_layout,
				(ViewGroup) findViewById(R.id.register_dialog));

		// �ֱ�Ϊ �˺ţ����룬����ȷ�ϣ��ǳ� ���ı������ �� ���������Ϣ�Ƿ�����İ�ť
		final EditText userid = (EditText) layout
				.findViewById(R.id.userid_eidt);
		final EditText password = (EditText) layout
				.findViewById(R.id.password_edit);
		final EditText pswagain = (EditText) layout
				.findViewById(R.id.password_again_edit);
		final EditText username = (EditText) layout
				.findViewById(R.id.username_edit);
		final Button check = (Button) layout.findViewById(R.id.check_info);

		// ���ע����Ϣ�Ƿ���Ϲ淶
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
					Toast.makeText(SettingsActivity.this, "�û��˺Ų���Ϊ��",
							Toast.LENGTH_SHORT).show();
					userid.selectAll();
				}
				if (registeredInfo[1].length() < 6) {
					ok = false;
					Toast.makeText(SettingsActivity.this, "���볤������ 6 λ������������",
							Toast.LENGTH_SHORT).show();
					password.selectAll();
				}
				if (!registeredInfo[1].equals(registeredInfo[2])) {
					ok = false;
					Toast.makeText(SettingsActivity.this, "������������벻һ�£�����������",
							Toast.LENGTH_SHORT).show();
					pswagain.selectAll();
				}
				if (registeredInfo[3].length() < 2) {
					ok = false;
					Toast.makeText(SettingsActivity.this, "�û��ǳƳ���С�� 2 λ������������",
							Toast.LENGTH_SHORT).show();
					username.selectAll();
				}
				if (ok)
					Toast.makeText(SettingsActivity.this, "��Ϣ��д��ȷ�����Ե��ȷ����~",
							Toast.LENGTH_SHORT).show();
			}
		});
		AlertDialog.Builder builder = new Builder(SettingsActivity.this);
		builder.setTitle("       ���û�ע��   ");
		builder.setView(layout);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					registeredInfo[0] = (userid.getText().toString());
					registeredInfo[1] = (password.getText().toString());
					registeredInfo[2] = (pswagain.getText().toString());
					registeredInfo[3] = (username.getText().toString());

					// ����ע����Ϣ��������
					sentRegisteredInfo();
					dialog.dismiss();
				} catch (Exception e) {
					Toast.makeText(SettingsActivity.this, "û������Ҫ��ȷ�ϰ�������һ�°�~",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.create().show();
	}

	// ��̨���ӷ���������,�ȼ������״̬��Ȼ���ڷ�������
	class LinkToWeb extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... allUrl) {
			String result = null;
			ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cwjManager.getActiveNetworkInfo();
			if (info == null || !info.isAvailable()) {
				Toast.makeText(SettingsActivity.this, "û��⵽���磬��������~",
						Toast.LENGTH_LONG).show();
				return "false";
			}
			System.out.println(allUrl[0] + "  " + allUrl[1]);
			result = GetPostUtil.sendPost(allUrl[0], allUrl[1]); // ����������������
			return result;
		}
	};

	// ������ʾ�� ������ʾ��������ִ�й����з��ص���ʾ��Ϣ
	void showMessageDiolog(String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("��ʾ~");
		builder.setMessage(info).setCancelable(false)
				.setPositiveButton("ȷ��", null);
		builder.create().show();
	}
}
