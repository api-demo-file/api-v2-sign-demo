package com.exchange.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Go 语言签名及 API 请求示例
 * This routine shows how do we post a API query.
 * @author LEO at BitZ
 * Date: 20200901
 */
public class Main {

	public static void main(String[] args) throws Exception {

		if (args.length<3) {
			System.out.println("usage: java com.exchange.utils.Main {Host} {Access Key} {Secret}") ;
			return ;
		}
		
		// 接收必要的命令行参数
		// Receive arguments from the CLI that API query required.
		String host = args[0];
		String accessKey = args[1];
		String secret = args[2];

		// 定义请求路径和请求参数
		// Define URL and POST parameters for API query.
		String qurl = host + "/V2/Trade/getUserNowEntrustSheet" ;
		
		Map<String, String> parameters = new HashMap<String, String>()  ;		
		parameters.put("coinFrom", "btc") ;
		parameters.put("coinTo", "usdt") ;
		parameters.put("accessKey", accessKey) ;
		
		// 时区必须使用 UTC
		// It must use UTC as timezone.
		String timeForQuery = RequestApi.getUTCtime() ;
		parameters.put("timestamp", timeForQuery) ;
		
		RequestApi requestApi = new RequestApi(host, accessKey, secret) ;
		// 生成签名
		// Generating signature.
		String sign = requestApi.buildSign(parameters) ;
		parameters.put("signature", sign) ;
				
		String curlParam = requestApi.getCurlParam(parameters) ;
		
		String result = String.format("curl \"%s\" -X POST -d \"%s\"", qurl, curlParam) ;
		
		System.out.println(result) ;
		
		// 执行 HTTP 请求
		// Executing HTTP request.
		String responseStr = requestApi.requestPost(qurl, curlParam) ;
		System.out.println("result:");
		System.out.println(responseStr);

	}
}
