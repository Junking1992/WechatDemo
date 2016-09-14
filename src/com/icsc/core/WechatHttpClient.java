package com.icsc.core;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;

import sun.security.jca.GetInstance;

public class WechatHttpClient {
	
	private static WechatHttpClient client= null;
	
	public static WechatHttpClient getInstance(){
		return null;
	}

	private HttpClient httpClient = HttpClients.createDefault();
	private HttpPost method = null;
	
	
}
