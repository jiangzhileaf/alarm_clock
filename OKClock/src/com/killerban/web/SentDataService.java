package com.killerban.web;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.killerban.database.GetUpDatabaseHelper;
import com.killerban.database.UserDatabaseHelper;

/**
 * @author Ban 向服务器发送本地起床时间的数据库信息，因为所需时间可能较长 所以采用了Service实现
 */
public class SentDataService extends Service {

	private final static String TAG = "SentDataService";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Service onCreate");
	}

	// 获取加密密匙
	public String getStartKey(String userid, String password) {
		String url = "http://172.16.41.161/Mobile/startRequest";
		String param = "loginName=" + userid + "&password=" + password;
		return GetPostUtil.sendPost(url, param);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		GetUpDatabaseHelper db = new GetUpDatabaseHelper(this,
				GetUpDatabaseHelper.DATABASE_NAME);
		Cursor cursor = db.selectGetUp();
		String userid = intent.getExtras().getString("id");
		String password = intent.getExtras().getString("password");
		System.out.println("session" + userid + password);

		String[] info = new String[9];
		String url = "http://172.16.41.161/Mobile/upload";
		String key = getStartKey(userid, password);
		System.out.println("service test");
		// 读取数据库信息，从后往前
		// 因为当上传的信息在服务器已经存在时，服务器返回错误提示，此时无需再往前
		// 读取数据库，因为前面的都已经上传过
		
		int sum=0;	//记录上传记录的数量
		if (cursor.moveToLast()) {
			do {
				info[0] = cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.YEAR));
				info[1] = cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.MONTH));
				info[2] = cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.DAY));
				info[3] = cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.HOUR));
				info[4] = cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.MINUTE));
				info[5] = cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.TIME));
				info[6] = cursor.getString(cursor
						.getColumnIndex(GetUpDatabaseHelper.LEVEL));
				info[7] = cursor.getString(
						cursor.getColumnIndex(GetUpDatabaseHelper.SUCCESS))
						.equals("1") ? "true" : "false";
				String param = "year=" + info[0] + "&month=" + info[1]
						+ "&day=" + info[2] + "&hour=" + info[3] + "&minute="
						+ info[4] + "&duration=" + info[5] + "&level="
						+ info[6] + "&state=" + info[7] + "&loginName="
						+ userid + "&password=" + password + "&key=" + key;
				System.out.println(param);
				Log.i(TAG, "sent param=" + param);
				// 该记录在服务器已经存在
				if (!GetPostUtil.sendPost(url, param).equals("成功")) {
					Log.i(TAG, "upload stop");
					break;
				}
				sum++;
			} while (cursor.moveToPrevious());
			// 结束标记
			GetPostUtil.sendPost("http://172.16.41.161/Mobile/endRequest",
					"key=" + key);
			Toast.makeText(SentDataService.this, "上传成功,共上传"+sum+"条记录~", Toast.LENGTH_SHORT)
			.show();
		} else // 无起床记录
		{
			Toast.makeText(SentDataService.this, "没有数据记录~", Toast.LENGTH_SHORT)
					.show();
		}
		cursor.close();
		db.close();
		return START_STICKY;
	}

	public void onDestroy() {
		super.onDestroy();
	}
}
