package com.killerban.editclock;

import com.killerban.database.DatabaseHelper;
import com.killerban.model.ClockParameter;
import com.killerban.okclock.R;
import com.killerban.okclock.R.id;
import com.killerban.okclock.R.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
	private Button musicButton;// ѡ������

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
		musicButton = (Button) findViewById(R.id.clock_sound);

		timeButton.setOnClickListener(listener);
		saveButton.setOnClickListener(listener);
		clockNameButton.setOnClickListener(listener);
		clockCircleButton.setOnClickListener(listener);
		levelButton.setOnClickListener(listener);
		vibrateButton.setOnClickListener(listener);
		deleteButton.setOnClickListener(listener);
		musicButton.setOnClickListener(listener);

		// �õ����ӵ��࣬��ߴ洢�����ӵ�������ϸ��Ϣ
		param = (ClockParameter) getIntent().getExtras().getSerializable(
				"clock");
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
				+ param.getRepeatInfo(param.getRepeat()));
		levelButton.setText("�����Ѷ�: "
				+ (param.getLevel() == 1 ? "��-�ӷ�" : "����-�˷�"));
		vibrateButton.setText(param.isIsvabrate() ? "��: ����" : "��: �ر�");
		String s = param.getAudiotype();
		System.out.println("getAudiotype:" + s);
		if (s.equals("default"))
			s = "Ĭ��";
		else {
			String[] name = s.split("/");
			s = name[name.length - 1];
		}
		musicButton.setText("����: " + s);
	}

	// Ϊ������ť�󶨼�����
	View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.ring_time: // �޸�����ʱ��
				showDialog(TIME_PICKER_ID);
				break;
			case R.id.save: // ��������
				callBack(true);
				finish();
				break;
			case R.id.delete_clock: // ɾ�����ӣ�����ȷ�϶Ի���
				showDeleteDialog();
				break;
			case R.id.clock_model:
				showLevelDialog(); // ѡ����Ŀ�����Ѷ�
				break;
			case R.id.clock_tag: // �޸���������
				showClockTagDialog();
				break;
			case R.id.clock_sound: // ѡ����������
				selectMusic();
				break;
			case R.id.clock_repeat: // ���������ظ�����
				setRepeatDay();
				break;
			case R.id.vibrate: // �������߹ر����ӵ��𶯹���
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

	// ѡ������������Dialog,��ѡ��Ĭ�� ������Ϊ���Ĭ������
	// ѡ���Զ���Ļ�������SD�����ļ��б�����SelectMusicActivity��������ѡ����ϸ�ʽ����Ϊ����
	void selectMusic() {
		int index = param.getAudiotype().equals("default") ? 0 : 1;
		new AlertDialog.Builder(this)
				.setTitle("  ѡ����������   ")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(new String[] { "Ĭ������", "�Զ�������" }, index,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == 0) {
									param.setAudiotype("default");
									musicButton.setText("����: Ĭ��");
								} else {
									Intent intent = new Intent(
											EditClockActivity.this,
											SelectMusicActivity.class);
									startActivityForResult(intent, REQUEST_CODE);
								}
								dialog.dismiss();
							}
						}).setNegativeButton("ȡ��", null).show();
	}

	// ɾ�����ӵ���ʾ�� ���ȷ����ɾ������
	void showDeleteDialog() {
		new AlertDialog.Builder(this).setTitle("ȷ��Ҫɾ����")
				.setPositiveButton("ȷ��", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Toast.makeText(EditClockActivity.this, "������ɾ��...",
								Toast.LENGTH_LONG).show();
						// �����Ӳ����½������� ���Ѿ������ݿ� �򷵻�ɾ������
						if (!param.isIsnew()) {
							
							// false����ɾ��������true����洢����
							callBack(false);
							finish();
						} else {		//���������ӣ�����ص���ֱ�ӽ���
							finish();
							return;
						}
					}
				}).setNegativeButton("ȡ��", null).show();
	}

	// ���������ظ����� ��һ����ĩ
	void setRepeatDay() {
		Intent intent = new Intent(EditClockActivity.this,
				WeekRepeatActivity.class);
		intent.putExtra("repeat", param.getRepeat());
		startActivityForResult(intent, REQUEST_CODE);
	}

	// ��������������Activity���ص�����
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE
				&& resultCode == WeekRepeatActivity.RESULT_CODE) {
			param.setRepeat(data.getBooleanArrayExtra("repeat"));
			clockCircleButton.setText("��������:"
					+ param.getRepeatInfo(param.getRepeat()));
		}
		if (requestCode == REQUEST_CODE
				&& resultCode == SelectMusicActivity.RESULT_CODE) {

			String path = data.getExtras().getString("path");
			param.setAudiotype(path);
			String[] name = path.split("/");
			System.out.println("return ring" + path + " name "
					+ name[name.length - 1]);
			musicButton.setText("����:" + name[name.length - 1]);
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

	// �Ѷȵȼ�ѡ�� Dialog
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

	// ���ظ�MainActivity������,save��true����洢������false����ɾ������
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

	// ʱ��ѡ��򱻵���ʱ��Ӧ����ʼ��Ĭ��ʱ��Ϊ���ӵ�ǰ��ʱ��
	protected Dialog onCreateDialog(int id) {
		return new TimePickerDialog(this, onTimeListener, param.getHour(),
				param.getMinute(), false);
	}

}
