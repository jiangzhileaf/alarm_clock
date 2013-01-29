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
 * @author Ban ѡ��MP3�ļ���Ϊ��������
 */
public class SelectMusicActivity extends ListActivity {
	public static final int RESULT_CODE = 2;
	/** Called when the activity is first created. */
	private List<String> items = null;// �������
	private List<String> paths = null;// ���·��
	private String rootPath = "/";
	private TextView tv;
	private String path = "/"; // ��ǰ·��

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectmusic);
		tv = (TextView) this.findViewById(R.id.textview);
		this.getFileDir(rootPath);// ��ȡrootPathĿ¼�µ��ļ�.
	}

	public void getFileDir(String filePath) {
		try {
			this.tv.setText("��ǰ·��:" + filePath);// ���õ�ǰ����·��
			items = new ArrayList<String>();
			paths = new ArrayList<String>();
			File f = new File(filePath);
			File[] files = f.listFiles();// �г������ļ�
			// ������Ǹ�Ŀ¼,���г����ظ�Ŀ¼����һĿ¼ѡ��
			if (!filePath.equals(rootPath)) {
				items.add("���ظ�Ŀ¼");
				paths.add(rootPath);
				items.add("������һ��Ŀ¼");
				paths.add(f.getParent());
			}
			// �������ļ�����list��
			if (files != null) {
				int count = files.length;// �ļ�����
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
		// ������ļ��оͼ����ֽ�
		if (file.isDirectory()) {
			this.getFileDir(path);
		} else {
			if (file.getName().endsWith(".mp3"))
				callBack(path);
			else {
				Toast.makeText(this, "��ʱ��֧��ѡ�������������Ϊ����", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void callBack(String path) {
		Intent intent = new Intent();
		intent.putExtra("path", path);
		setResult(RESULT_CODE, intent);
		finish();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) { // ��д���ؼ�

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			String temp = path;
			File f = new File(temp);
			if (!f.isDirectory()) // �����ļ��� ����ȡ��·��
			{
				int i = temp.lastIndexOf("/");
				if(i==0)
					i=1;
				temp = temp.substring(0, i);
			}
			System.out.println("temp " +temp);
			if (!temp.equals(rootPath)) // ���Ǹ�Ŀ¼
			{
				File f2 = new File(temp);
				path=f2.getParent();			//�ı�Ŀ¼·��Ϊ�丸Ŀ¼·��
				this.getFileDir(f2.getParent()); // ���ظ�Ŀ¼
				return true;
			} else // ��Ŀ¼���˳�
			{
				return super.onKeyDown(keyCode, event);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}