package com.mojieai.predict.util.JDDefray;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDDefrayCodeConst {
    public static final String RETURN_PARAM_NULL = "RETURN_PARAM_NULL";//返回数据为null
    public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    public static final String OUT_TRADE_NO_EXIST = "OUT_TRADE_NO_EXIST";
    public static final String TRADE_NOT_EXIST = "TRADE_NOT_EXIST";
    public static final String ACCOUNT_BALANCE_NOT_ENOUGH = "ACCOUNT_BALANCE_NOT_ENOUGH";
    public static final String SUCCESS = "0000";

    public static final String TRADE_FINI = "FINI";
    public static final String TRADE_CLOS = "CLOS";
    public static final String TRADE_WPAR = "WPAR";
    public static final String TRADE_BUID = "BUID";
    public static final String TRADE_ACSU = "ACSU";

    private static Map<String, String> codeMap = new HashMap<String, String>();
    private static List<Map<String, Object>> jdBankList = new ArrayList<>();

    static {
        codeMap.put("0000", "成功");
        codeMap.put("EXPARTNER_INFO_UNCORRECT", "传入商户接口信息不正确");
        codeMap.put("ILLEGAL_SIGN", "签名验证出错");
        codeMap.put("ILLEGAL_ARGUMENT", "输入参数有错误");
        codeMap.put("ILLEGAL_AUTHORITY", "权限不正确");
        codeMap.put("CUSTOMER_NOT_EXIST", "提交会员不存在");
        codeMap.put("ILLEGAL_CHARSET", "字符集不合法");
        codeMap.put("ILLEGAL_CLIENT_IP", "客户端IP地址无权访问服务");
        codeMap.put("SYSTEM_ERROR", "系统错误");//（非失败，不能按失败处理）
        codeMap.put("OUT_TRADE_NO_EXIST", "外部交易号已经存在");// （非失败，不能按失败处理）
        codeMap.put("TRADE_NOT_EXIST", "交易不存在");// （如果查询返回此信息，不能以失败处理）
        codeMap.put("ILLEGAL_TRADE_TYPE", "无效交易类型");
        codeMap.put("BUYER_USER_NOT_EXIST", "买家会员不存在");
        codeMap.put("SELLER_USER_NOT_EXIST", "卖家会员不存在");
        codeMap.put("BUYER_SELLER_EQUAL", "买家、卖家是同一帐户 ");
        codeMap.put("USER_STATE_ERROR", "会员状态不正确");
        codeMap.put("COMMISION_ID_NOT_EXIST", "佣金收取帐户不存在");
        codeMap.put("COMMISION_SELLER_DUPLICATE", "收取佣金帐户和卖家是同一帐户");
        codeMap.put("COMMISION_FEE_OUT_OF_RANGE", "佣金金额超出范围");
        codeMap.put("TOTAL_FEE_OUT_OF_RANGE", "交易总金额超出范围");
        codeMap.put("ILLEGAL_AMOUNT_FORMAT", "非法金额格式");
        codeMap.put("ILLEGAL_TRADE_AMMOUT", "交易金额不正确");
        codeMap.put("ILLEGAL_TRADE_CURRENCY", "交易币种不正确");
        codeMap.put("SELF_TIMEOUT_NOT_SUPPORT", "不支持自定义超时");
        codeMap.put("COMMISION_NOT_SUPPORT", "不支持佣金 ");
        codeMap.put("VIRTUAL_NOT_SUPPORT", "不支持虚拟収货方式");
        codeMap.put("PAYMENT_LIMITED", "支付受限");
        codeMap.put("ILLEGAL_BANK_CARD_NO", "卡号不正确");
        codeMap.put("ILLEGAL_BANK_CARD_VALID_PERIOD", "卡有效期不正确");
        codeMap.put("ILLEGAL_ID_CARD_NO", "身份证号码不正确");
        codeMap.put("ILLEGAL_BANK_CARD_NAME", "持卡人姓名不正确");
        codeMap.put("ILLEGAL_BANK_CARD_TYPE", "卡类型不正确 ");
        codeMap.put("REFUND_FAILED", "退款失败");
        codeMap.put("CURRENT_PAY_CANNOT_REVOKE", "当前支付请求状态无法撤销");
        codeMap.put("CURRENT_USER_DIFFERENT_FROM", "当前用户和已登录绑定用户不一致");
        codeMap.put("ILLEGAL_PAY_TYPE", "无效支付类型");
        codeMap.put("USER_STATE_ERROR", "用户状态错误");
        codeMap.put("ACCOUNT_BALANCE_NOT_ENOUGH", "账户余额不足");

        String jdBankInfo = "[{\"bankCode\":\"BOLJ\",\"bankName\":\"龙江银行\",\"id\":\"1\"},{\"bankCode\":\"AHNX\"," +
                "\"bankName\":\"安徽农村信用社\",\"id\":\"2\"},{\"bankCode\":\"RHCZCZ\",\"bankName\":\"长子县融汇村镇银行\",\"id\":\"3\"},{\"bankCode\":\"HBNX\",\"bankName\":\"河北省农村信用社联合社\",\"id\":\"4\"},{\"bankCode\":\"MTB\",\"bankName\":\"浙江民泰商业银行\",\"id\":\"5\"},{\"bankCode\":\"JDIOU\",\"bankName\":\"京东白条\",\"id\":\"6\"},{\"bankCode\":\"BOZZ\",\"bankName\":\"枣庄银行(新)\",\"id\":\"7\"},{\"bankCode\":\"SDNX\",\"bankName\":\"山东农信\",\"id\":\"8\"},{\"bankCode\":\"XJNX\",\"bankName\":\"新疆农村信用社联合社\",\"id\":\"9\"},{\"bankCode\":\"BOWH\",\"bankName\":\"乌海银行\",\"id\":\"10\"},{\"bankCode\":\"HLJNX\",\"bankName\":\"黑龙江省农村信用社联合社\",\"id\":\"11\"},{\"bankCode\":\"NCCCB\",\"bankName\":\"四川天府银行\",\"id\":\"12\"},{\"bankCode\":\"SXCCB\",\"bankName\":\"绍兴银行\",\"id\":\"13\"},{\"bankCode\":\"YCCCB\",\"bankName\":\"宜昌市商业银行\",\"id\":\"14\"},{\"bankCode\":\"GXNX\",\"bankName\":\"广西农村信用社\",\"id\":\"15\"},{\"bankCode\":\"BOFX\",\"bankName\":\"阜新银行（新）\",\"id\":\"16\"},{\"bankCode\":\"CGSXCB\",\"bankName\":\"重庆三峡银行(新)\",\"id\":\"17\"},{\"bankCode\":\"ZZB\",\"bankName\":\"郑州银行\",\"id\":\"18\"},{\"bankCode\":\"ZYCCB\",\"bankName\":\"遵义商业银行\",\"id\":\"19\"},{\"bankCode\":\"BOYK\",\"bankName\":\"营口银行\",\"id\":\"20\"},{\"bankCode\":\"BOJL\",\"bankName\":\"吉林银行（新）\",\"id\":\"21\"},{\"bankCode\":\"BOXJ\",\"bankName\":\"华融湘江银行\",\"id\":\"22\"},{\"bankCode\":\"YZCZ\",\"bankName\":\"台州银座村镇银行\",\"id\":\"23\"},{\"bankCode\":\"SHXNX\",\"bankName\":\"陕西农村信用社\",\"id\":\"24\"},{\"bankCode\":\"BOHRB\",\"bankName\":\"哈尔滨银行股份有限公司\",\"id\":\"25\"},{\"bankCode\":\"KSRCB\",\"bankName\":\"昆山农村商业银行\",\"id\":\"26\"},{\"bankCode\":\"HNRCC\",\"bankName\":\"海南省农村信用社联合社\",\"id\":\"27\"},{\"bankCode\":\"WXRCB\",\"bankName\":\"无锡农村商业银行\",\"id\":\"28\"},{\"bankCode\":\"BOHH\",\"bankName\":\"新疆汇和银行\",\"id\":\"29\"},{\"bankCode\":\"UNQR\",\"bankName\":\"货到付款银联二维码（虚拟）\",\"id\":\"30\"},{\"bankCode\":\"BONC\",\"bankName\":\"江西银行\",\"id\":\"31\"},{\"bankCode\":\"LLPAY\",\"bankName\":\"连连支付\",\"id\":\"32\"},{\"bankCode\":\"PCXLZF\",\"bankName\":\"山东高速信联支付有限公司\",\"id\":\"33\"},{\"bankCode\":\"BOGL\",\"bankName\":\"桂林银行\",\"id\":\"34\"},{\"bankCode\":\"BOSJ\",\"bankName\":\"盛京银行（新）\",\"id\":\"35\"},{\"bankCode\":\"NCCB\",\"bankName\":\"南昌银行\",\"id\":\"36\"},{\"bankCode\":\"JNRCB\",\"bankName\":\"江南农村商业银行\",\"id\":\"37\"},{\"bankCode\":\"HSBC\",\"bankName\":\"汇丰银行\",\"id\":\"38\"},{\"bankCode\":\"BOXX\",\"bankName\":\"新乡银行\",\"id\":\"39\"},{\"bankCode\":\"BOTZ\",\"bankName\":\"台州银行\",\"id\":\"40\"},{\"bankCode\":\"PCYZF\",\"bankName\":\"中国电信天翼电子商务有限公司 \",\"id\":\"41\"},{\"bankCode\":\"BOLZ\",\"bankName\":\"柳州银行\",\"id\":\"42\"},{\"bankCode\":\"WOORI\",\"bankName\":\"友利银行\",\"id\":\"43\"},{\"bankCode\":\"BOBBW\",\"bankName\":\"广西北部湾银行（新）\",\"id\":\"44\"},{\"bankCode\":\"JLNX\",\"bankName\":\"吉林省农村信用社联合社\",\"id\":\"45\"},{\"bankCode\":\"深圳清结算中心\",\"bankName\":\"SZFS\",\"id\":\"46\"},{\"bankCode\":\"SZFS\",\"bankName\":\"深圳清结算中心\",\"id\":\"47\"},{\"bankCode\":\"YRRCB\",\"bankName\":\"黄河农村商业银行\",\"id\":\"48\"},{\"bankCode\":\"BHRCB\",\"bankName\":\"天津滨海农村商业银行\",\"id\":\"49\"},{\"bankCode\":\"HBCL\",\"bankName\":\"湖北银行股份有限公司\",\"id\":\"50\"},{\"bankCode\":\"TJRCB\",\"bankName\":\"天津农商\",\"id\":\"51\"},{\"bankCode\":\"BOJS\",\"bankName\":\"晋商银行\",\"id\":\"52\"},{\"bankCode\":\"BOCS\",\"bankName\":\"长沙银行（新）\",\"id\":\"53\"},{\"bankCode\":\"ZJNX\",\"bankName\":\"浙江省农村信用社联合社\",\"id\":\"54\"},{\"bankCode\":\"BEA\",\"bankName\":\"东亚银行(新)\",\"id\":\"55\"},{\"bankCode\":\"PHFUND\",\"bankName\":\"鹏华基金管理有限公司\",\"id\":\"56\"},{\"bankCode\":\"JSNX\",\"bankName\":\"江苏省农村信用社联合社\",\"id\":\"57\"},{\"bankCode\":\"ZJMTB\",\"bankName\":\"浙江民泰商业银行(新)\",\"id\":\"58\"},{\"bankCode\":\"ZJWSCB\",\"bankName\":\"浙江网商银行\",\"id\":\"59\"},{\"bankCode\":\"BOJN\",\"bankName\":\"济宁银行(新)\",\"id\":\"60\"},{\"bankCode\":\"LZCB\",\"bankName\":\"兰州银行（新）\",\"id\":\"61\"},{\"bankCode\":\"WEBANK\",\"bankName\":\"深圳前海微众银行股份有限公司\",\"id\":\"62\"},{\"bankCode\":\"UCCB\",\"bankName\":\"乌鲁木齐市商业银行\",\"id\":\"63\"},{\"bankCode\":\"ZJKCCB\",\"bankName\":\"张家口市商业银行\",\"id\":\"64\"},{\"bankCode\":\"BOXIA\",\"bankName\":\"西安银行\",\"id\":\"65\"},{\"bankCode\":\"EXIMBANK\",\"bankName\":\"中国进出口银行\",\"id\":\"66\"},{\"bankCode\":\"URCB\",\"bankName\":\"农村合作银行\",\"id\":\"67\"},{\"bankCode\":\"RCB\",\"bankName\":\"农村商业银行\",\"id\":\"68\"},{\"bankCode\":\"CDB\",\"bankName\":\"承德银行\",\"id\":\"69\"},{\"bankCode\":\"RCC\",\"bankName\":\"农村信用社\",\"id\":\"70\"},{\"bankCode\":\"CCU\",\"bankName\":\"城市信用社\",\"id\":\"71\"},{\"bankCode\":\"ADBC\",\"bankName\":\"中国农业发展银行\",\"id\":\"72\"},{\"bankCode\":\"CZB\",\"bankName\":\"浙商银行\",\"id\":\"73\"},{\"bankCode\":\"CBC\",\"bankName\":\"城市商业银行\",\"id\":\"74\"},{\"bankCode\":\"YBCCB\",\"bankName\":\"宜宾市商业银行\",\"id\":\"75\"},{\"bankCode\":\"LZCCB\",\"bankName\":\"泸州商业银行\",\"id\":\"76\"},{\"bankCode\":\"CCTQGB\",\"bankName\":\"重庆三峡银行\",\"id\":\"77\"},{\"bankCode\":\"HNNX\",\"bankName\":\"河南省农村信用社联合社\",\"id\":\"78\"},{\"bankCode\":\"JLB\",\"bankName\":\"吉林银行\",\"id\":\"79\"},{\"bankCode\":\"HRBCB\",\"bankName\":\"哈尔滨银行\",\"id\":\"80\"},{\"bankCode\":\"PZHCCB\",\"bankName\":\"攀枝花市商业银行\",\"id\":\"81\"},{\"bankCode\":\"BOASH\",\"bankName\":\"鞍山银行\",\"id\":\"82\"},{\"bankCode\":\"WHCCB\",\"bankName\":\"威海市商业银行\",\"id\":\"83\"},{\"bankCode\":\"WHNX\",\"bankName\":\"武汉市农村信用社联合社\",\"id\":\"84\"},{\"bankCode\":\"TEST\",\"bankName\":\"测试银行\",\"id\":\"85\"},{\"bankCode\":\"TJWQCZ\",\"bankName\":\"天津武清村镇银行\",\"id\":\"86\"},{\"bankCode\":\"CCFCCB\",\"bankName\":\"城市商业银行资金清算中心\",\"id\":\"87\"},{\"bankCode\":\"HANABANK\",\"bankName\":\"韩亚银行（新）\",\"id\":\"88\"},{\"bankCode\":\"SNCCB\",\"bankName\":\"遂宁市商业银行\",\"id\":\"89\"},{\"bankCode\":\"BOLF\",\"bankName\":\"廊坊银行（新）\",\"id\":\"90\"},{\"bankCode\":\"GSNX\",\"bankName\":\"甘肃省农村信用社联合社\",\"id\":\"91\"},{\"bankCode\":\"JXNX\",\"bankName\":\"江西省农村信用社联合社\",\"id\":\"92\"},{\"bankCode\":\"LYCB\",\"bankName\":\"辽阳银行\",\"id\":\"93\"},{\"bankCode\":\"YKYH\",\"bankName\":\"营口沿海银行\",\"id\":\"94\"},{\"bankCode\":\"UNIONPAY\",\"bankName\":\"银联在线支付\",\"id\":\"95\"},{\"bankCode\":\"BOXC\",\"bankName\":\"西昌金信村镇银行\",\"id\":\"96\"},{\"bankCode\":\"BODG\",\"bankName\":\"东莞银行\",\"id\":\"97\"},{\"bankCode\":\"JXCCB\",\"bankName\":\"嘉兴银行\",\"id\":\"98\"},{\"bankCode\":\"CGB\",\"bankName\":\"广东发展银行\",\"id\":\"99\"},{\"bankCode\":\"BOLY\",\"bankName\":\"洛阳银行\",\"id\":\"100\"},{\"bankCode\":\"TAB\",\"bankName\":\"泰安商业银行\",\"id\":\"101\"},{\"bankCode\":\"QSB\",\"bankName\":\"齐商银行\",\"id\":\"102\"},{\"bankCode\":\"LWB\",\"bankName\":\"莱芜商业银行\",\"id\":\"103\"},{\"bankCode\":\"ICBC\",\"bankName\":\"中国工商银行\",\"id\":\"104\"},{\"bankCode\":\"ABC\",\"bankName\":\"中国农业银行\",\"id\":\"105\"},{\"bankCode\":\"BOC\",\"bankName\":\"中国银行\",\"id\":\"106\"},{\"bankCode\":\"CCB\",\"bankName\":\"中国建设银行\",\"id\":\"107\"},{\"bankCode\":\"BCM\",\"bankName\":\"交通银行\",\"id\":\"108\"},{\"bankCode\":\"CMB\",\"bankName\":\"招商银行\",\"id\":\"109\"},{\"bankCode\":\"CEB\",\"bankName\":\"中国光大银行\",\"id\":\"110\"},{\"bankCode\":\"CMBC\",\"bankName\":\"中国民生银行\",\"id\":\"111\"},{\"bankCode\":\"CIB\",\"bankName\":\"兴业银行\",\"id\":\"112\"},{\"bankCode\":\"HXB\",\"bankName\":\"华夏银行\",\"id\":\"113\"},{\"bankCode\":\"CITIC\",\"bankName\":\"中信银行\",\"id\":\"114\"},{\"bankCode\":\"PSBC\",\"bankName\":\"中国邮政储蓄银行\",\"id\":\"115\"},{\"bankCode\":\"PAB\",\"bankName\":\"平安银行\",\"id\":\"116\"},{\"bankCode\":\"GDB\",\"bankName\":\"广东发展银行\",\"id\":\"117\"},{\"bankCode\":\"SPDB\",\"bankName\":\"浦东发展银行\",\"id\":\"118\"},{\"bankCode\":\"SDB\",\"bankName\":\"深圳发展银行\",\"id\":\"119\"},{\"bankCode\":\"BOB\",\"bankName\":\"北京银行\",\"id\":\"120\"},{\"bankCode\":\"HZB\",\"bankName\":\"杭州银行\",\"id\":\"121\"},{\"bankCode\":\"NJCB\",\"bankName\":\"南京银行\",\"id\":\"122\"},{\"bankCode\":\"BOS\",\"bankName\":\"上海银行\",\"id\":\"123\"},{\"bankCode\":\"NBCB\",\"bankName\":\"宁波银行\",\"id\":\"124\"},{\"bankCode\":\"JSB\",\"bankName\":\"江苏银行\",\"id\":\"125\"},{\"bankCode\":\"JHCCB\",\"bankName\":\"金华银行\",\"id\":\"126\"},{\"bankCode\":\"BONX\",\"bankName\":\"宁夏银行\",\"id\":\"127\"},{\"bankCode\":\"FDB\",\"bankName\":\"富滇银行\",\"id\":\"128\"},{\"bankCode\":\"EGB\",\"bankName\":\"恒丰银行\",\"id\":\"129\"},{\"bankCode\":\"CQCB\",\"bankName\":\"重庆银行\",\"id\":\"130\"},{\"bankCode\":\"WZCB\",\"bankName\":\"温州银行\",\"id\":\"131\"},{\"bankCode\":\"TJCB\",\"bankName\":\"天津银行\",\"id\":\"132\"},{\"bankCode\":\"BODL\",\"bankName\":\"大连银行\",\"id\":\"133\"},{\"bankCode\":\"CBHB\",\"bankName\":\"渤海银行\",\"id\":\"134\"},{\"bankCode\":\"HKB\",\"bankName\":\"汉口银行\",\"id\":\"135\"},{\"bankCode\":\"JZB\",\"bankName\":\"锦州银行\",\"id\":\"136\"},{\"bankCode\":\"HSB\",\"bankName\":\"徽商银行\",\"id\":\"137\"},{\"bankCode\":\"HBB\",\"bankName\":\"河北银行\",\"id\":\"138\"},{\"bankCode\":\"SCBL\",\"bankName\":\"渣打银行\",\"id\":\"139\"},{\"bankCode\":\"BEEB\",\"bankName\":\"鄞州银行\",\"id\":\"140\"},{\"bankCode\":\"JJCCB\",\"bankName\":\"九江银行\",\"id\":\"141\"},{\"bankCode\":\"BSB\",\"bankName\":\"包商银行\",\"id\":\"142\"},{\"bankCode\":\"GZCB\",\"bankName\":\"广州银行\",\"id\":\"143\"},{\"bankCode\":\"HDCB\",\"bankName\":\"邯郸银行\",\"id\":\"144\"},{\"bankCode\":\"JCCB\",\"bankName\":\"晋城银行\",\"id\":\"145\"},{\"bankCode\":\"LZB\",\"bankName\":\"兰州银行\",\"id\":\"146\"},{\"bankCode\":\"QLB\",\"bankName\":\"齐鲁银行\",\"id\":\"147\"},{\"bankCode\":\"WFCCB\",\"bankName\":\"潍坊银行\",\"id\":\"148\"},{\"bankCode\":\"ZJTLCB\",\"bankName\":\"浙江泰隆银行\",\"id\":\"149\"},{\"bankCode\":\"CZCB\",\"bankName\":\"浙江稠州银行\",\"id\":\"150\"},{\"bankCode\":\"XTB\",\"bankName\":\"邢台市商业银行\",\"id\":\"151\"},{\"bankCode\":\"SRCB\",\"bankName\":\"上海农村商业银行\",\"id\":\"152\"},{\"bankCode\":\"YDRCB\",\"bankName\":\"尧都农村商业银行\",\"id\":\"153\"},{\"bankCode\":\"GRCB\",\"bankName\":\"广州农村商业银行\",\"id\":\"154\"},{\"bankCode\":\"CQRCB\",\"bankName\":\"重庆农村商业银行\",\"id\":\"155\"},{\"bankCode\":\"BJRCB\",\"bankName\":\"北京农村商业银行\",\"id\":\"156\"},{\"bankCode\":\"WJRCB\",\"bankName\":\"吴江农村商业银行\",\"id\":\"157\"},{\"bankCode\":\"DRCB\",\"bankName\":\"东莞农村商业银行\",\"id\":\"158\"},{\"bankCode\":\"SZRCB\",\"bankName\":\"深圳农村商业银行\",\"id\":\"159\"},{\"bankCode\":\"CDRCB\",\"bankName\":\"成都农村商业银行\",\"id\":\"160\"},{\"bankCode\":\"SDEB\",\"bankName\":\"顺德农村商业银行\",\"id\":\"161\"},{\"bankCode\":\"QDCCB\",\"bankName\":\"青岛银行\",\"id\":\"162\"},{\"bankCode\":\"RZB\",\"bankName\":\"日照银行\",\"id\":\"163\"},{\"bankCode\":\"CHAB\",\"bankName\":\"长安银行\",\"id\":\"164\"},{\"bankCode\":\"YTCB\",\"bankName\":\"烟台银行\",\"id\":\"165\"},{\"bankCode\":\"BOCD\",\"bankName\":\"成都银行\",\"id\":\"166\"},{\"bankCode\":\"GDNYB\",\"bankName\":\"广东南粤银行\",\"id\":\"167\"},{\"bankCode\":\"DZB\",\"bankName\":\"德州商业银行\",\"id\":\"168\"},{\"bankCode\":\"SXNX\",\"bankName\":\"山西省农村信用社联合社\",\"id\":\"169\"},{\"bankCode\":\"BOIMC\",\"bankName\":\"内蒙古银行\",\"id\":\"170\"},{\"bankCode\":\"LSB\",\"bankName\":\"临商银行\",\"id\":\"171\"},{\"bankCode\":\"JZCB\",\"bankName\":\"晋中银行\",\"id\":\"172\"},{\"bankCode\":\"ZGCCB\",\"bankName\":\"自贡市商业银行\",\"id\":\"173\"},{\"bankCode\":\"JYRCB\",\"bankName\":\"江阴农商银行\",\"id\":\"174\"},{\"bankCode\":\"BOHS\",\"bankName\":\"衡水银行\",\"id\":\"175\"},{\"bankCode\":\"DGCB\",\"bankName\":\"东莞银行(新)\",\"id\":\"176\"},{\"bankCode\":\"DYLSCZ\",\"bankName\":\"东营莱商村镇银行\",\"id\":\"177\"},{\"bankCode\":\"BOZH\",\"bankName\":\"庄河汇通村镇银行\",\"id\":\"178\"},{\"bankCode\":\"BOGS\",\"bankName\":\"甘肃银行\",\"id\":\"179\"},{\"bankCode\":\"99BILL\",\"bankName\":\"快钱\",\"id\":\"180\"},{\"bankCode\":\"QHNX\",\"bankName\":\"青海省农村信用社联合社\",\"id\":\"181\"},{\"bankCode\":\"GZNX\",\"bankName\":\"贵州农村信用社\",\"id\":\"182\"},{\"bankCode\":\"SHBANK\",\"bankName\":\"新韩银行\",\"id\":\"183\"},{\"bankCode\":\"ORDOS\",\"bankName\":\"鄂尔多斯银行\",\"id\":\"184\"},{\"bankCode\":\"CHINAUMS\",\"bankName\":\"银联商务有限公司\",\"id\":\"185\"},{\"bankCode\":\"HURCB\",\"bankName\":\"湖北省农村信用社联合社\",\"id\":\"186\"},{\"bankCode\":\"TSCCB\",\"bankName\":\"唐山市商业银行\",\"id\":\"187\"},{\"bankCode\":\"BOMOCK\",\"bankName\":\"MOCK银行\",\"id\":\"188\"},{\"bankCode\":\"JFCZ\",\"bankName\":\"临朐聚丰村镇银行\",\"id\":\"189\"},{\"bankCode\":\"BOGY\",\"bankName\":\"贵阳银行\",\"id\":\"190\"},{\"bankCode\":\"JXAYRCB\",\"bankName\":\"江西安远农村商业银行\",\"id\":\"191\"},{\"bankCode\":\"LZRCB\",\"bankName\":\"兰州农商行\",\"id\":\"192\"},{\"bankCode\":\"MFRCB\",\"bankName\":\"江苏民丰农村商业银行\",\"id\":\"193\"},{\"bankCode\":\"SCNX\",\"bankName\":\"四川农村信用社\",\"id\":\"194\"},{\"bankCode\":\"BODY\",\"bankName\":\"德阳银行\",\"id\":\"195\"},{\"bankCode\":\"HUNNX\",\"bankName\":\"湖南农村信用社\",\"id\":\"196\"},{\"bankCode\":\"UMP\",\"bankName\":\"联动优势\",\"id\":\"197\"},{\"bankCode\":\"SFCZ\",\"bankName\":\"舜丰村镇银行\",\"id\":\"198\"},{\"bankCode\":\"RCU\",\"bankName\":\"农商银行公共(代付跨行使用)\",\"id\":\"199\"},{\"bankCode\":\"ZYB\",\"bankName\":\"中原银行\",\"id\":\"200\"},{\"bankCode\":\"MYBANK\",\"bankName\":\"浙江网商银行\",\"id\":\"201\"},{\"bankCode\":\"HBSB\",\"bankName\":\"鹤壁银行\",\"id\":\"202\"},{\"bankCode\":\"XGCCB\",\"bankName\":\"孝感市商业银行\",\"id\":\"203\"},{\"bankCode\":\"HZUB\",\"bankName\":\"杭州联合农村商业银行\",\"id\":\"204\"},{\"bankCode\":\"NXYQS\",\"bankName\":\"农信银资金清算中心\",\"id\":\"205\"},{\"bankCode\":\"BOZJ\",\"bankName\":\"郑州银行（新）\",\"id\":\"206\"},{\"bankCode\":\"YNNX\",\"bankName\":\"云南省农村信用社联合社\",\"id\":\"207\"},{\"bankCode\":\"XMIB\",\"bankName\":\"厦门国际银行\",\"id\":\"208\"},{\"bankCode\":\"HFCBC\",\"bankName\":\"大洼恒丰村镇银行\",\"id\":\"209\"},{\"bankCode\":\"CJCCB\",\"bankName\":\"江苏长江商业银行\",\"id\":\"210\"},{\"bankCode\":\"CSRCB\",\"bankName\":\"江苏常熟农村商业银行\",\"id\":\"211\"},{\"bankCode\":\"DYCCB\",\"bankName\":\"东营银行\",\"id\":\"212\"},{\"bankCode\":\"LNNX\",\"bankName\":\"辽宁省农村信用社联合社\",\"id\":\"213\"},{\"bankCode\":\"TCRCB\",\"bankName\":\"太仓农村商业银行\",\"id\":\"214\"},{\"bankCode\":\"ZJGRCB\",\"bankName\":\"张家港农村商业银行\",\"id\":\"215\"},{\"bankCode\":\"YNHT\",\"bankName\":\"云南红塔银行\",\"id\":\"216\"},{\"bankCode\":\"BOPDS\",\"bankName\":\"平顶山银行\",\"id\":\"217\"},{\"bankCode\":\"ZHHRCB\",\"bankName\":\"珠海华润银行\",\"id\":\"218\"},{\"bankCode\":\"GDXXRCB\",\"bankName\":\"广东新兴农村商业银行\",\"id\":\"219\"},{\"bankCode\":\"BOCZ\",\"bankName\":\"沧州银行（新）\",\"id\":\"220\"},{\"bankCode\":\"BOHX\",\"bankName\":\"福建海峡银行(新)\",\"id\":\"221\"},{\"bankCode\":\"BOHN\",\"bankName\":\"海南银行\",\"id\":\"222\"},{\"bankCode\":\"CITIB\",\"bankName\":\"花旗银行\",\"id\":\"223\"},{\"bankCode\":\"BOXM\",\"bankName\":\"厦门银行(新)\",\"id\":\"224\"},{\"bankCode\":\"BOBD\",\"bankName\":\"保定银行\",\"id\":\"225\"},{\"bankCode\":\"BOQZ\",\"bankName\":\"泉州银行（新）\",\"id\":\"226\"},{\"bankCode\":\"BOHZ\",\"bankName\":\"湖州银行（新）\",\"id\":\"227\"},{\"bankCode\":\"FJHXB\",\"bankName\":\"福建海峡银行\",\"id\":\"228\"},{\"bankCode\":\"BOSZ\",\"bankName\":\"苏州银行\",\"id\":\"229\"},{\"bankCode\":\"JNB\",\"bankName\":\"济宁银行\",\"id\":\"230\"},{\"bankCode\":\"FXB\",\"bankName\":\"阜新银行\",\"id\":\"231\"},{\"bankCode\":\"HKBEA\",\"bankName\":\"东亚银行\",\"id\":\"232\"},{\"bankCode\":\"BOAS\",\"bankName\":\"鞍山银行(新)\",\"id\":\"233\"},{\"bankCode\":\"CSXB\",\"bankName\":\"湖南三湘银行\",\"id\":\"234\"},{\"bankCode\":\"CSCB\",\"bankName\":\"长沙银行\",\"id\":\"235\"},{\"bankCode\":\"CZCCB\",\"bankName\":\"长治银行\",\"id\":\"236\"},{\"bankCode\":\"BOTL\",\"bankName\":\"铁岭银行\",\"id\":\"237\"},{\"bankCode\":\"BOSHR\",\"bankName\":\"上饶市商业银行\",\"id\":\"238\"},{\"bankCode\":\"ZZHB\",\"bankName\":\"枣庄银行\",\"id\":\"239\"},{\"bankCode\":\"YYCCB\",\"bankName\":\"岳阳市商业银行\",\"id\":\"240\"},{\"bankCode\":\"XHDLCZ\",\"bankName\":\"大连开发区鑫汇村镇银行\",\"id\":\"241\"},{\"bankCode\":\"YQCCB\",\"bankName\":\"阳泉市商业银行\",\"id\":\"242\"},{\"bankCode\":\"ZZCCB\",\"bankName\":\"株洲市商业银行\",\"id\":\"243\"},{\"bankCode\":\"BONY\",\"bankName\":\"南阳银行\",\"id\":\"244\"},{\"bankCode\":\"LCCB\",\"bankName\":\"廊坊银行\",\"id\":\"245\"},{\"bankCode\":\"BOQH\",\"bankName\":\"青海银行\",\"id\":\"246\"},{\"bankCode\":\"BOQHD\",\"bankName\":\"秦皇岛银行\",\"id\":\"247\"},{\"bankCode\":\"GDNX\",\"bankName\":\"广东省农村信用社联合社\",\"id\":\"248\"},{\"bankCode\":\"NUCC\",\"bankName\":\"网联支付\",\"id\":\"249\"},{\"bankCode\":\"BOCY\",\"bankName\":\"朝阳银行\",\"id\":\"250\"},{\"bankCode\":\"BOHLD\",\"bankName\":\"葫芦岛银行\",\"id\":\"251\"},{\"bankCode\":\"DTCCB\",\"bankName\":\"大同市商业银行\",\"id\":\"252\"},{\"bankCode\":\"LJBC\",\"bankName\":\"龙江银行1\",\"id\":\"253\"},{\"bankCode\":\"XFCCB\",\"bankName\":\"襄樊市商业银行\",\"id\":\"254\"},{\"bankCode\":\"XMCCB\",\"bankName\":\"厦门银行\",\"id\":\"255\"},{\"bankCode\":\"BOFS\",\"bankName\":\"抚顺银行\",\"id\":\"256\"},{\"bankCode\":\"PJCCB\",\"bankName\":\"盘锦市商业银行\",\"id\":\"257\"},{\"bankCode\":\"YXCCB\",\"bankName\":\"玉溪市商业银行\",\"id\":\"258\"},{\"bankCode\":\"GZCCB\",\"bankName\":\"赣州银行\",\"id\":\"259\"},{\"bankCode\":\"KLB\",\"bankName\":\"昆仑银行\",\"id\":\"260\"},{\"bankCode\":\"BOBBG\",\"bankName\":\"广西北部湾银行\",\"id\":\"261\"},{\"bankCode\":\"URB\",\"bankName\":\"联合村镇银行\",\"id\":\"262\"},{\"bankCode\":\"BOGZ\",\"bankName\":\"贵州银行\",\"id\":\"263\"},{\"bankCode\":\"FJNX\",\"bankName\":\"福建省农村信用社联合社\",\"id\":\"264\"},{\"bankCode\":\"PCSYXZF\",\"bankName\":\"商银信支付服务有限责任公司\",\"id\":\"265\"},{\"bankCode\":\"SHJB\",\"bankName\":\"盛京银行\",\"id\":\"266\"},{\"bankCode\":\"BOQZH\",\"bankName\":\"泉州银行\",\"id\":\"267\"},{\"bankCode\":\"HZCCB\",\"bankName\":\"湖州银行\",\"id\":\"268\"},{\"bankCode\":\"ANNX\",\"bankName\":\"安徽农村信用社\",\"id\":\"269\"},{\"bankCode\":\"PCKLT\",\"bankName\":\"开联通支付服务有限公司\",\"id\":\"270\"},{\"bankCode\":\"PCSFT\",\"bankName\":\"北京首采联合电子商务有限责任公司\",\"id\":\"271\"},{\"bankCode\":\"WQTJCZ\",\"bankName\":\"富滇银行(新)\",\"id\":\"272\"},{\"bankCode\":\"DLRCB\",\"bankName\":\"大连农村商业银行\",\"id\":\"273\"},{\"bankCode\":\"TRCB\",\"bankName\":\"天津农村商业银行\",\"id\":\"274\"},{\"bankCode\":\"HUBNX\",\"bankName\":\"湖北省农村信用社联合社\",\"id\":\"275\"},{\"bankCode\":\"OTH\",\"bankName\":\"韩亚银行\",\"id\":\"276\"},{\"bankCode\":\"CCQTGB\",\"bankName\":\"重庆三峡银行\",\"id\":\"277\"},{\"bankCode\":\"CXRCB\",\"bankName\":\"慈溪农村商业银行\",\"id\":\"278\"},{\"bankCode\":\"BOKL\",\"bankName\":\"昆仑银行（新）\",\"id\":\"279\"},{\"bankCode\":\"ORDOSB\",\"bankName\":\"鄂尔多斯银行1\",\"id\":\"280\"},{\"bankCode\":\"CITYB\",\"bankName\":\"城市商业银行公共(代付跨行使用)\",\"id\":\"281\"},{\"bankCode\":\"WHRCB\",\"bankName\":\"武汉农村商业银行\",\"id\":\"282\"},{\"bankCode\":\"NMGNX\",\"bankName\":\"内蒙古农村信用社联合社\",\"id\":\"283\"},{\"bankCode\":\"BOSR\",\"bankName\":\"上饶银行\",\"id\":\"284\"}]";
        jdBankList = JSONObject.parseObject(jdBankInfo, ArrayList.class);
    }

    public static boolean isContainCode(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        return codeMap.containsKey(code);
    }

    public static String getBankCardType(String bankName) {
        if (StringUtils.isBlank(bankName)) {
            return null;
        }
        for (Map<String, Object> bankInfo : jdBankList) {
            if (bankInfo.get("bankName").toString().contains(bankName)) {
                return bankInfo.get("bankCode").toString();
            }
        }
        return null;
    }

}