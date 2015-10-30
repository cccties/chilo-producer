/**********************************************************************************************
 * CHiLOBook用 Javascript API (chiloAPI : String, chiloData : JSON )
 *
 * chiloAPI = 'downloadbook'	ePub file download
 * chiloAPI = 'setmyfavorite';	お気に入り登録
 * chiloAPI = "testlog"			テスト回答記録
 * chiloAPI = "showTestResults"	テスト採点
 **********************************************************************************************/
 
//var localhostApiDomain = "http://localhost:8080/";


function chiloreadercheckFunc(main,sub,w){
	//alert(main);
	var appcheck;

	if(jQuery.cookie("appchk") == 1){
    	//alert('cookie');
	    appcheck = 1;
	    //cookieを保存する(naka)
	    var cookiedate = new Date();
		cookiedate.setTime( cookiedate.getTime() + ( 3600 * 1000 * 24)); //有効期限1日
		//cookiedate.setTime( cookiedate.getTime() + ( 60 * 1000)); //有効期限60秒
	    jQuery.cookie('appchk', '1', { expires: cookiedate, path: '/' }); //有効範囲はドメイン全体
	}else{
    	//alert('no cookie');
	    appcheck = 0;
	}

	if(window.location.search){
       var n=window.location.search.substring(1,window.location.search.length);
       //alert(n);

	    if ( n.match(/chiloreader=external/)) {
			//alert('external');
			appcheck = 0;
	        jQuery.removeCookie('appchk', { path: '/' });
		}
		
	    if ( n.match(/chiloreader=internal/)) {
		//引数にchiloreaderを含む
			//alert('internal');
		    appcheck = 1;
		    //cookieを保存する(naka)
		    var cookiedate = new Date();
			//cookiedate.setTime( cookiedate.getTime() + ( 3600 * 1000 * 24)); //有効期限1日
			cookiedate.setTime( cookiedate.getTime() + ( 60 * 1000)); //有効期限60秒
		    jQuery.cookie('appchk', '1', { expires: cookiedate, path: '/' }); //有効範囲はドメイン全体
		}
	}

	if(appcheck == 1){
     //alert("naka");
			jQuery("#community_naka").append(jQuery("<a/>").css("cursor","pointer").on('click', function(){callUrl(main);}).append( jQuery("<img/>").css("width", w + "%").attr({"src":"../../common/images/community2.png","alt":sub}).hover(function(){jQuery(this).attr('src', jQuery(this).attr('src').replace('2', '2_mover'));}, function(){if (!jQuery(this).hasClass('currentPage')){jQuery(this).attr('src', jQuery(this).attr('src').replace('2_mover', '2'));}})));
			jQuery("#community_str_naka").append('・').append(jQuery("<a/>").css("cursor","pointer").attr("href", main).on('click', function(){callUrl(main);return false;}).append(sub));
        	jQuery("#quiz_naka").append(jQuery("<a/>").css("cursor","pointer").on('click', function(){callUrl(main);}).append( jQuery("<img/>").attr({"src":"../../common/images/b_quiz.png","alt":"クイズページを開く"})));
	}else{
     //alert("soto");
			jQuery("#community_soto").append(jQuery("<a/>").css("cursor","pointer").attr("href", main).on('click', function($){window.open(this.href, '_blank');return false;}).append( jQuery("<img/>").css("width", w + "%").attr({"src":"../../common/images/community2.png" ,"alt":sub}).hover(function(){jQuery(this).attr('src', jQuery(this).attr('src').replace('2', '2_mover'));}, function(){if (!jQuery(this).hasClass('currentPage')){jQuery(this).attr('src', jQuery(this).attr('src').replace('2_mover', '2'));}})));
			jQuery("#community_str_soto").append('・').append(jQuery("<a/>").css("cursor","pointer").attr("href", main).on('click', function($){window.open(this.href, '_blank');return false;}).append(sub));
			jQuery("#quiz_soto").append(jQuery("<a/>").css("cursor","pointer").attr("href", main).on('click', function($){window.open(this.href, '_blank');return false;}).append( jQuery("<img/>").attr({"src":"../../common/images/b_quiz.png" ,"alt":"クイズページを開く"})));
	}
}

/**********************************************************************************************
 * CHiLO API パラメータ chiloWeb urlStrにurlを設定
 **********************************************************************************************/
var chiloData;
function chiloWebFunc(url,title){
	//alert(url);
	//top.chiloStep1(url,title);
	window.open(url);
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
/*
var chiloVar1;
var chiloVar2;
function chiloStep1(url,title){
	chiloVar1 = url;
	if(title){
	chiloVar2 = title;
	}else{
	chiloVar2 = "";
	}
	window.location.href = "http://127.0.0.1/chiloApiCallback/chiloStep2";
}
function chiloStep2(port){
var localhostApiDomain = "http://localhost:"+ port + "/";
var chiloAPI = "chiloWeb";
var chiloData = { urlStr: chiloVar1, headerTitle:chiloVar2, returnPage:"FtLibraryBtn" };
var urlPath = localhostApiDomain + chiloAPI + "?" + JSON.stringify(chiloData);
//alert("urlPath: "+ urlPath);
window.location.href = urlPath;
}
*/
  
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
	var urlPath = localhostApiDomain + chiloAPI + "?" + JSON.stringify(chiloData);
	//alert(urlPath);
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


/**********************************************************************************************
 * CHiLO API パラメータ callUrlScheme urlStrにurlを設定
 **********************************************************************************************/
var chiloData;
function callUrl(url){
	//var localhostApiDomain = "http://localhost:8080/";
	//var chiloAPI = "callUrlScheme";
	
	chiloData = {
		scheme: url
	};
	
	//callCHiLOjsonp(localhostApiDomain,chiloAPI,chiloData);
	chiloApiCallback("callUrl2");
}
function callUrl2(port){
	//alert(JSON.stringify(chiloData));
	var localhostApiDomain = "http://localhost:"+ port + "/";
	var chiloAPI = 'callUrlScheme';
	callCHiLOhref(localhostApiDomain,chiloAPI,chiloData);
}
