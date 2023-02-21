/**
 * Created by chenfangyu on 2018/08/15.
 */
var config = {
    shareTitle: "送你3注双色球大奖号码，一起来抢1000万！",
    shareIcon: "https://predictdebug.mojieai.com/web/share/img/200.png",
    shareDesc: "下载智慧彩票预测APP，精选预测免费看，冲击双色球千万大奖！"
};

//打开客户端
function openNativePage(params) {
    if (typeof(window.webkit) != "undefined") {
        window.webkit.messageHandlers.goNative.postMessage({body: params.replace("mjlottery://", "mjLottery://")})
    } else {
        if (typeof(webViewNative) != "undefined") {
            webViewNative.goNative(params)
        } else {
            window.location.href = params
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