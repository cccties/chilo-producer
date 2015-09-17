/**********************************************************************************************
 * CHiLOBook用 Javascript API (chiloAPI : String, chiloData : JSON )
 *
 * chiloAPI = 'downloadbook'	ePub file download
 * chiloAPI = 'setmyfavorite';	お気に入り登録
 * chiloAPI = "testlog"			テスト回答記録
 * chiloAPI = "showTestResults"	テスト採点
 **********************************************************************************************/
 
//var localhostApiDomain = "http://localhost:8080/";

/**********************************************************************************************
 * CHiLO API パラメータ chiloWeb urlStrにurlを設定
 **********************************************************************************************/
var chiloData;
function chiloWebFunc(url,title){
	//alert(url)
	
	try{
	 top.chiloStep1(url,title);
	} catch (e) {
	 window.open(url);
	}

/*
	var localhostApiDomain = "http://localhost:8080/";
	var chiloAPI = "chiloWeb";
	var chiloData = {
		urlStr: url
	};
	callTestLogAPI(localhostApiDomain,chiloAPI,chiloData);
	
	chiloData = {
		urlStr: url
	};
	chiloApiCallback("chiloWebFunc2");
*/
}
function chiloWebFunc2(port){
	var localhostApiDomain = "http://localhost:"+ port + "/";
	var chiloAPI = "chiloWeb";
	callCHiLOhref(localhostApiDomain,chiloAPI,chiloData);
}

/**********************************************************************************************
 * CHiLOBook API Jquery ajax使用
 **********************************************************************************************/
function callCHiLOjsonp(localhostApiDomain,chiloAPI,chiloData) {
	//alert("callCHiLOjsonp url: "+ localhostApiDomain + chiloAPI);
	$.ajax({
              url:localhostApiDomain + chiloAPI,
              type:'GET',
              data:JSON.stringify(chiloData),
              dataType: 'jsonp',
              jsonp : "callbackChange",
              timeout:10000
            });
}//end testLog

/**********************************************************************************************
 * CHiLOBook API jsonp callback 使用
 **********************************************************************************************/
function callbackChange(msg){
	alert("callbackChange: "+ msg);
}

/**********************************************************************************************
 * CHiLOBook API location href 使用
 **********************************************************************************************/
function callCHiLOhref(localhostApiDomain,chiloAPI,chiloData) {
	//alert("callCHiLOhref url: "+ localhostApiDomain + chiloAPI);
	var urlPath = localhostApiDomain + chiloAPI + "?" + JSON.stringify(chiloData);
	window.location.href = urlPath;
}//end callCHiLO

 /**********************************************************************************************
 * CHiLOBook API localhostApiPort 変更 Called by CHILO Reader
	getChiloApiPort : 	現在のPort番号を取得, 
				CHIRO ReaderからsetChiloApiPortがコールバックされportに値が渡されます
	chiloApiCallback(apifunc) : getChiloApiPortのコールバック関数をapifuncに指定できます。
					apifuncで指定した関数がコールバックされportに値が渡されます
 **********************************************************************************************/
function setChiloApiPort(port) {
	chiloApiPort = port;
	alert("chiloApiPort: "+ port);
}//end callCHiLO
function getChiloApiPort() {
	window.location.href = "http://localhost/" + "getChiloApiPort";
}//end callCHiLO

function chiloApiCallback(apifunc){
	window.location.href = "http://localhost/" + "chiloApiCallback/"+apifunc;
}
