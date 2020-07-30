// Go 语言签名及 API 请求示例
// This routine shows how do we post a API query.
// Auther: LEO
// Date: 20200729

package main

import (
	"bytes"
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"os"
	"sort"
	"time"
)

var (
	host = ""
	accessKey = ""
	secret = ""
)

func main() {
	if len(os.Args)<4 {
		fmt.Println("usage: go run demo.go {Host} {Access Key} {Secret}")
		os.Exit(1)
	}

	// 接收必要的命令行参数
	// Receive arguments from the CLI that API query required.
	host = os.Args[1]
	accessKey = os.Args[2]
	secret = os.Args[3]

	// 定义请求路径和请求参数
	// Define URL and POST parameters for API query.
	qurl := host + "/V2/Trade/getUserNowEntrustSheet"
	parameters := map[string]string{
		"coinFrom": "btc" ,
		"coinTo": "usdt" ,
	}
	parameters["accessKey"] = accessKey

	// 时区必须使用 UTC
	// It must use UTC as timezone.
	parameters["timestamp"] = time.Now().Format("2006-01-02T15:04:05")

	// 生成签名
	// Generating signature.
	signature := getSign(parameters, secret)
	parameters["signature"] = signature

	fmt.Println(curlString(qurl, parameters))

	// 执行 HTTP 请求
	// Executing HTTP request.
	respData,e := requestPost(qurl, parameters)
	if e!=nil {
		fmt.Println("ERROR: ", e)
		os.Exit(1)
	}

	fmt.Println("http response : ", respData)
}

func curlString(qurl string, postParams map[string]string) string {
	reqPars := make(url.Values)
	for k,v := range postParams {
		reqPars.Set(k,v)
	}

	result := fmt.Sprintf("curl \"%s\" -X POST -d \"%s\"", qurl, reqPars.Encode())
	return result
}

// 使用 http 包执行 HTTP POST 请求
// Using package "http" to executing HTTP POST request.
func requestPost(qurl string, postParams map[string]string) (result string, e error) {
	reqPars := make(url.Values)
	for k,v := range postParams {
		reqPars.Set(k,v)
	}

	resp,e := http.PostForm(qurl, reqPars)

	if e!=nil {
		return "", e
	}

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("request failed http code %d", resp.StatusCode)
	}

	defer resp.Body.Close()

	respByte,e := ioutil.ReadAll(resp.Body)
	if e != nil {
		return "", e
	}

	return string(respByte), nil
}

// 签名函数
// The function that for signature.
func getSign(param map[string]string, secretKey string) string {
	var preStrBuf bytes.Buffer
	paramKeys := make([]string, 0)
	for k,_ := range param {
		paramKeys = append(paramKeys, k)
	}
	sort.Strings(paramKeys)
	for _,k := range paramKeys {
		v := param[k]
		if preStrBuf.Len()>0 {
			preStrBuf.WriteRune('&')
		}
		preStrBuf.WriteString(k)
		preStrBuf.WriteRune('=')
		preStrBuf.WriteString(url.QueryEscape(v))
	}
	h:=hmac.New(sha256.New, []byte(secretKey))
	h.Write(preStrBuf.Bytes())
	s := base64.StdEncoding.EncodeToString(h.Sum(nil))
	return s
}
