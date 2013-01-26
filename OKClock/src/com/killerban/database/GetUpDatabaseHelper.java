package com.killerban.database;

import com.killerban.model.GetUpInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class GetUpDatabaseHelper extends SQLiteOpenHelper {

	private static final int VERSION = 1;
	public final static String DATABASE_NAME = "getup_db";
	public static final String TABLE_NAME = "getup_info";

	public final static String ID = "id";
	public final static String YEAR = "year";
	public final static String MONTH = "month";
	public final static String DAY = "day";
	public final static String TIME = "time";
	public final static String LEVEL = "level";
	public final static String SUCCESS = "success";

	// 必须要有以下构造函数
	public GetUpDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public GetUpDatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
		// TODO Auto-generated constructor stub
	}

	public GetUpDatabaseHelper(Context context, String name) {
		this(context, name, VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		System.out.println("create a getup DataBase");
		String sql = "drop table if exists "+TABLE_NAME;
		db.execSQL(sql);
		db.execSQL("create table " + TABLE_NAME
				+ "(id integer primary key autoincrement," + YEAR
				+ " int not null," + MONTH + " int not null," + DAY
				+ " day not null," + TIME + " long not null," + LEVEL
				+ " int not null," + SUCCESS + " boolean)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		String sql = "drop table if exists " + TABLE_NAME;
		db.execSQL(sql);
	}

	public long insertGetUp(GetUpInfo info) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(YEAR, info.getYear());
		values.put(MONTH, info.getMonth());
		values.put(DAY, info.getDay());
		values.put(LEVEL, info.getLevel());
		values.put(TIME, info.getTime());
		values.put(SUCCESS, info.isSuccess());
		System.out.println("insert Database");
		long result = db.insert(TABLE_NAME, null, values);
		db.close();
		return result;
	}

	public Cursor selectGetUp() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_NAME, null, null, null, null, null, null);
		return cursor;
	}

	public Cursor getGetUp(String id) {
		SQLiteDatabase db = this.getReadableDatabase();
		String where = ID + "=?";
		String[] whereValues = { id };
		Cursor cursor = db.query(TABLE_NAME, null, where, whereValues, null,
				null, null);
		return cursor;
	}

	public void deleteGetUp(String id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = ID + "=?";
		String[] whereValues = { id };
		db.delete(TABLE_NAME, where, whereValues);
		db.close();
	}

	public int updateGetUp(String id, GetUpInfo info) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = ID + "=?";
		String[] whereValues = { id };
		ContentValues values = new ContentValues();
		values.put(YEAR, info.getYear());
		values.put(MONTH, info.getMonth());
		values.put(DAY, info.getDay());
		values.put(LEVEL, info.getLevel());
		values.put(TIME, info.getTime());
		values.put(SUCCESS, info.isSuccess());

		int result = db.update(TABLE_NAME, values, where, whereValues);
		db.close();
		return result;
	}
}
