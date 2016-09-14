package com.icsc.service;

import static com.icsc.service.AccessTokenListener.getAccessToken;

import org.json.JSONArray;
import org.json.JSONObject;

import com.icsc.core.AutoReply;

public class BasicAutoReply extends AutoReply {

	private static BasicAutoReply ar = null;

	public static BasicAutoReply getInstance() {
		if (ar == null) {
			return new BasicAutoReply();
		}
		return ar;
	}

	@Override
	public void init() {
		TOKEN = "Junking";
		ENCODINGAESKEY = "b8IrveqB4TVYTeKriGb8WwcqIMyNDdgK0Q9Z3dtHhQb";
		APPID = "wx110a685a82054c59";
		APPSECRET = "4f58d5bc1e01ea2e957e90d6a5c2acb1";
	}

	@Override
	public String doText(String content) {
		if ("at".equalsIgnoreCase(content)) {
			return text(getAccessToken());
		}
		return text("Yes!" + content);
	}

	@Override
	public String doImage(String picUrl, String mediaId) {
		String http = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=" + AccessTokenListener.getAccessToken() + "&type=image&";
		return image(mediaId);
	}

	@Override
	public String doVoice(String mediaId, String format) {
		return voice(mediaId);
	}

	@Override
	public String doVideo(String mediaId, String thumbMediaId) {
		return video(mediaId, "", "");
	}

	@Override
	public String doShortvideo(String mediaId, String thumbMediaId) {
		return image(thumbMediaId);
	}

	@Override
	public String doLocation(String location_X, String location_Y, String scale, String label) {
		return text("location_X:" + location_X + "\nlocation_Y:" + location_Y + "\nscale:" + scale + "\nlabel:" + label);
	}

	@Override
	public String doLink(String title, String description, String url) {
		return text("title:" + title + "\ndescription:" + description + "\nurl:" + url);
	}
	
	public static void main(String[] args) {
		JSONObject root = new JSONObject();
		JSONArray array = new JSONArray();
		root.put("button", array);
		
		//a
		JSONObject a = new JSONObject();
		array.put(a);
		a.put("type", "click");
		a.put("name", "今日歌曲");
		a.put("key", "V1001_TODAY_MUSIC");
		
		//b
		JSONObject b = new JSONObject();
		array.put(b);
		b.put("name","菜单");
		JSONArray b_array = new JSONArray();
		b.put("sub_button",b_array);
		//b_a
		JSONObject b_a = new JSONObject();
		b_array.put(b_a);
		b_a.put("type", "view");
		b_a.put("name", "搜索");
		b_a.put("url", "http://www.soso.com/");
		//b_b
		JSONObject b_b = new JSONObject();
		b_array.put(b_b);
		b_b.put("type", "view");
		b_b.put("name", "视频");
		b_b.put("url", "http://v.qq.com/");
		//b_c
		JSONObject b_c = new JSONObject();
		b_array.put(b_c);
		b_c.put("type", "click");
		b_c.put("name", "赞一下我们");
		b_c.put("key", "V1001_GOOD");
		
		System.out.println(root);
	}

}
