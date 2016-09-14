package com.icsc.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.icsc.core.security.AesException;
import com.icsc.core.security.EncryptUtil;
import com.icsc.core.security.WXBizMsgCrypt;

/**
 * 自动回复:7种消息的接收并可自动回复6种消息类型
 * 
 * 使用:	1.实现所有抽象方法:1个初始化+7个接收消息方法.
 * 		2.在init()里初始化:TOKEN, ENCODINGAESKEY, APPID, APPSECRET 在微信公众平台上获取.
 * 		3.接收消息类型及结构官方详解:https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140453&token=&lang=zh_CN
 * 		4.回复消息类型及结构官方详解:https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140543&token=&lang=zh_CN
 * 
 * *注意*： 默认 Java 中仅支持 128 位密钥，当使用 256 位密钥的时候，会报告密钥长度错误:illegal key size
 * 		   你需要下载一个支持更长密钥的包。这个包叫做 Java Cryptography Extension (JCE)
 * 		 Oracle官方下载加密包地址：
 * 		 jdk1.6:http://www.oracle.com/technetwork/java/javase/downloads/jce-6-download-429243.html
 *  	 jdk1.7:http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
 *   	 jdk1.8:http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
 */
public abstract class AutoReply {

	/**
	 * 实现类需初始化的参数
	 */
	public static String TOKEN, ENCODINGAESKEY, APPID, APPSECRET;

	/**
	 * 所有类型消息
	 */
	private static final String TEXT = "text";
	private static final String IMAGE = "image";
	private static final String VOICE = "voice";
	private static final String VIDEO = "video";
	private static final String SHORTVIDEO = "shortvideo";
	private static final String LOCATION = "location";
	private static final String LINK = "link";

	private HttpServletRequest httpServletRequest = null;
	private Element root = null;

	/**
	 * 构造函数初始化
	 */
	public AutoReply() {
		init();
	}

