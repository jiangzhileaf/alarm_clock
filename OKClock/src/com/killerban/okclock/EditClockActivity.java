package com.killerban.okclock;

import java.util.Calendar;

import com.killerban.database.DatabaseHelper;
import com.killerban.model.ClockParameter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

public class EditClockActivity extends Activity {

	public final static int REQUEST_CODE = 2;
	public final static int RESULT_CODE = 1;
	private final static int TIME_PICKER_ID = 1;
	private ClockParameter param;
	private Button clockNameButton; // ��������
	private Button clockCircleButton; // ��������
	private Button vibrateButton; // ����ʱ�Ƿ���
	private Button saveButton; // ����
	private Button deleteButton; // ɾ��
	private Button timeButton; // ��������ʱ�䰴ť
	private Button levelButton; // ����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_clock);
		timeButton = (Button) findViewById(R.id.ring_time);
		saveButton = (Button) findViewById(R.id.save);
		clockNameButton = (Button) findViewById(R.id.clock_tag);
		clockCircleButton = (Button) findViewById(R.id.clock_repeat);
		levelButton = (Button) findViewById(R.id.clock_model);
		vibrateButton = (Button) findViewById(R.id.vibrate);
		deleteButton = (Button) findViewById(R.id.delete_clock);

		timeButton.setOnClickListener(listener);
		saveButton.setOnClickListener(listener);
		clockNameButton.setOnClickListener(listener);
		clockCircleButton.setOnClickListener(listener);
		levelButton.setOnClickListener(listener);
		vibrateButton.setOnClickListener(listener);
		deleteButton.setOnClickListener(listener);

		param = (ClockParameter) getIntent().getExtras().getSerializable(
				"clock");
		System.out.println(param.getId() + " " + param.isIsnew());
		initButtonText(); // ��ʼ����ť���ı���Ϣ

	}

	// ��ʼ��������ť���ı���Ϣ
	void initButtonText() {
		timeButton.setText("����ʱ��: "
				+ param.getHour()
				+ ":"
				+ (param.getMinute() > 9 ? param.getMinute() : ("0" + param
						.getMinute())));
		clockNameButton.setText("��������: " + param.getName());
		clockCircleButton.setText("��������: "
				+ MainActivity.getRepeatInfo(param.getRepeat()));
		levelButton.setText("�����Ѷ�: "
				+ (param.getLevel() == 1 ? "��-�ӷ�" : "����-�˷�"));
		vibrateButton.setText(param.isIsvabrate() ? "��: ����" : "��: �ر�");
	}

	View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.ring_time:
				showDialog(TIME_PICKER_ID);
				break;
			case R.id.save:
				param.setIsopen(true);
				callBack(true);
				finish();
				break;
			case R.id.delete_clock: // ɾ�����ӣ�����ȷ�϶Ի���
				showDeleteDialog();
				break;
			case R.id.clock_model:
				showLevelDialog(); // ѡ����Ŀ�����Ѷ�
				break;
			case R.id.clock_tag:
				showClockTagDialog();
				break;
			case R.id.clock_repeat:
				setRepeatDay();
				break;
			case R.id.vibrate:
				if (param.isIsvabrate()) // ���ڿ���״̬��ת��Ϊ�ر�
				{
					vibrateButton.setText("��: �ر�");
					param.setIsvabrate(false);
				} else // ���ڹر�״̬��ת��Ϊ����
				{
					vibrateButton.setText("��: ����");
					param.setIsvabrate(true);
				}
				break;
			}
		}
	};

	void showDeleteDialog() {
		new AlertDialog.Builder(this).setTitle("ȷ��Ҫɾ����")
				.setPositiveButton("ȷ��", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Toast.makeText(EditClockActivity.this, "������ɾ��...",
								Toast.LENGTH_LONG).show();
						// �����Ӳ����½������� ���Ѿ������ݿ� ��ɾ��
						if (!param.isIsnew()) {
							DatabaseHelper db = new DatabaseHelper(
									EditClockActivity.this,
									DatabaseHelper.TABLE_NAME);
							System.out.println("����Id=" + param.getId());
							db.deleteOKColock(String.valueOf(param.getId()));
						}
						callBack(false);
						finish();
					}
				}).setNegativeButton("ȡ��", null).show();
	}

	void setRepeatDay() {
		Intent intent = new Intent(EditClockActivity.this,
				WeekRepeatActivity.class);
		intent.putExtra("repeat", param.getRepeat());
		startActivityForResult(intent, REQUEST_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE
				&& resultCode == WeekRepeatActivity.RESULT_CODE) {
			param.setRepeat(data.getBooleanArrayExtra("repeat"));
			clockCircleButton.setText("��������:"
					+ MainActivity.getRepeatInfo(param.getRepeat()));

		}
	}

	// �����������Ƶ�Dialog
	void showClockTagDialog() {
		final EditText editText = new EditText(this); // Ϊ��Ҫ��final?
		new AlertDialog.Builder(this).setTitle("��������������")
				.setIcon(android.R.drawable.ic_dialog_info).setView(editText)
				.setPositiveButton("ȷ��", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						param.setName(editText.getText().toString());
						clockNameButton.setText("��������:" + param.getName());
					}
				}).setNegativeButton("ȡ��", null).show();
	}

	void showLevelDialog() {
		new AlertDialog.Builder(this)
				.setTitle("��ѡ���Ѷ�")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(new String[] { "��:�ӷ�", "����:�˷�" },
						param.getLevel() - 1,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								param.setLevel(which + 1);
								levelButton
										.setText("�����Ѷ�: "
												+ ((which + 1) == 1 ? "��-�ӷ�"
														: "����-�˷�"));
								dialog.dismiss();
							}
						}).setNegativeButton("ȡ��", null).show();
	}

	void callBack(boolean save) {
		Intent intent = new Intent();
		intent.putExtra("save", save);
		intent.putExtra("clock", param);
		setResult(RESULT_CODE, intent);
	}

	// ���ѡ��ʱ������ʱ����������ʱ��
	TimePickerDialog.OnTimeSetListener onTimeListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			param.setHour(hourOfDay);
			param.setMinute(minute);
			timeButton.setText("����ʱ�䣺" + hourOfDay + ":"
					+ (minute > 9 ? minute : ("0" + minute)));
		}
	};

	// ʱ��ѡ��򱻵���ʱ��Ӧ����ʼ��Ĭ��ʱ��Ϊ��ǰʱ��
	protected Dialog onCreateDialog(int id) {
		Calendar currentTime = Calendar.getInstance();
		return new TimePickerDialog(this, onTimeListener, param.getHour(),
				param.getMinute(), false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_edit_clock1, menu);
		return true;
	}

}
