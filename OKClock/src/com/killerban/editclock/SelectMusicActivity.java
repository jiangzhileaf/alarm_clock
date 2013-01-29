package com.killerban.editclock;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.killerban.okclock.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Ban 选择MP3文件作为闹钟铃声
 */
public class SelectMusicActivity extends ListActivity {
	public static final int RESULT_CODE = 2;
	/** Called when the activity is first created. */
	private List<String> items = null;// 存放名称
	private List<String> paths = null;// 存放路径
	private String rootPath = "/";
	private TextView tv;
	private String path = "/"; // 当前路径

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectmusic);
		tv = (TextView) this.findViewById(R.id.textview);
		this.getFileDir(rootPath);// 获取rootPath目录下的文件.
	}

	public void getFileDir(String filePath) {
		try {
			this.tv.setText("当前路径:" + filePath);// 设置当前所在路径
			items = new ArrayList<String>();
			paths = new ArrayList<String>();
			File f = new File(filePath);
			File[] files = f.listFiles();// 列出所有文件
			// 如果不是根目录,则列出返回根目录和上一目录选项
			if (!filePath.equals(rootPath)) {
				items.add("返回根目录");
				paths.add(rootPath);
				items.add("返回上一层目录");
				paths.add(f.getParent());
			}
			// 将所有文件存入list中
			if (files != null) {
				int count = files.length;// 文件个数
				for (int i = 0; i < count; i++) {
					File file = files[i];
					items.add(file.getName());
					paths.add(file.getPath());
				}
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, items);
			this.setListAdapter(adapter);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		path = paths.get(position);
		File file = new File(path);
		// 如果是文件夹就继续分解
		if (file.isDirectory()) {
			this.getFileDir(path);
		} else {
			if (file.getName().endsWith(".mp3"))
				callBack(path);
			else {
				Toast.makeText(this, "暂时不支持选择该类型音乐作为闹铃", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void callBack(String path) {
		Intent intent = new Intent();
		intent.putExtra("path", path);
		setResult(RESULT_CODE, intent);
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) { // 覆写返回键

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			String temp = path;
			File f = new File(temp);
			if (!f.isDirectory()) // 不是文件夹 ，获取其路径
			{
				int i = temp.lastIndexOf("/");
				if(i==0)
					i=1;
				temp = temp.substring(0, i);
			}
			System.out.println("temp " +temp);
			if (!temp.equals(rootPath)) // 不是根目录
			{
				File f2 = new File(temp);
				path=f2.getParent();			//改变目录路径为其父目录路径
				this.getFileDir(f2.getParent()); // 返回父目录
				return true;
			} else // 根目录，退出
			{
				return super.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}