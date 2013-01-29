package com.killerban.settings;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.killerban.database.GetUpDatabaseHelper;
import com.killerban.model.ClockParameter;
import com.killerban.okclock.R;
import com.killerban.okclock.R.layout;

public class DataShowActivity extends ListActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_show);
		GetUpDatabaseHelper db = new GetUpDatabaseHelper(this,
				GetUpDatabaseHelper.DATABASE_NAME);

		Cursor cursor = db.selectGetUp();
		System.out.println("data show");
		String[] info = new String[cursor.getCount()];
		int i = 0;
		while (cursor.moveToNext()) {
			String s = "";
			s += cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.YEAR)) + "/";
			s += cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.MONTH)) + "/";
			s += cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.DAY)) + "  ";
			s += cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.HOUR)) + ":";
			if (cursor.getString(
					cursor.getColumnIndex(GetUpDatabaseHelper.MINUTE)).length() == 1)
				s += "0"
						+ cursor.getString(cursor
								.getColumnIndex(GetUpDatabaseHelper.MINUTE));
			else
				s += cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.MINUTE));
			if (cursor.getString(
					cursor.getColumnIndex(GetUpDatabaseHelper.LEVEL)).equals(
					"1"))
				s += "\n简单模式";
			else
				s += "\n困难模式";
			if (cursor.getString(
					cursor.getColumnIndex(GetUpDatabaseHelper.SUCCESS)).equals(
					"1"))
				s += " 起床成功\n";
			else
				s += " 起床失败\n";
			s += "挣扎时间:";
			long time = Long.parseLong(cursor.getString(cursor
					.getColumnIndex(GetUpDatabaseHelper.TIME)));
			s += ClockParameter.getFormatTime(time * 1000)+"\n";
			info[i++] = s;
		}
		cursor.close();
		db.close();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, info);
		setListAdapter(adapter);
	}
}
