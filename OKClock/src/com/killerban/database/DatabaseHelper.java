package com.killerban.database;

import com.killerban.model.ClockParameter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;
	public final static String DATABASE_NAME = "alarmclock_db";
	public static final String TABLE_NAME = "okclock";

	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String HOUR = "hour";
	public final static String MINUTE = "minute";
	public final static String LEVEL = "level";
	public final static String REPEAT = "repeat"; // 周一到周末
	public final static String ISOPEN = "isopen"; // 是否已经开启
	public final static String ISVIBRATE = "isvabrate"; // 是否震动
	public final static String AUDIOTYPE = "audiotype"; // 铃声类型
	public final static String ISNEW = "isnew"; // 铃声类型

	// 必须要有以下构造函数
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public DatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
		// TODO Auto-generated constructor stub
	}

	public DatabaseHelper(Context context, String name) {
		this(context, name, VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		System.out.println("create a DataBase");
		String partSQL = "";
		for (int i = 0; i <= 6; i++)
			// 周一到周末 分别为 repeat0 到 repeat6
			partSQL += REPEAT + i + " boolean,";

		db.execSQL("create table " + TABLE_NAME
				+ "(id integer primary key autoincrement," + NAME
				+ " varchar(20) ," + HOUR + " int not null," + MINUTE
				+ " int not null," + LEVEL + " int not null," + partSQL
				+ ISOPEN + " boolean," + ISVIBRATE + " boolean," + ISNEW
				+ " boolean," + AUDIOTYPE + " varchar(30))");
		System.out.println("create ok");
		db.execSQL("insert into " + TABLE_NAME
				+ " values(1,'undefine',8,0,1,1,1,1,1,1,1,1,0,1,0,'default')");
		db.execSQL("insert into " + TABLE_NAME
				+ " values(2,'undefine',9,0,1,1,1,1,1,1,1,1,0,1,0,'default')");
		System.out.println("insert ok");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		String sql = "drop table if exists " + TABLE_NAME;
		db.execSQL(sql);
	}

	public long insertOKColock(ClockParameter param) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		boolean[] repeat = param.getRepeat();
		values.put(HOUR, param.getHour());
		values.put(MINUTE, param.getMinute());
		values.put(NAME, param.getName());
		values.put(LEVEL, param.getLevel());
		for (int i = 0; i <= 6; i++)
			values.put(REPEAT + i, repeat[i]);
		values.put(ISOPEN, param.isIsopen());
		values.put(ISVIBRATE, param.isIsvabrate());
		values.put(ISNEW, param.isIsnew());
		values.put(AUDIOTYPE, "default");
		System.out.println("insert Database");
		long result = db.insert(TABLE_NAME, null, values);
		db.close();
		return result;
	}

	public Cursor selectOKColock() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_NAME, null, null, null, null, null, null);
		return cursor;
	}

	public Cursor getOKColock(String id) {
		SQLiteDatabase db = this.getReadableDatabase();
		String where = ID + "=?";
		String[] whereValues = { id };
		Cursor cursor = db.query(TABLE_NAME, null, where, whereValues, null,
				null, null);
		return cursor;
	}

	public void deleteOKColock(String id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = ID + "=?";
		String[] whereValues = { id };
		System.out.println("deleteSQL : delete from" + TABLE_NAME + " " + where
				+ "=" + id);
		int i=db.delete(TABLE_NAME, where, whereValues);
		System.out.println("delete result"+i);
		db.close();
	}

	public int updateOKColock(String id, ClockParameter param) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = ID + "=?";
		String[] whereValues = { id };
		ContentValues values = new ContentValues();
		boolean[] repeat = param.getRepeat();
		values.put(HOUR, param.getHour());
		values.put(MINUTE, param.getMinute());
		values.put(NAME, param.getName());
		values.put(LEVEL, param.getLevel());
		for (int i = 0; i <= 6; i++)
			values.put(REPEAT + i, repeat[i]);
		values.put(ISOPEN, param.isIsopen());
		values.put(ISVIBRATE, param.isIsvabrate());
		values.put(ISNEW, param.isIsnew());
		values.put(AUDIOTYPE, "default");

		int result = db.update(TABLE_NAME, values, where, whereValues);
		db.close();
		return result;
	}
}
