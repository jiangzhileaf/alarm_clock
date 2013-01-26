package com.killerban.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class GetPostUtil {
	
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		
		try {
			URL realUrl = new URL(url);
			URLConnection conn = realUrl.openConnection();
			System.out.println("URL");
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			System.out.println("in out");
			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			System.out.println("print param");
			out.flush();
			
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String info;
			System.out.println("set info");
			while ((info = in.readLine()) != null) {
				result += "\n" + info;
			}
			
		} catch (Exception e) {
			System.out.println("error");
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}

			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}

		}
		System.out.println("Finish!"+result);
		return result;
	}

	public static String sendGet(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		
		try {
			String urlname = url+"?"+param;
			URL realUrl = new URL(urlname);
			URLConnection conn = realUrl.openConnection();
			System.out.println("URL");
			conn.setRequestProperty("accept", "*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0(compatible;MSIE 6.0;Windows NT 5.1;SV1");
			conn.connect();
			
			Map<String,List<String>> map = conn.getHeaderFields();
			for(String key:map.keySet())
			{
				System.out.println(key+"--->"+map.get(key));
			}
			System.out.println("map");

			
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String info;
			System.out.println("set info");
			int i=1;
			while (i<=20&&(info = in.readLine()) != null) {
				result += "\n" + info;
				i++;
			}

		} catch (Exception e) {
			System.out.println("error");
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}

			} catch (Exception e2) {
				// TODO: handle exception
				e2.printStackTrace();
			}

		}

		return result;
	}
}
