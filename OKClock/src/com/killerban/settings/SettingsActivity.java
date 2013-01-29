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
 * @author Ban ����Activity �����¹��ܣ�Ԥ�������������� ��¼���ϴ����ݣ���ʱ��PK ������������鿴������ʱ������
 */
public class SettingsActivity extends Activity {
	private final static String TAG = "SettingActivity";
	private final static int REQUEST_CODE = 1;

	private Button volumeUpButton; // Ԥ����������������
	private Button volumeDownButton; // Ԥ����������������
	private Button showGetUpDataButton; // չʾ����Ϣ
	private Button uploadButton; // �ϴ�����
	private Button rankListButton; // ���а� ���һ����ʱ������а�
	private Button freebackButton; // �������
	private Button loginButton; // ��¼
	private Button upgradeButton;	//������
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

	// ������ť�ļ����� ������Ҫ������������õĹ��ܣ��ȵ����������Activity
	// ���ӳɹ����ٵ��ö�Ӧ�Ĺ���
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
			case R.id.upload: // �ϴ�����
				Log.i(TAG, "�ϴ�����");
				sentWebIntent(R.id.upload);		
				break;
			case R.id.rankList: // PK
				sentWebIntent(R.id.rankList);
				break;
			case R.id.feedback: // �������
				sentWebIntent(R.id.feedback);
				break;
			case R.id.login:
				sentWebIntent(R.id.login); // ��¼
				break;
			case R.id.update:		//������
				sentWebIntent(R.id.update);
				break;
			}
		}
	};

	// ������������״̬
	// �ڲ������ִ����Ӧ�Ĳ��������ݲ�����Id����
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
				Toast.makeText(SettingsActivity.this, "δ��⵽�������磬��������~",
						Toast.LENGTH_SHORT).show();
		}
	}

	// ��������״̬����ʱ��ִ�����²���
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
	
	//���ݰ汾���ж��Ƿ�Ϊ���°棬�����������أ������������·���,�Ӻ�̨����
	void upDateSoftware()
	{
		String url="http://172.16.41.161/Mobile/getNewestVersion";
		String param="";
		String appVersion="";
		String newVersion=GetPostUtil.sendPost(url, param);
		PackageManager manager = this.getPackageManager();
		try {
		        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
		        appVersion = info.versionName; // �汾����versionCodeͬ��
		} catch (NameNotFoundException e) {
		        e.printStackTrace();
		}
		
		if(newVersion.equals(appVersion))	//�汾����ͬ
		{
			Toast.makeText(SettingsActivity.this, "�㵱ǰʹ�õ������°汾���������~", Toast.LENGTH_SHORT).show();
		}
		else
		{
			AlertDialog.Builder builder = new Builder(SettingsActivity.this);
			builder.setTitle("�㵱ǰ�汾Ϊ "+appVersion+" ,���°汾Ϊ "+newVersion+",�Ƿ��������°棿");
			builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// ������Ϣ��������
					Intent intent = new Intent(SettingsActivity.this,UpgradeService.class);
					startService(intent);
					Toast.makeText(SettingsActivity.this, "���л�����̨����", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
				}
			});
			builder.setNegativeButton("ȡ��", null);
			builder.create().show();
			
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
		String response = GetPostUtil.sendPost(url, param); // ����������������
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
			String response = GetPostUtil.sendPost(url, param);

			showMessageDiolog(response);
		} else // ���ݿ�Ϊ�գ��޷�
		{
			showMessageDiolog("���ݿ�Ϊ�գ��޼�¼�������PK~~"); // ������ʾ
		}
		cursor.close();
		db.close();
	}

	// �����û�������Ϣ�����������ڷ���֮ǰ���ж��û��Ƿ��Ѿ���¼ ���򵯳�ע�����û��ĶԻ���
	// ���Ѿ�ע�����˺ź� ��¼ �ж������Ƿ���ȷ ������Է����û�������Ϣ�������� �����򵯳���¼��
	void uploadData() {
		UserDatabaseHelper db = new UserDatabaseHelper(SettingsActivity.this,
				UserDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectUser();
		if (cursor.getCount() == 0) // ���û���Ϣ ��ע��
		{

			Toast.makeText(SettingsActivity.this, "�㻹ûע�ᣬ����ע���~",
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
			if (login(loginId, loginPSW)) // ��¼�ɹ� �û���������ƥ��
			{
				showMessageDiolog("OK����̨����Ϊ���ϴ����ݵ�������~~");
				Intent intent = new Intent(this, SentDataService.class);
				startService(intent);
			} else {
				showLoginDialog(); // ������¼Dialog
			}
		}

	}

	// ��¼�ĶԻ���
	void showLoginDialog() {

		final UserDatabaseHelper db = new UserDatabaseHelper(
				SettingsActivity.this, UserDatabaseHelper.DATABASE_NAME);
		final Cursor cursor = db.selectUser();
		if (cursor.getCount() == 0) // δע�� ����ע��ҳ��
		{
			cursor.close();
			db.close();
			showRegisteredDiolag();
		} else {
			cursor.moveToLast(); // �Ѿ�ע�ᣬȡ�����ݿ���û���Ϣ
			final String loginId = cursor.getString(cursor
					.getColumnIndex(UserDatabaseHelper.USERID));
			final String loginPSW = cursor.getString(cursor
					.getColumnIndex(UserDatabaseHelper.PASSWORD));
			final String userName = cursor.getString(cursor
					.getColumnIndex(UserDatabaseHelper.USERNAME));

			final EditText passwordET = new EditText(this);
			// ���������ַ�Ϊ�������� ���������*
			passwordET.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			passwordET.setText(loginPSW);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("��¼��");
			builder.setView(passwordET);
			builder.setMessage("�û���:" + loginId)
					.setCancelable(false)
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// �����б���������û��޸Ĺ����룬�������ݿ�
									if (loginPSW != passwordET.getText()
											.toString()) {
										db.updateUser(loginId, new User(
												loginId, passwordET.getText()
														.toString(), userName));
									}
									// ����������͵�¼����
									login(loginId, passwordET.getText()
											.toString());
									dialog.dismiss();
									db.close();
								}
							});
			builder.setNegativeButton("ȡ��", null);
			builder.create().show();
		}
		cursor.close();
		db.close();
	}

	// ����������͵�¼����
	boolean login(String id, String password) {
		String url = "http://172.16.41.161/Mobile/login";
		String param = "loginName=" + id + "&password=" + password;
		String response = GetPostUtil.sendPost(url, param);
		Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
		if (!response.equals("��¼�ɹ�")) // ���벻�� �����µ�¼
		{
			showLoginDialog();
		}
		return response.equals("��¼�ɹ�");
	}

	// �����������ע���������ɹ���д�����ݿ⣬���� ����������ʾ,�����µ���ע��Dialog���û�����ע��
	void sentRegisteredInfo() {
		String url = "http://172.16.41.161/Mobile/signUp";
		String param = "user.loginName=" + registeredInfo[0]
				+ "&user.password=" + registeredInfo[1] + "&user.name="
				+ registeredInfo[3];
		String response = GetPostUtil.sendPost(url, param); // ����������������
		Toast.makeText(this,
				"ע��" + (Boolean.parseBoolean(response) ? "�ɹ�" : "ʧ��"),
				Toast.LENGTH_LONG).show();
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
			uploadData(); // ���Ե�¼����������
		} else {
			showRegisteredDiolag(); // ����ע��
		}

	}

	// ����ע��Dialog ����������Ϣ����ע������
	@SuppressLint("ShowToast")
	void showRegisteredDiolag() {

		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.registered_layout,
				(ViewGroup) findViewById(R.id.registed_dialog));

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
					keepDialog(dialog);
				}
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.create().show();
	}

	// ������ʾ�� ������ʾ��������ִ�й����з��ص���ʾ��Ϣ
	void showMessageDiolog(String info) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("��ʾ~");
		builder.setMessage(info).setCancelable(false)
				.setPositiveButton("ȷ��", null);
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
