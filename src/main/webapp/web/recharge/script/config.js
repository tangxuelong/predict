/**
 * Created by tangxuelong on 2017/11/13.
 */
var config = {
    shareTitle: "双色球会员预测免费领取！",
    shareIcon: "https://predictapi.mojieai.com/web/newShare/img/shareIcon.png",
    shareDesc: "点击就送3天VIP，免费看双色球精选绝杀、上万彩民大数据汇总！",
    activityId: "201803001"
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
// 获取客户端token会用到
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