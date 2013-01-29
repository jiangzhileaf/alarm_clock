package com.killerban.settings;

import com.killerban.okclock.R;
import com.killerban.okclock.R.layout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * @author Ban 负责向网络传输数据的Activity
 */
public class WebTranslateActivity extends Activity {
	private ProgressDialog pd;
	public final static int RESULT_CODE = 4;
	final int PROGRESS_DIALOG = 0x112;
	private int status = 0; // 联网状态
	private int operate;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_progressdialog);
		operate = getIntent().getExtras().getInt("id");
		System.out.println("in status" + status);
		Handler mHandler = new Handler(Looper.getMainLooper());
		Handler mHandler2 = new Handler(Looper.getMainLooper());
		mHandler.post(dailogRun); 
		mHandler2.post(myRunnable);
	}

	public Dialog onCreateDialog(int id, Bundle status) {
		System.out.println("dialog");
		pd = new ProgressDialog(this);
		pd.setTitle("  正在连接...");
		pd.setCancelable(false);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setIndeterminate(false);
		return pd;
	}

	public void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
	}
	
	Runnable dailogRun = new Runnable() {
		public void run() {
			showDialog(PROGRESS_DIALOG);
		}
	};
	Runnable myRunnable = new Runnable() {
		public void run() {
			System.out.println("run");
			ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cwjManager.getActiveNetworkInfo();
			if (info != null && info.isAvailable()) {
				status = 1;
			} else {
				status = -1;
			}
			callBack();
		}
	};

	void callBack() {
		Intent intent = new Intent();
		intent.putExtra("status", status);
		intent.putExtra("id", operate);
		setResult(RESULT_CODE, intent);
		finish();
	}
}