	/**
	 * 自动消息回复，支持7种类型消息
	 * 
	 * @param request
	 * @return 回复消息
	 * @author W11821
	 * @date 2016年9月9日 下午6:22:56
	 */
	public synchronized String reply(HttpServletRequest request) {
		try {
			// 解密
			String xmlContent = decoderXML(request);
			// 解析
			if(!xmlContent.isEmpty()){
				root = DocumentHelper.parseText(xmlContent).getRootElement();
				httpServletRequest = request;
				// 转发
				return replyController(root);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return "success";
	}

	/**
	 * 解密
	 * 
	 * @param request
	 * @return
	 * @author W11821
	 * @date 2016年9月9日 下午4:06:02
	 */
	private String decoderXML(HttpServletRequest request) {
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			// 1.从request中取得输入流
			inputStream = request.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			StringBuffer buf = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				buf.append(line);
			}
			if(buf.toString().trim().isEmpty()){
				return "";
			}
			// 2.解密
			WXBizMsgCrypt wxCeypt = new WXBizMsgCrypt(TOKEN, ENCODINGAESKEY, APPID);
			return wxCeypt.decryptMsg(request.getParameter("msg_signature"), request.getParameter("timestamp"), request.getParameter("nonce"), buf.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AesException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	/**
	 * 加密
	 * 
	 * @param request
	 * @param replyMsg
	 * @return
	 * @author W11821
	 * @date 2016年9月9日 下午4:05:35
	 */
	private String encoderXML(HttpServletRequest request, String replyMsg) {
		try {
			WXBizMsgCrypt wxCeypt = new WXBizMsgCrypt(TOKEN, ENCODINGAESKEY, APPID);
			return wxCeypt.encryptMsg(replyMsg, request.getParameter("timestamp"), request.getParameter("nonce"));
		} catch (AesException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 消息回复控制器
	 * 
	 * @param root
	 * @return
	 * @author W11821
	 * @date 2016年9月9日 下午6:19:09
	 */
	private String replyController(Element root) {
		String msg = "";
		switch (root.element("MsgType").getText()) {
		case TEXT:
			msg = doText(root.element("Content").getText());
			break;
		case IMAGE:
			msg = doImage(root.element("PicUrl").getText(), root.element("MediaId").getText());
			break;
		case VOICE:
			msg = doVoice(root.element("MediaId").getText(), root.element("Format").getText());
			break;
		case VIDEO:
			msg = doVideo(root.element("MediaId").getText(), root.element("ThumbMediaId").getText());
			break;
		case SHORTVIDEO:
			msg = doShortvideo(root.element("MediaId").getText(), root.element("ThumbMediaId").getText());
			break;
		case LOCATION:
			msg = doLocation(root.element("Location_X").getText(), root.element("Location_Y").getText(), root.element("Scale").getText(), root.element("Label").getText());
			break;
		case LINK:
			msg = doLink(root.element("Title").getText(), root.element("Description").getText(), root.element("Url").getText());
			break;
		default:
			break;
		}
		return msg;
	}

	/**
	 * 接入微信验证
	 * 
	 * @param request
	 * @return
	 * @author W11821
	 * @date 2016年9月9日 下午6:20:21
	 */
	public String authentication(HttpServletRequest request) {
		// 1.将token、timestamp、nonce三个参数进行字典序排序
		List<String> list = new ArrayList<String>();
		list.add(TOKEN);
		list.add(request.getParameter("timestamp"));
		list.add(request.getParameter("nonce"));
		Collections.sort(list);

		// 2.将三个参数字符串拼接成一个字符串进行sha1加密
		StringBuffer sb = new StringBuffer();
		for (String str : list) {
			sb.append(str);
		}
		// 3.比较加密后的字串和signature是否一致，一致返回echostr
		if (EncryptUtil.sha1Encode(sb.toString()).equals(request.getParameter("signature"))) {
			return request.getParameter("echostr");
		}
		return "";
	}

	/**
	 * 回复文本消息
	 * 
	 * @param content
	 *            回复的消息内容\n（换行：在content中能够换行，微信客户端就支持换行显示）
	 * @return
	 */

	public String text(String content) {
		Document xml = DocumentHelper.createDocument();
		Element returnRoot = xml.addElement("xml");
		returnRoot.addElement("ToUserName").addCDATA(root.element("FromUserName").getText());
		returnRoot.addElement("FromUserName").addCDATA(root.element("ToUserName").getText());
		returnRoot.addElement("CreateTime").addCDATA((System.currentTimeMillis() + "").substring(0, 10));
		returnRoot.addElement("MsgType").addCDATA(TEXT);
		returnRoot.addElement("Content").addCDATA(content);
		return encoderXML(httpServletRequest, returnRoot.asXML());
	}

	/**
	 * 回复图片消息
	 * 
	 * @param mediaId
	 *            通过素材管理中的接口上传多媒体文件，得到的id。
	 * @return
	 */
	public String image(String mediaId) {
		Document xml = DocumentHelper.createDocument();
		Element returnRoot = xml.addElement("xml");
		returnRoot.addElement("ToUserName").addCDATA(root.element("FromUserName").getText());
		returnRoot.addElement("FromUserName").addCDATA(root.element("ToUserName").getText());
		returnRoot.addElement("CreateTime").addCDATA((System.currentTimeMillis() + "").substring(0, 10));
		returnRoot.addElement("MsgType").addCDATA(IMAGE);
		returnRoot.addElement("Image").addElement("MediaId").addCDATA(mediaId);
		return encoderXML(httpServletRequest, returnRoot.asXML());
	}

	/**
	 * 回复语音消息
	 * 
	 * @param mediaId
	 *            通过素材管理中的接口上传多媒体文件，得到的id
	 * @return
	 */
	public String voice(String mediaId) {
		Document xml = DocumentHelper.createDocument();
		Element returnRoot = xml.addElement("xml");
		returnRoot.addElement("ToUserName").addCDATA(root.element("FromUserName").getText());
		returnRoot.addElement("FromUserName").addCDATA(root.element("ToUserName").getText());
		returnRoot.addElement("CreateTime").addCDATA((System.currentTimeMillis() + "").substring(0, 10));
		returnRoot.addElement("MsgType").addCDATA(VOICE);
		returnRoot.addElement("Voice").addElement("MediaId").addCDATA(mediaId);
		return encoderXML(httpServletRequest, returnRoot.asXML());
	}

	/**
	 * 回复视频消息
	 * 
	 * @param mediaId
	 *            通过素材管理中的接口上传多媒体文件，得到的id
	 * @param title
	 *            视频消息的标题
	 * @param description
	 *            视频消息的描述
	 * @return
	 */
	public String video(String mediaId, String title, String description) {
		Document xml = DocumentHelper.createDocument();
		Element returnRoot = xml.addElement("xml");
		returnRoot.addElement("ToUserName").addCDATA(root.element("FromUserName").getText());
		returnRoot.addElement("FromUserName").addCDATA(root.element("ToUserName").getText());
		returnRoot.addElement("CreateTime").addCDATA((System.currentTimeMillis() + "").substring(0, 10));
		returnRoot.addElement("MsgType").addCDATA(VIDEO);
		Element videoElement = returnRoot.addElement("Video");
		videoElement.addElement("MediaId").addCDATA(mediaId);
		videoElement.addElement("Title").addCDATA(title);
		videoElement.addElement("Description").addCDATA(description);
		return encoderXML(httpServletRequest, returnRoot.asXML());
	}

	/**
	 * 回复音乐消息
	 * 
	 * @param title
	 *            音乐标题
	 * @param description
	 *            音乐描述
	 * @param musicURL
	 *            音乐链接
	 * @param hQMusicUrl
	 *            高质量音乐链接，WIFI环境优先使用该链接播放音乐
	 * @param thumbMediaId
	 *            缩略图的媒体id，通过素材管理中的接口上传多媒体文件，得到的id
	 * @return
	 */
	public String music(String title, String description, String musicURL, String hQMusicUrl, String thumbMediaId) {
		Document xml = DocumentHelper.createDocument();
		Element returnRoot = xml.addElement("xml");
		returnRoot.addElement("ToUserName").addCDATA(root.element("FromUserName").getText());
		returnRoot.addElement("FromUserName").addCDATA(root.element("ToUserName").getText());
		returnRoot.addElement("CreateTime").addCDATA((System.currentTimeMillis() + "").substring(0, 10));
		returnRoot.addElement("MsgType").addCDATA("music");
		Element musicElement = returnRoot.addElement("Music");
		musicElement.addElement("Title").addCDATA(title);
		musicElement.addElement("Description").addCDATA(description);
		musicElement.addElement("MusicUrl").addCDATA(musicURL);
		musicElement.addElement("HQMusicUrl").addCDATA(hQMusicUrl);
		musicElement.addElement("ThumbMediaId").addCDATA(thumbMediaId);
		return encoderXML(httpServletRequest, returnRoot.asXML());
	}

	/**
	 * 回复图文消息
	 * 
	 * @param items
	 *            (NewsVo图文实体最大10个:Title 图文消息标题 ;Description 图文消息描述 PicUrl
	 *            图片链接，支持JPG、PNG格式，较好的效果为大图360*200，小图200*200 Url 点击图文消息跳转链接)
	 * @return
	 */
	public String news(ArrayList<NewsVo> items) {
		Document xml = DocumentHelper.createDocument();
		Element returnRoot = xml.addElement("xml");
		returnRoot.addElement("ToUserName").addCDATA(root.element("FromUserName").getText());
		returnRoot.addElement("FromUserName").addCDATA(root.element("ToUserName").getText());
		returnRoot.addElement("CreateTime").addCDATA((System.currentTimeMillis() + "").substring(0, 10));
		returnRoot.addElement("MsgType").addCDATA("news");
		returnRoot.addElement("ArticleCount").addCDATA(items.size() + "");
		Element newsElement = returnRoot.addElement("Articles");
		Element itemElement = null;
		for (NewsVo item : items) {
			itemElement = newsElement.addElement("item");
			itemElement.addElement("Title").addCDATA(item.getTitle());
			itemElement.addElement("Description").addCDATA(item.getDescription());
			itemElement.addElement("PicUrl").addCDATA(item.getPicUrl());
			itemElement.addElement("Url").addCDATA(item.getUrl());
		}
		return encoderXML(httpServletRequest, returnRoot.asXML());
	}

	/**
	 * 初始化:TOKEN, ENCODINGAESKEY, APPID, APPSECRET
	 * 
	 * @author W11821
	 * @date 2016年9月9日 下午6:20:31
	 */
	public abstract void init();

	/**
	 * 接收文本消息
	 * 
	 * @param content
	 *            文本消息内容
	 * @return
	 * @author W11821
	 * @date 2016年9月9日 下午5:53:06
	 */
	public abstract String doText(String content);

	/**
	 * 接收图片消息
	 * 
	 * @param picUrl
	 *            图片链接（由系统生成）
	 * @param mediaId
	 *            图片消息媒体id，可以调用多媒体文件下载接口拉取数据。
	 * @return
	 * @author W11821
	 * @date 2016年9月9日 下午6:36:23
	 */
	public abstract String doImage(String picUrl, String mediaId);

	/**
	 * 接收语音消息
	 * 
	 * @param mediaId
	 *            语音消息媒体id ，可以调用多媒体文件下载接口拉取数据。
	 * @param format
	 *            语音格式，如amr，speex等
	 * @return
	 * @author W11821
	 * @date 2016年9月9日 下午6:50:38
	 */
	public abstract String doVoice(String mediaId, String format);

	/**
	 * 接收视频消息
	 * 
	 * @param mediaId
	 *            视频消息媒体id，可以调用多媒体文件下载接口拉取数据。
	 * @param thumbMediaId
	 *            视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据。
	 * @return
	 */
	public abstract String doVideo(String mediaId, String thumbMediaId);

	/**
	 * 接收小视频
	 * 
	 * @param mediaId
	 *            视频消息媒体id，可以调用多媒体文件下载接口拉取数据。
	 * @param thumbMediaId
	 *            视频消息缩略图的媒体id，可以调用多媒体文件下载接口拉取数据。
	 * @return
	 */
	public abstract String doShortvideo(String mediaId, String thumbMediaId);

	/**
	 * 接收地理位置消息
	 * 
	 * @param location_X
	 *            地理位置维度
	 * @param location_Y
	 *            地理位置经度
	 * @param scale
	 *            地图缩放大小
	 * @param label
	 *            地理位置信息
	 * @return
	 */
	public abstract String doLocation(String location_X, String location_Y, String scale, String label);

	/**
	 * 接收链接消息
	 * 
	 * @param title
	 *            消息标题
	 * @param description
	 *            消息描述
	 * @param url
	 *            消息链接
	 * @return
	 */
	public abstract String doLink(String title, String description, String url);

}
