/**
 * Created by lijiye on 17-5-4.
 */
/* *******************
 *  growingIO统计代码
 ******************** */
var _vds = _vds || [];
window._vds = _vds;
(function(){
    _vds.push(['setAccountId', 'a84b517d69bd1b0c']);
    (function() {
        var vds = document.createElement('script');
        vds.type='text/javascript';
        vds.async = true;
        vds.src = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'dn-growing.qbox.me/vds.js';
        var s = document.getElementsByTagName('script')[0];
        s.parentNode.insertBefore(vds, s);
    })();
})();


/* *******************
 * cnzz网站统计分析平台
 * 市场 id　1257851220
 * 运营 id　1261397694
 * 老鹰运营 1263039440
 ******************** */

var cnzz_protocol = (("https:" == document.location.protocol) ? " https://" : " http://");
// document.write(unescape("%3Cspan id='cnzz_stat_icon_1257851220'  style='height:0;visibility:hidden;display:none' %3E%3C/span%3E%3Cscript src='" + cnzz_protocol + "s4.cnzz.com/z_stat.php%3Fid%3D1257851220' type='text/javascript'%3E%3C/script%3E"));
//
// document.write(unescape("%3Cspan id='cnzz_stat_icon_1261397694' style='height:0;visibility:hidden;display:none' %3E%3C/span%3E%3Cscript src='" + cnzz_protocol + "s95.cnzz.com/z_stat.php%3Fid%3D1261397694' type='text/javascript'%3E%3C/script%3E"));
//
// document.write(unescape("%3Cspan id='cnzz_stat_icon_1263039440' style='height:0;visibility:hidden;display:none' %3E%3C/span%3E%3Cscript src='" + cnzz_protocol + "s22.cnzz.com/z_stat.php%3Fid%3D1263039440' type='text/javascript'%3E%3C/script%3E"));

(function () {
    var hostname =  window.location.hostname;
    var _czc = _czc || [];
    var cnzz_id = '';
    var cnzzObj = {
        'mz.caiqr':'1260171157',
        'fx.caiqr':'1261834085',
        "mobile.caiqr": '1260689198',
        "wap.caiqr": '1261862708',
        "pt.caiqr": "1261862741",
        "3g.caiqr":"1261796618",
        "m.caiqr":"1257851220",
        "m.caiqr.":"1261397694",
        "m.laoyingcp":"1263039440",
    };
    for (var key in cnzzObj) {
        if(hostname.indexOf(key) >-1){
            cnzz_id = cnzzObj[key];
            document.write(unescape("%3Cspan style='height:0;visibility:hidden;display:none;' id='cnzz_stat_icon_" + cnzz_id + "'" + "'%3E%3C/span%3E%3Cscript src='" + cnzz_protocol + "s4.cnzz.com/z_stat.php%3Fid%3D" + cnzz_id + "'" + " type='text/javascript'%3E%3C/script%3E"));
            _czc.push(["_setAccount", cnzz_id]);
        }
    }
})();
