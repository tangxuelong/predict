/**
 * Created by tangxuelong on 2017/11/13.
 */
var config = {
    shareTitle: "送你3注双色球大奖号码，一起来抢1000万！",
    shareIcon: "https://predictdebug.mojieai.com/web/share/img/200.png",
    shareDesc: "下载智慧彩票预测APP，精选预测免费看，冲击双色球千万大奖！"
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