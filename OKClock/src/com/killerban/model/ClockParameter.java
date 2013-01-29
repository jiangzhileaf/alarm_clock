package com.killerban.model;

import java.io.Serializable;
import java.util.Calendar;

import com.killerban.database.DatabaseHelper;

import android.app.AlarmManager;
import android.database.Cursor;

public class ClockParameter implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private int hour;
	private int minute;
	private int level;
	private String name;
	private boolean[] repeat;
	private boolean isopen;
	private boolean isnew; // 是否新建闹钟
	private boolean isvabrate; // 是否震动
	private String audiotype; // 响铃铃声类型

	public ClockParameter() {
		this.name = "";
		this.hour = 8;
		this.minute = 0;
		this.isopen = false;
		this.level = 1;
		this.repeat = new boolean[7];
		this.isnew = true;
		this.isvabrate = true;
		this.audiotype = "default";
	}

	// 将数据库的查询结果地赋值到该类对应的属性
	public void translateFromDatabase(Cursor cursor) {
		this.setHour(Integer.parseInt(cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.HOUR))));
		this.setMinute(Integer.parseInt(cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.MINUTE))));
		this.setId(Integer.parseInt(cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.ID))));
		this.setLevel(Integer.parseInt(cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.LEVEL))));
		this.setName(cursor.getString(cursor
				.getColumnIndex(DatabaseHelper.NAME)));
		this.setIsvabrate(cursor.getString(
				cursor.getColumnIndex(DatabaseHelper.ISVIBRATE)).equals("1"));
		this.setIsopen(cursor.getString(
				cursor.getColumnIndex(DatabaseHelper.ISOPEN)).equals("1"));
		this.setIsnew(cursor.getString(
				cursor.getColumnIndex(DatabaseHelper.ISNEW)).equals("1"));
		this.setAudiotype(cursor.getString(
				cursor.getColumnIndex(DatabaseHelper.AUDIOTYPE)));
		for (int i = 0; i <= 6; i++) {
			/*
			 * repeat[i] = Boolean.parseBoolean(cursor.getString(cursor
			 * .getColumnIndex((DatabaseHelper.REPEAT + i)))); 用此方法无效 其只有01值
			 */
			if ((cursor.getString(cursor
					.getColumnIndex((DatabaseHelper.REPEAT + i)))).equals("1")) {
				repeat[i] = true;
			} else
				repeat[i] = false;
		}
	}

	// 判断该闹钟是否只响铃一次，即无重复
	public boolean noRepeating() {
		boolean flag = true;
		for (int i = 0; i <= 6; i++) {
			if (repeat[i]) {
				flag = false;
				break;
			}
		}
		return flag;
	}

	// 根据重复周期的boolean数组 返回重复日期的格式字符串
	public String getRepeatInfo(boolean[] repeat) {
		String info = "周";
		String[] week = { "日 ", "一 ", "二 ", "三 ", "四 ", "五 ", "六 " };
		if (repeat[0] && repeat[6] && !repeat[1] && !repeat[2] && !repeat[3]
				&& !repeat[4] && !repeat[5]) {
			return "周末";
		}
		if (!repeat[0] && !repeat[6] && repeat[1] && repeat[2] && repeat[3]
				&& repeat[4] && repeat[5]) {
			return "周一到五";
		}
		if (repeat[0] && repeat[6] && repeat[1] && repeat[2] && repeat[3]
				&& repeat[4] && repeat[5]) {
			return "每天响铃";
		}
		if (!repeat[0] && !repeat[6] && !repeat[1] && !repeat[2] && !repeat[3]
				&& !repeat[4] && !repeat[5]) {
			return "只响一次";
		}
		for (int i = 0; i <= 6; i++) {
			if (repeat[i])
				info += week[i];
		}
		return info;
	}

	// 获取该闹钟响铃时间的Calendar对象
	public Calendar getCalendar() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, this.hour);
		c.set(Calendar.MINUTE, this.minute);
		c.set(Calendar.SECOND, 0); // 此处如果不设置的话，响铃时间精确至秒
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}

	// 求出距离现时到最近一次准备响铃的时刻
	public long getRecentlyAlarmTime() {
		Calendar c = getCalendar();
		long now = System.currentTimeMillis();
		long temp;
		long min = c.getTimeInMillis() + 8 * AlarmManager.INTERVAL_DAY;
		long circleTime = 7 * AlarmManager.INTERVAL_DAY;
		if (noRepeating()) {
			if (c.getTimeInMillis() < now) 
				min = c.getTimeInMillis() + AlarmManager.INTERVAL_DAY;
			  else
				min = c.getTimeInMillis();
			return min;
		} else {
			for (int i = 0; i < 7; i++) {
				if (repeat[i]) {
					c.set(Calendar.DAY_OF_WEEK, i + 1);
					if (c.getTimeInMillis() < now) {
						temp = c.getTimeInMillis() + circleTime;
						if (temp < min)
							min = temp;
					} else {
						if (c.getTimeInMillis() < min)
							min = c.getTimeInMillis();
					}
				}
			}
		}
		return min;
	}

	public long[] cycleAlarmTime() {
		long[] cycleTime = new long[7];
		Calendar c = getCalendar();
		long now = System.currentTimeMillis();
		for (int i = 0; i < 7; i++) {
			if (repeat[i]) {
				c.set(Calendar.DAY_OF_WEEK, i + 1);
				if (c.getTimeInMillis() < now) {
				} else {
					cycleTime[i] = c.getTimeInMillis()
							+ AlarmManager.INTERVAL_DAY * 7;
				}
			} else
				cycleTime[i] = -1;
		}
		return cycleTime;
	}

	// 求出time的格式化时间字符串
	public static String getFormatTime(long time) {
		String s = "";
		long temp = time;
		if (temp / AlarmManager.INTERVAL_DAY > 0) // 天数
		{
			s += ((temp / AlarmManager.INTERVAL_DAY) + " 天");
			temp = temp % AlarmManager.INTERVAL_DAY;
		}
		if (temp / AlarmManager.INTERVAL_HOUR > 0) // 求出时
		{
			s += (temp / AlarmManager.INTERVAL_HOUR + " 小时 ");
			temp = temp % AlarmManager.INTERVAL_HOUR;
		}
		if (temp / 1000 / 60 > 0) // 求出分
		{
			s += (temp / 1000 / 60 + " 分 ");
			temp = temp % 60000;
		}
		s += (temp / 1000 + " 秒"); // 求出秒
		return s;
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
