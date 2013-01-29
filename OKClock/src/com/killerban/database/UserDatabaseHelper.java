package com.killerban.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.killerban.model.User;

public class UserDatabaseHelper extends SQLiteOpenHelper {

	private final static String TAG="UserDatabaseHelper";
	
	private static final int VERSION = 1;
	public final static String DATABASE_NAME = "user_db";
	public static final String TABLE_NAME = "user";

	public final static String USERID = "userid";
	public final static String PASSWORD = "password";
	public final static String USERNAME = "username";

	// 必须要有以下构造函数
	public UserDatabaseHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public UserDatabaseHelper(Context context, String name, int version) {
		this(context, name, null, version);
		// TODO Auto-generated constructor stub
	}

	public UserDatabaseHelper(Context context, String name) {
		this(context, name, VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.i(TAG,"create a user DataBase");
		String sql = "drop table if exists " + TABLE_NAME;
		db.execSQL(sql);
		db.execSQL("create table " + TABLE_NAME
				+ "(userid varchar(255) primary key," + PASSWORD
				+ " varchar(255) not null," + USERNAME + " varchar(255) not null)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		String sql = "drop table if exists " + TABLE_NAME;
		db.execSQL(sql);
	}

	public long insertUser(User info) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(USERID, info.getUserid());
		values.put(PASSWORD, info.getPassword());
		values.put(USERNAME, info.getUsername());
		long result = db.insert(TABLE_NAME, null, values);
		db.close();
		return result;
	}

	public Cursor selectUser() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_NAME, null, null, null, null, null, null);
		return cursor;
	}

	public Cursor getUser(String id) {
		SQLiteDatabase db = this.getReadableDatabase();
		String where = USERID + "=?";
		String[] whereValues = { id };
		Cursor cursor = db.query(TABLE_NAME, null, where, whereValues, null,
				null, null);
		return cursor;
	}

	public void deleteUser(String id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = USERID + "=?";
		String[] whereValues = { id };
		db.delete(TABLE_NAME, where, whereValues);
		db.close();
	}

	public int updateUser(String id, User info) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = USERID + "=?";
		String[] whereValues = { id };
		ContentValues values = new ContentValues();
		values.put(USERID, info.getUserid());
		values.put(PASSWORD, info.getPassword());
		values.put(USERNAME, info.getUsername());

		int result = db.update(TABLE_NAME, values, where, whereValues);
		db.close();
		return result;
	}
}
