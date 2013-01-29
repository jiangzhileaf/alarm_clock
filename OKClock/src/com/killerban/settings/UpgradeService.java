package com.killerban.settings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.widget.RemoteViews;

public class UpgradeService extends Service {
	private File file = null;
	private String url = null;

	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("in onstartCommand");
		file = new File(Environment.getExternalStorageDirectory(),
				"OkClock.apk");
		url = "http://down.meizumi.com/m9/Application/201301/com.lovebizhi.wallpaper.apk";
		DownloadFile task = new DownloadFile();
		task.execute(url);
		return START_STICKY;
	}

	// 在非UI主线程运行，以防阻塞
	class DownloadFile extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... allUrl) {
			try {
				
				System.out.println("download");
				URL url = new URL(allUrl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				// 下载文件，并写入SDCARD中
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(file);
				System.out.println("in out");
				byte data[] = new byte[1024];

				while (input.read(data) != -1) {
					output.write(data);
				}
				output.flush();
				output.close();
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
				
				System.out.println("error");
			}
			showNotification();
			return null;
		}
	};

	public void showNotification()
	{
		System.out.println("shownotification");
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(android.R.drawable.stat_sys_download_done,
				"OkClock下载完成", System.currentTimeMillis());
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		PendingIntent pi = PendingIntent.getActivity(UpgradeService.this,
				0, intent, 0);
		RemoteViews contentView = new RemoteViews(getPackageName(),
				com.killerban.okclock.R.layout.activity_download);
		notification.contentIntent = pi;
		notification.contentView = contentView;
		mNotificationManager.notify(0, notification);
	}
	
	public void installSoftware() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}
}
