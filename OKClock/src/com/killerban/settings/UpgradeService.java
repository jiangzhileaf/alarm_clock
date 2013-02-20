package com.killerban.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;

public class UpgradeService extends Service {
	private String url = null;

	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		url = "http://qqfe.org/dodo/dodo-last.apk";
		DownloadFile task = new DownloadFile();
		task.execute(url);
		return START_STICKY;
	}

	// 在非UI主线程运行，以防阻塞
	class DownloadFile extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... allUrl) {
			try {

				URL url = new URL(allUrl[0]);
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();

				String PATH = Environment.getExternalStorageDirectory() + "/";
				File file = new File(PATH, "hello.apk");
				FileOutputStream fos = new FileOutputStream(file);

				InputStream is = c.getInputStream();

				byte[] buffer = new byte[1024];
				int len1 = 0;
				while ((len1 = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len1);
				}
				fos.close();
				is.close();
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(android.content.Intent.ACTION_VIEW);
				System.out.println("-------->" + Uri.fromFile(file));
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				startActivity(intent);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return null;
		}
	};
}
