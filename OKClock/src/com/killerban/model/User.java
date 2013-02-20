package com.killerban.model;

import com.killerban.database.UserDatabaseHelper;

import android.database.Cursor;

public class User {
	private String userid;
	private String password;
	private String username;

	public User() {
	}

	public User(String userid, String password, String username) {
		this.userid = userid;
		this.password = password;
		this.username = username;
	}

	// 将数据库信息存至User对象
	public void translateFromDB(Cursor cursor) {
		this.userid = cursor.getString(cursor
				.getColumnIndex(UserDatabaseHelper.USERID));
		this.password = cursor.getString(cursor
				.getColumnIndex(UserDatabaseHelper.PASSWORD));
		this.username = cursor.getString(cursor
				.getColumnIndex(UserDatabaseHelper.USERNAME));
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
