package com.icsc.core.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {

	/**
	 * MD5加密 生成16码或32码
	 * 
	 * @param inStr
	 * @param digit
	 * @return
	 * @throws Exception
	 * @author W11821
	 * @throws UnsupportedEncodingException 
	 * @date 2016年7月12日 上午10:22:46
	 */
	public static String md5Encode(String inStr, int digit) throws UnsupportedEncodingException{
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		byte[] byteArray = inStr.getBytes("UTF-8");
		//移位
		for (int i = 0; i < byteArray.length; i++) {
			byteArray[i] = (byte) (byteArray[i] << 3);
		}
		byte[] md5Bytes = md.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		// 字节数组转换为 十六进制 数
		for (int i = 0; i < md5Bytes.length; i++) {
			String shaHex = Integer.toHexString(md5Bytes[i] & 0xFF);
			if (shaHex.length() < 2) {
				hexValue.append("0");
			}
			hexValue.append(shaHex);
		}
		if (digit == 16) {
			return hexValue.toString().substring(8, 24);
		} else if (digit == 32) {
			return hexValue.toString();
		}
		return "";
	}
	
	/**
	 * SHA1加密 生成40码
	 * 
	 * @param inStr
	 * @return
	 * @author W11821 
	 * @throws UnsupportedEncodingException 
	 * @date 2016年9月7日 下午4:50:07
	 */
	public static String sha1Encode(String inStr){  
		StringBuffer hexValue = new StringBuffer();
        try {  
        	MessageDigest md = MessageDigest.getInstance("SHA-1");  
            md.update(inStr.getBytes("UTF-8"));
            byte[] messageDigest = md.digest();
    		// 字节数组转换为 十六进制 数
    		for (int i = 0; i < messageDigest.length; i++) {
    			String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
    			if (shaHex.length() < 2) {
    				hexValue.append("0");
    			}
    			hexValue.append(shaHex);
    		}
        } catch (NoSuchAlgorithmException e) {  
        	e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return hexValue.toString();  
    }
	
	public static void main(String[] args) throws Exception {
		System.out.println(md5Encode("1234",32));
	}

}
