<?php
// PHP 语言签名及 API 请求示例
// This routine shows how do we post a API query.
// Auther: LEO
// Date: 20200729

if (count($argv)<4) {
	# code...
	echo "usage: php demo.php {Host} {Access Key} {Secret}",PHP_EOL;
	exit(1);
}

// 接收必要的命令行参数
// Receive arguments from the CLI that API query required.
$host = $argv[1] ;
$accessKey = $argv[2] ;
$secret = $argv[3] ;

$parameters = [] ;

// 定义请求路径和请求参数
// Define URL and POST parameters for API query.
$url = $host.'/V2/Trade/getUserNowEntrustSheet' ;
$parameters = [
	'coinFrom' => 'btc',
	'coinTo' => 'usdt',
] ;

$parameters['accessKey'] = $accessKey ;

// 时区必须使用 UTC
// It must use UTC as timezone.
$timeObj = new DateTime('now', new DateTimeZone('UTC')) ;
$parameters['timestamp'] = $timeObj->format("Y-m-d\TH:i:s") ;

// 生成签名
// Generating signature.
$signature = getSign($parameters, $secret) ;
$parameters["signature"] = $signature;

$formData = http_build_query($parameters) ;
echo sprintf('curl "%s" -X POST -d "%s"', $url, $formData) , PHP_EOL ;

try {
	// 执行 HTTP 请求
	// Executing HTTP request.
	list($respCode, $respData) = requestPost($url, $parameters) ;
	echo "http code : ", $respCode , PHP_EOL ;
	echo "http response : ", $respData , PHP_EOL ;
}catch (Exception $e) {
	echo "ERROR : ", $e->getMessage();
}

// 使用 CURL 库执行 HTTP POST 请求
// Using CURL to executing HTTP POST request.
function requestPost($url, $postParams){
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL,$url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_TIMEOUT, 10);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $postParams);

    $data = curl_exec($ch);

    if (curl_errno($ch)) {
        throw new Exception("CURL-Error: " . curl_error($ch)) ;
    } else {
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);
        return [$httpCode, $data];
    }
}

// 签名函数
// The function that for signature.
function getSign($param, $secretKey)
{
    $preStr = "" ;

    ksort($param);
    foreach ($param as $k=>$v) {
        if (strlen($preStr)>0) {
            $preStr .= "&" ;
        }

        $preStr .= $k."=".urlencode($v) ;
    }

    $signature = hash_hmac('sha256', $preStr, $secretKey, true);
    return base64_encode($signature);
}