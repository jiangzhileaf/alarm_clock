package com.killerban.okclock;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DataShowActivity extends Activity {

	private ListView mylist;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data_show);
		mylist = (ListView) findViewById(R.id.mylistview);
		String[] names = new String[] { "Show Hidden Files",
				"Suffle Tab Position" };

		// Create an ArrayAdapter, that will actually make the Strings above
		// appear in the ListView
		mylist.setAdapter(new ArrayAdapter<String>(this,
				R.layout.listview_layout, names));
	}
}
