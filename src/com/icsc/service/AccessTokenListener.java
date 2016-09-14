package com.icsc.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.json.JSONObject;

public class AccessTokenListener implements ServletContextListener {

	private static String ACCESS_TOKEN = "";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			Runnable runnable = new Runnable() {
				private String APPID = BasicAutoReply.getInstance().APPID;
				private String APPSECRET = BasicAutoReply.getInstance().APPSECRET;
				private String urlStr = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APPID + "&secret=" + APPSECRET;
				private URL url = new URL(urlStr);
				private URLConnection connection = null;
				private BufferedReader reader = null;
				private StringBuffer sb = null;
				private String line = "";
				private int expires_in = 1;

				@Override
				public void run() {
					while (true) {
						try {
							connection = url.openConnection();
							connection.connect();
							// 取得输入流，并使用Reader读取
							// 设置编码,否则中文乱码
							reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
							sb = new StringBuffer();
							while ((line = reader.readLine()) != null) {
								sb.append(line);
							}
							// {"access_token":"ACCESS_TOKEN","expires_in":7200}
							JSONObject jsonData = new JSONObject(sb.toString());
							expires_in = jsonData.getInt("expires_in");
							ACCESS_TOKEN = jsonData.getString("access_token");
							System.out.println("Start!-->EXPIRES_IN=" + expires_in + ";  ACCESS_TOKEN=" + ACCESS_TOKEN);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								if (reader != null) {
									reader.close();
								}
								Thread.sleep(expires_in * 1000);
							} catch (IOException | InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			};
			Thread t = new Thread(runnable);
			t.start();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

	public static String getAccessToken() {
		return ACCESS_TOKEN;
	}

}
