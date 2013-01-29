package com.killerban.model;

import java.io.Serializable;

import android.database.Cursor;

import com.killerban.database.GetUpDatabaseHelper;

@SuppressWarnings("serial")
public class GetUpInfo implements Serializable {

	private int id;
	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private long time;
	private int level;
	private boolean success;

	public GetUpInfo() {
		this.year = 2013;
		this.month = 1;
		this.day = 21;
		this.time = 0;
		this.success = true;
		this.level = 1;
	}

	//返回起床信息的格式化字符串
	public String showGetUpInfo(Cursor cursor)
	{
		String s="";
		s += Integer.parseInt(cursor.getString(cursor
				.getColumnIndex(GetUpDatabaseHelper.YEAR))) + "年";
		s += Integer.parseInt(cursor.getString(cursor
				.getColumnIndex(GetUpDatabaseHelper.MONTH))) + "月";
		s += Integer.parseInt(cursor.getString(cursor
				.getColumnIndex(GetUpDatabaseHelper.DAY))) + "日 \n";
		s += "等级："
				+ Integer.parseInt(cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.LEVEL)));
		s += "\n耗时："
				+ Integer.parseInt(cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.TIME))) + "秒\n";
		return s;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

}
