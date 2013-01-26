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
	private Button volumeUpButton; // Ԥ����������������
	private Button volumeDownButton; // Ԥ����������������
	private Button showGetUpDataButton; // չʾ����Ϣ
	private Button uploadButton; // �ϴ�����
	private Button rankListButton; // ���а� ���һ����ʱ������а�
	private Button freebackButton; // �������
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
				Intent intent = new Intent(SettingsActivity.this,DataShowActivity.class);
				startActivity(intent);
				break;
			case R.id.upload: // �ϴ�����
				System.out.println("�ҵ���ˣ��� ");
				uploadData();
				break;
			case R.id.rankList: // PK
				pkWithOther();
				break;
			case R.id.feedback: // �������
				showFeedbackDialog();
				break;
			}
		}
	};

	void showFeedbackDialog() {
		final EditText editText = new EditText(this);
		editText.setTextSize(20);
		AlertDialog.Builder builder = new Builder(SettingsActivity.this);
		builder.setTitle("��ʲô��������˵~~");
		builder.setView(editText);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sentFeedback(editText.getText().toString());
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.create().show();
	}

	void sentFeedback(String info) {
		String url = "http://172.16.41.161:9000/Mobile/receiveAdvice";
		String param = "advice=" + info;
		String response = GetPostUtil.sendPost(url, param); // ����������������
															// ����������Ϣ������response��
		Toast.makeText(SettingsActivity.this, response, Toast.LENGTH_LONG)
				.show();
	}

	// ȡ���һ����ʱ��ͱ��˽���PK
	void pkWithOther() {
		GetUpDatabaseHelper db = new GetUpDatabaseHelper(SettingsActivity.this,
				GetUpDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectGetUp();
		cursor.moveToLast(); // ȡ���һ�μ�¼
		String time = cursor.getString(cursor
				.getColumnIndex(GetUpDatabaseHelper.TIME));
		System.out.println("PKTime=" + time);

		// ��������������󣬲���������Ϣ������response��
		String url = "http://172.16.41.161:9000/Mobile/getOrder";
		String param = "duration=" + time;
		String response = GetPostUtil.sendPost(url, param);

		Toast.makeText(SettingsActivity.this, response, Toast.LENGTH_LONG)
				.show();
		cursor.close();
		db.close();
	}

	// �����û�������Ϣ�����������ڷ���֮ǰ���ж��û��Ƿ��Ѿ���¼ ���򵯳�ע�����û��ĶԻ���
	void uploadData() {
		UserDatabaseHelper db = new UserDatabaseHelper(SettingsActivity.this,
				UserDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectUser();
		if (cursor.getCount() == 0) // ���û���Ϣ ��ע��
		{
			registeredInfo = new String[] { "", "", "", "" };
			registered();
			System.out.println("ע�����û�");
		} else {

		}
		cursor.close();
		db.close();
	}

	// �����������ע����Ϣ�����ɹ���д�����ݿ⣬���� ����������ʾ,�����µ���ע��Dialog���û�����ע��
	void sentRegisteredInfo() {
		String url = "http://172.16.41.161:9000/Mobile/signin";
		String param = "loginName=" + registeredInfo[0] + "&password1="
				+ registeredInfo[1] + "&password2=" + registeredInfo[2]
				+ "&name=" + registeredInfo[3];
		System.out.println(param);
		String response = GetPostUtil.sendPost(url, param); // ����������������
		Toast.makeText(SettingsActivity.this, response, Toast.LENGTH_LONG)
				.show();

		System.out.println(response + "\n" + response == "ע��ɹ�");
		// ע��ɹ� д�����ݿ�
		if (response.equals("ע��ɹ�")) {
			User user = new User();
			user.setUserid(registeredInfo[0]);
			user.setPassword(registeredInfo[1]);
			user.setUsername(registeredInfo[3]);
			UserDatabaseHelper db = new UserDatabaseHelper(
					SettingsActivity.this, UserDatabaseHelper.DATABASE_NAME);
			db.insertUser(user);
		} else {
			registered(); // ����ע��
		}

	}

	// �����Զ����ע��Dialog ����������Ϣ����ע������
	@SuppressLint("ShowToast")
	void registered() {

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
				registeredInfo[0] = (userid.getText().toString());
				registeredInfo[1] = (password.getText().toString());
				registeredInfo[2] = (pswagain.getText().toString());
				registeredInfo[3] = (username.getText().toString());
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
				if(ok)
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
				registeredInfo[0] = (userid.getText().toString());
				registeredInfo[1] = (password.getText().toString());
				registeredInfo[2] = (pswagain.getText().toString());
				registeredInfo[3] = (username.getText().toString());

				// ����ע����Ϣ��������
				sentRegisteredInfo();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.create().show();
	}

	// չʾ�û�����������Ϣ
	void showData() {

		GetUpDatabaseHelper dbHelper = new GetUpDatabaseHelper(
				SettingsActivity.this, GetUpDatabaseHelper.DATABASE_NAME);
		Cursor cursor = dbHelper.selectGetUp();
		while (cursor.moveToNext()) {
			String s = "";
			s += Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.YEAR))) + "��";
			s += Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.MONTH))) + "��";
			s += Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.DAY))) + "�� \n";
			s += "�ȼ���"
					+ Integer.parseInt(cursor.getString(cursor
							.getColumnIndex(GetUpDatabaseHelper.LEVEL)));
			s += "\n��ʱ��"
					+ Integer.parseInt(cursor.getString(cursor
							.getColumnIndex(GetUpDatabaseHelper.TIME))) + "��\n";
			Toast.makeText(SettingsActivity.this, s, Toast.LENGTH_LONG).show();
		}
		cursor.close();
		dbHelper.close();
	}
}
