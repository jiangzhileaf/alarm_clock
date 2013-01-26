package com.killerban.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class GetUpInfo implements Serializable {

	private int id;
	private int year;
	private int month;
	private int day;
	private long time;
	private int level;
	private boolean success;

	public GetUpInfo() {
		this.year=2013;
		this.month=1;
		this.day=21;
		this.time=0;
		this.success=true;
		this.level=1;
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

}
