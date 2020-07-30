// NodeJS 语言签名及 API 请求示例
// This routine shows how do we post a API query.
// Auther: LEO
// Date: 20200729

const process = require("process") ;
const crypto = require('crypto');
const querystring = require('querystring') ;
const url = require('url') ;

main() ;
process.stdin.resume();

// -----------------------------    [Functions]    ----------------------------- //

async function main(){

	if (process.argv.length<5) {
		console.error("usage: node demo.js {Host} {Access Key} {Secret}") ;
		process.exit(1);
	}

	// 接收必要的命令行参数
	// Receive arguments from the CLI that API query required.
	let host = process.argv[2] ;
	let accessKey = process.argv[3] ;
	let secret = process.argv[4] ;

	// 定义请求路径和请求参数
	// Define URL and POST parameters for API query.
	let qurl = host + '/V2/Trade/getUserNowEntrustSheet' ;
	let parameters = {
		'coinFrom': 'btc',
		'coinTo': 'usdt'
	} ; 

	parameters['accessKey'] = accessKey ;

	// 时区必须使用 UTC
	// It must use UTC as timezone.
	const now = new Date();
	let timeStr = now.toISOString() ;
	parameters['timestamp'] = timeStr.substring(0, timeStr.indexOf("."));

	// 生成签名
	// Generating signature.
	let signature = getSign(parameters, secret) ;
	parameters["signature"] = signature;

	// 执行 HTTP 请求
	// Executing HTTP request.
	let result = await requestPost(qurl, parameters) ;
	console.log(result) ;

	console.log("Execution done. Enter ctrl+c to exit") ;
}

// 使用 http/https 库执行 HTTP POST 请求
// Using library 'http/https' to executing HTTP POST request.
function requestPost(qurl, postParams){
	return new Promise(function (resolve, reject){

		const postData = querystring.stringify(postParams);
		const urlPathObj = url.parse(qurl) ;

		let http = require("http")
		if (urlPathObj.protocol.indexOf("https")>-1) {
			http = require("https")
		}

		const options = {
			hostname: urlPathObj.hostname ,
			port: urlPathObj.port ,
			path: urlPathObj.pathname ,
			method: 'POST' ,
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded',
				'Content-Length': Buffer.byteLength(postData)
			}
		}

		const req = http.request(options, (resp)=>{
			resp.setEncoding('utf8');

			let respBody = "" ;

			resp.on('data', (chunk) => {
				respBody += chunk ;
			});
			resp.on('end', () => {
				resolve({
					status: true,
					data: respBody,
					httpCode: resp.statusCode
				}) ;
			});
		}) ;


		req.on('error', (e) => {
			resolve({
				status: false,
				error: e.message
			}) ;
		});

		// 将数据写入请求主体。
		req.write(postData);
		req.end();

	});

}

// 签名函数
// The function that for signature.
function getSign(param, secretKey)
{
	let ks = [] ;

	for (let k in param) {
		ks.push(k)
	}

	ks.sort() ;

	let preStr = "" ;

	ks.forEach((k) => {
		if (preStr.length>0) {
			preStr += "&" ;
		}

		preStr += k + "=" + encodeURIComponent(param[k])
	});

	const hmac = crypto.createHmac('sha256', secretKey);
	hmac.update(preStr);
	return hmac.digest('base64')
}
