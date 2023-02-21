/**
 * Created by tangxuelong on 2017/11/13.
 */
var config = {
    shareTitle: "答题赢千元现金！",
    shareIcon: "https://predictapi.mojieai.com/web/newShare/img/shareIcon.png",
    shareDesc: "点击就送3天VIP，免费看双色球精选绝杀、上万彩民大数据汇总！",
    activityId: "201802002"
};

//打开客户端
function openNativePage(params) {
    if (typeof(window.webkit) != "undefined") {
        window.webkit.messageHandlers.goNative.postMessage({body: params})
    } else {
        if (typeof(webViewNative) != "undefined") {
            webViewNative.goNative(params)
        } else {
            window.location.href = params
        }
    }
}
// 右上角分享
function shareRight(params) {
    if (typeof(window.webkit) != "undefined") {
        window.webkit.messageHandlers.outerShareSetData.postMessage({body: params})
    } else {
        if (typeof(webViewNative) != "undefined") {
            webViewNative.outerShareSetData(params)
        }
    }
}
// 调用分享按钮
function share(params) {
    if (typeof(window.webkit) != "undefined") {
        window.webkit.messageHandlers.innerShareOpen.postMessage({body: params})
    } else {
        if (typeof(webViewNative) != "undefined") {
            //alert("android share");
            webViewNative.innerShareOpen(params)
        }
    }
}
function copyWxFunction() {
    var params = {"popupStr": "复制成功", "copyStr": "zhihuicp"};
    if (typeof(window.webkit) != "undefined") {
        window.webkit.messageHandlers.copyString.postMessage({body: JSON.stringify(params)})
    } else {
        if (typeof(webViewNative) != "undefined") {
            webViewNative.copyString(JSON.stringify(params));
        }
    }
}

function getCookie(c_name) {
    if (document.cookie.length > 0) {
        c_start = document.cookie.indexOf(c_name + "=")
        if (c_start != -1) {
            c_start = c_start + c_name.length + 1
            c_end = document.cookie.indexOf(";", c_start)
            if (c_end == -1) c_end = document.cookie.length
            return unescape(document.cookie.substring(c_start, c_end))
        }
    }
    return ""
}
function setCookie(c_name, value, expiredays) {
    var exdate = new Date()
    exdate.setDate(exdate.getDate() + expiredays)
    document.cookie = c_name + "=" + escape(value) +
        ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString())
}

function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    if (r != null) return unescape(r[2]);
    return null; //返回参数值
}
