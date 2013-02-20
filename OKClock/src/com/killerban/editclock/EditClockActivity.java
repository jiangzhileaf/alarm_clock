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
	private Button clockNameButton; // 闹钟名称
	private Button clockCircleButton; // 闹钟周期
	private Button vibrateButton; // 响铃时是否震动
	private Button saveButton; // 储存
	private Button deleteButton; // 删除
	private Button timeButton; // 设置闹铃时间按钮
	private Button levelButton; // 级别
	private Button musicButton;// 选择铃声

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

		// 得到闹钟的类，里边存储着闹钟的所有详细信息
		param = (ClockParameter) getIntent().getExtras().getSerializable(
				"clock");
		initButtonText(); // 初始化按钮的文本信息
	}

	// 初始化各个按钮的文本信息
	void initButtonText() {
		timeButton.setText("闹钟时间: "
				+ param.getHour()
				+ ":"
				+ (param.getMinute() > 9 ? param.getMinute() : ("0" + param
						.getMinute())));
		clockNameButton.setText("闹钟名称: " + param.getName());
		clockCircleButton.setText("闹钟周期: "
				+ param.getRepeatInfo(param.getRepeat()));
		levelButton.setText("闹钟难度: "
				+ (param.getLevel() == 1 ? "简单-加法" : "困难-乘法"));
		vibrateButton.setText(param.isIsvabrate() ? "震动: 开启" : "震动: 关闭");
		String s = param.getAudiotype();
		System.out.println("getAudiotype:" + s);
		if (s.equals("default"))
			s = "默认";
		else {
			String[] name = s.split("/");
			s = name[name.length - 1];
		}
		musicButton.setText("铃声: " + s);
	}

	// 为各个按钮绑定监听器
	View.OnClickListener listener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.ring_time: // 修改闹钟时间
				showDialog(TIME_PICKER_ID);
				break;
			case R.id.save: // 保存闹钟
				callBack(true);
				finish();
				break;
			case R.id.delete_clock: // 删除闹钟，弹出确认对话框
				showDeleteDialog();
				break;
			case R.id.clock_model:
				showLevelDialog(); // 选择题目的困难度
				break;
			case R.id.clock_tag: // 修改闹钟名称
				showClockTagDialog();
				break;
			case R.id.clock_sound: // 选择闹钟铃声
				selectMusic();
				break;
			case R.id.clock_repeat: // 设置闹钟重复日期
				setRepeatDay();
				break;
			case R.id.vibrate: // 开启或者关闭闹钟的震动功能
				if (param.isIsvabrate()) // 处于开启状态，转换为关闭
				{
					vibrateButton.setText("震动: 关闭");
					param.setIsvabrate(false);
				} else // 处于关闭状态，转换为开启
				{
					vibrateButton.setText("震动: 开启");
					param.setIsvabrate(true);
				}
				break;
			}
		}
	};

	// 选择闹钟铃声的Dialog,若选择默认 则设置为软件默认铃声
	// 选择自定义的话，弹出SD卡的文件列表（调用SelectMusicActivity），从中选择符合格式的作为铃声
	void selectMusic() {
		int index = param.getAudiotype().equals("default") ? 0 : 1;
		new AlertDialog.Builder(this)
				.setTitle("  选择闹铃铃声   ")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(new String[] { "默认铃声", "自定义铃声" }, index,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == 0) {
									param.setAudiotype("default");
									musicButton.setText("铃声: 默认");
								} else {
									Intent intent = new Intent(
											EditClockActivity.this,
											SelectMusicActivity.class);
									startActivityForResult(intent, REQUEST_CODE);
								}
								dialog.dismiss();
							}
						}).setNegativeButton("取消", null).show();
	}

	// 删除闹钟的提示框 点击确认则删除闹钟
	void showDeleteDialog() {
		new AlertDialog.Builder(this).setTitle("确定要删除？")
				.setPositiveButton("确定", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Toast.makeText(EditClockActivity.this, "闹钟已删除...",
								Toast.LENGTH_LONG).show();
						// 若闹钟不是新建的闹钟 即已经在数据库 则返回删除操作
						if (!param.isIsnew()) {
							
							// false代表删除操作，true代表存储操作
							callBack(false);
							finish();
						} else {		//若是新闹钟，无需回调，直接结束
							finish();
							return;
						}
					}
				}).setNegativeButton("取消", null).show();
	}

	// 设置闹钟重复周期 周一到周末
	void setRepeatDay() {
		Intent intent = new Intent(EditClockActivity.this,
				WeekRepeatActivity.class);
		intent.putExtra("repeat", param.getRepeat());
		startActivityForResult(intent, REQUEST_CODE);
	}

	// 从设置闹钟周期Activity返回的数据
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE
				&& resultCode == WeekRepeatActivity.RESULT_CODE) {
			param.setRepeat(data.getBooleanArrayExtra("repeat"));
			clockCircleButton.setText("闹钟周期:"
					+ param.getRepeatInfo(param.getRepeat()));
		}
		if (requestCode == REQUEST_CODE
				&& resultCode == SelectMusicActivity.RESULT_CODE) {

			String path = data.getExtras().getString("path");
			param.setAudiotype(path);
			String[] name = path.split("/");
			System.out.println("return ring" + path + " name "
					+ name[name.length - 1]);
			musicButton.setText("铃声:" + name[name.length - 1]);
		}
	}

	// 输入闹钟名称的Dialog
	void showClockTagDialog() {
		final EditText editText = new EditText(this); // 为何要用final?
		new AlertDialog.Builder(this).setTitle("请输入闹钟名称")
				.setIcon(android.R.drawable.ic_dialog_info).setView(editText)
				.setPositiveButton("确定", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						param.setName(editText.getText().toString());
						clockNameButton.setText("闹钟名称:" + param.getName());
					}
				}).setNegativeButton("取消", null).show();
	}

	// 难度等级选择 Dialog
	void showLevelDialog() {
		new AlertDialog.Builder(this)
				.setTitle("请选择难度")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setSingleChoiceItems(new String[] { "简单:加法", "困难:乘法" },
						param.getLevel() - 1,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								param.setLevel(which + 1);
								levelButton
										.setText("闹钟难度: "
												+ ((which + 1) == 1 ? "简单-加法"
														: "困难-乘法"));
								dialog.dismiss();
							}
						}).setNegativeButton("取消", null).show();
	}

	// 返回给MainActivity的数据,save若true代表存储操作，false代表删除操作
	void callBack(boolean save) {
		Intent intent = new Intent();
		intent.putExtra("save", save);
		intent.putExtra("clock", param);
		setResult(RESULT_CODE, intent);
	}

	// 完成选择时间后根据时间设置闹钟时间
	TimePickerDialog.OnTimeSetListener onTimeListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			param.setHour(hourOfDay);
			param.setMinute(minute);
			timeButton.setText("闹钟时间：" + hourOfDay + ":"
					+ (minute > 9 ? minute : ("0" + minute)));
		}
	};

	// 时间选择框被调用时响应，初始化默认时间为闹钟当前的时间
	protected Dialog onCreateDialog(int id) {
		return new TimePickerDialog(this, onTimeListener, param.getHour(),
				param.getMinute(), false);
	}

}
