package com.killerban.model;

import java.io.Serializable;

public class ClockParameter implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private int hour;
	private int minute;
	private int level;
	private String name;
	private boolean[] repeat;
	private boolean isopen;
	private boolean isnew;		//是否新建闹钟
	private boolean isvabrate;		//是否震动
	private String audiotype;		//响铃铃声类型
	
	public ClockParameter() {
		this.name = "Undefined";
		this.hour = 8;
		this.minute = 0;
		this.isopen = false;
		this.level = 1;
		this.isnew=false;
		this.isvabrate=true;
		this.repeat = new boolean[7];
		this.audiotype="";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean[] getRepeat() {
		return repeat;
	}

	public void setRepeat(boolean[] repeat) {
		this.repeat = repeat;
	}

	public boolean isIsopen() {
		return isopen;
	}

	public void setIsopen(boolean isopen) {
		this.isopen = isopen;
	}

	public boolean isIsnew() {
		return isnew;
	}

	public void setIsnew(boolean isnew) {
		this.isnew = isnew;
	}

	public boolean isIsvabrate() {
		return isvabrate;
	}

	public void setIsvabrate(boolean isvabrate) {
		this.isvabrate = isvabrate;
	}

	public String getAudiotype() {
		return audiotype;
	}

	public void setAudiotype(String audiotype) {
		this.audiotype = audiotype;
	}
}
