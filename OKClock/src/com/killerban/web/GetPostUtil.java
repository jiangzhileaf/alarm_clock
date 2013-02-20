package com.killerban.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

public class GetPostUtil {
	private final static String TAG = "GetPostUtil";

	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = new String();
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			
			conn.setConnectTimeout(2000);   
			conn.setReadTimeout(2000); 
			
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.flush();

			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String info=new String();
			while ((info = in.readLine()) != null) {
				result += info;
			}
		} catch (Exception e) {
			Log.i(TAG, "Send Post error");
			result ="出错了,网络连接有问题";
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
}
