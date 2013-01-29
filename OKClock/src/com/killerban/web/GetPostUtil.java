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
		System.out.println(url+"?"+param);
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			
			System.out.println("url");
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			System.out.println("send");
			
			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.flush();

			System.out.println("out");
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String info=new String();
			System.out.println("info");
			while ((info = in.readLine()) != null) {
				result += info;
				System.out.println("read");
			}
		} catch (Exception e) {
			Log.i(TAG, "Send Post error");
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
