package com.exchange.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 核心工具程序
 * SDK of BitZ-API-V2
 * @author LEO at BitZ
 * Date: 20200901
 */
public class RequestApi {

	private String host;
	private String accessKey;
	private String secret;
	
	/**
	 * 获取当前 UTC 时间
	 * Get UTC time
	 * @return
	 */
	public static String getUTCtime()
	{
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
		String str = dateFormatGmt.format(new Date());
		str = str.replace(" ", "T") ;
		return str;
	}
	
	public RequestApi(String host, String accessKey, String secret) {
		this.host = host;
		this.accessKey = accessKey;
		this.secret = secret;
	}
	
	/**
	 * 使用 http 包执行 HTTP POST 请求
	 * Using package "http" to executing HTTP POST request.
	 * @param qurl 请求路径
	 * @param postParams 请求参数
	 * @return
	 */
	public String requestPost(String qurl, String postParams) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(qurl);
			URLConnection conn = realUrl.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
			out.print(postParams);
			out.flush();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return result;
	}
	
	public String getCurlParam(Map<String, String> sArray) throws UnsupportedEncodingException {
		String result = "" ;
		
		for(String i : sArray.keySet()) {
			if (result.length()>0) {
				result += "&" ;
			}
			
			String s = URLEncoder.encode(sArray.get(i), "UTF-8") ;
			
			result += i + "=" + s ;
		}
		
		return result ;
	}

	/**
	 * 拼接时，不包括最后一个&字符
	 * @param params
	 * @return 拼接后字符串
	 * @throws UnsupportedEncodingException 
	 */
	public String createLinkString(Map<String, String> params) throws UnsupportedEncodingException {

		String prestr = "";
		if( params == null) return "";
		List<String> keys = new ArrayList<String>(params.keySet());
		if(keys.size()>0){
			Collections.sort(keys);
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				String value = params.get(key);
				if (i == keys.size() - 1) {
					prestr = prestr + key + "=" + URLEncoder.encode(value, "UTF-8");
				} else {
					prestr = prestr + key + "=" + URLEncoder.encode(value, "UTF-8") + "&";
				}
			}
		}
		return prestr;
	}
	
	/**
	 * 生成签名
	 * 首先，将待签名字符串要求按照参数名进行排序
	 * 首先比较所有参数名的第一个字母，按abcd顺序排列，若遇到相同首字母，则看第二个字母，以此类推
	 * @param 	sArray
	 * @param  	secretKey
	 * @return 签名结果字符串
	 */
	public String buildSign(Map<String, String> sArray) {
		String mysign = "";
		try {
			String prestr = createLinkString(sArray);
			
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(this.secret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);
			mysign = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(prestr.getBytes())) ;
			 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mysign;
	}
	
	
}
