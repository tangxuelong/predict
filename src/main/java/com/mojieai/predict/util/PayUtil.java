package com.mojieai.predict.util;

import com.alibaba.fastjson.JSONObject;
import com.jd.jr.pay.gate.signature.util.JdPayXmlUtil;
import com.jd.jr.pay.gate.signature.vo.JdPayBaseResponse;
import com.jd.jr.pay.gate.signature.vo.JdPayResponse;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.util.JDPay.RSACoder;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;

/**
 * Created by tangxuelong on 2017/12/18.
 */
public class PayUtil {
    public static final String WX_PAY_SIGN_ERROR_MSG = "\"<xml>\" + \"<return_code><![CDATA[FAIL]]></return_code>\"\n" +
            "                        + \"<return_msg><![CDATA[通知签名验证失败]]></return_msg>\" + \"</xml> \"";
    public static final String WX_PAY_SIGN_SUCCESS_MSG = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
            + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
    public static final String WX_PAY_SIGN_PARAMS_ERROR_MSG = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
            + "<return_msg><![CDATA[参数错误]]></return_msg>" + "</xml> ";
    public static final String WX_PAY_FAILED_MSG = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
            + "<return_msg><![CDATA[交易失败]]></return_msg>" + "</xml> ";

    public static final String ALI_PAY_SUCCESS_MSG = "success";
    public static final String ALI_PAY_FAILURE_MSG = "failure";

    /*
     * 微信支付随机字符串
     * */
    public static String wxNonceStr() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String res = "";
        for (int i = 0; i < 16; i++) {
            Random rd = new Random();
            res += chars.charAt(rd.nextInt(chars.length() - 1));
        }
        return res;
    }

    /*
     * 微信支付签名
     * */
    public static String wxSignStr(String characterEncoding, SortedMap<String, String> parameters, String key) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + key);
        String sign = Md5Util.getMD5String(sb.toString(), characterEncoding).toUpperCase();
        return sign;
    }

    public static String jdSignStr(SortedMap<String, String> parameters, String privateKey) {
        String sourceSignString = getSortedKeyString(parameters);
        String result = "";
        String sha256SourceSignString = SHAUtil.Encrypt(sourceSignString, null);
//        String rsaStr = RSAUtil.sign(sha256SourceSignString, privateKey);
        try {
            byte[] newsks = RSACoder.encryptByPrivateKey(sha256SourceSignString.getBytes("UTF-8"), privateKey);
            result = RSACoder.encryptBASE64(newsks);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getSortedKeyString(SortedMap<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Object v = entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        String result = sb.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /*
     * 将请求参数转换为xml格式的string
     * */
    public static String getRequestXml(SortedMap<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if ("attach".equalsIgnoreCase(k) || "body".equalsIgnoreCase(k)) {
                sb.append("<" + k + ">" + "<![CDATA[" + v + "]]></" + k + ">");
            } else {
                sb.append("<" + k + ">" + v + "</" + k + ">");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 返回给微信的参数
     */
    public static String setXML(String return_code, String return_msg) {
        return "<xml><return_code><![CDATA[" + return_code + "]]></return_code><return_msg><![CDATA[" + return_msg
                + "]]></return_msg></xml>";
    }


    /**
     * 校验签名正确,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
     */
    public static boolean checkPaySign(String characterEncoding, SortedMap<Object, Object> packageParams, String key) {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            if (!"sign".equals(k) && null != v && !"".equals(v)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + key);
        // 算出摘要
        String mySign = Md5Util.getMD5String(sb.toString(), characterEncoding).toLowerCase();
        String checkPaySign = ((String) packageParams.get("sign")).toLowerCase();
        return checkPaySign.equals(mySign);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertNodesFromXml(Document doc) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (doc == null)
            return map;
        Element root = doc.getRootElement();
        for (Iterator iterator = root.elementIterator(); iterator.hasNext(); ) {
            Element e = (Element) iterator.next();
            List list = e.elements();
            if (list.size() > 0) {
                map.put(e.getName(), convertNodesFromXml(e));
            } else
                map.put(e.getName(), e.getText());
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static Map convertNodesFromXml(Element e) {
        Map map = new HashMap();
        List list = e.elements();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Element iter = (Element) list.get(i);
                List mapList = new ArrayList();

                if (iter.elements().size() > 0) {
                    Map m = convertNodesFromXml(iter);
                    if (map.get(iter.getName()) != null) {
                        Object obj = map.get(iter.getName());
                        if (!obj.getClass().getName().equals("java.util.ArrayList")) {
                            mapList = new ArrayList();
                            mapList.add(obj);
                            mapList.add(m);
                        }
                        if (obj.getClass().getName().equals("java.util.ArrayList")) {
                            mapList = (List) obj;
                            mapList.add(m);
                        }
                        map.put(iter.getName(), mapList);
                    } else
                        map.put(iter.getName(), m);
                } else {
                    if (map.get(iter.getName()) != null) {
                        Object obj = map.get(iter.getName());
                        if (!obj.getClass().getName().equals("java.util.ArrayList")) {
                            mapList = new ArrayList();
                            mapList.add(obj);
                            mapList.add(iter.getText());
                        }
                        if (obj.getClass().getName().equals("java.util.ArrayList")) {
                            mapList = (List) obj;
                            mapList.add(iter.getText());
                        }
                        map.put(iter.getName(), mapList);
                    } else
                        map.put(iter.getName(), iter.getText());
                }
            }
        } else
            map.put(e.getName(), e.getText());
        return map;
    }

    public static String wxNotify(HttpServletRequest request) {
        try {
            InputStream inputStream;
            StringBuffer sb = new StringBuffer();
            inputStream = request.getInputStream();
            String s;
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            while ((s = in.readLine()) != null) {
                sb.append(s);
            }
            in.close();
            inputStream.close();
            return sb.toString();
//            ServletInputStream inputStream = request.getInputStream();
//            StringBuffer sb = new StringBuffer();
//            byte[] b = new byte[1024];
//            int lens = -1;
//            while ((lens = inputStream.read(b))>0){
//                sb.append(new String(b,0,lens));
//            }
            //return IOUtils.toString(request.getReader());
            /*StringBuilder sb = new StringBuilder();
            try {
                BufferedReader reader = request.getReader();
                reader.mark(10000);

                String line;
                do {
                    line = reader.readLine();
                    sb.append(line).append("\n");
                } while (line != null);
                reader.reset();
                // do NOT close the reader here, or you won't be able to get the post data twice
            } catch (IOException e) {
                System.out.println("getPostData couldn't.. get the post data");  // This has happened if the
                // request's reader
                // is closed
            }

            return sb.toString();*/
        } catch (Exception e) {

        }
        return null;
    }

    public static Map<String, Object> analysisCashPayMap(Map<String, Object> payMap) {
        Map<String, Object> res = new HashMap<>();
        if (payMap != null) {
            Integer payCode = 0;
            if (payMap.containsKey("payStatus")) {
                payCode = Integer.valueOf(payMap.get("payStatus").toString());
            }
            if (payCode == ResultConstant.ERROR) {
                res.put("code", -1);
                res.put("msg", payMap.get("payMsg"));
                return res;
            }
            res.put("code", 0);
            res.put("flowId", payMap.get("flowId"));
            res.put("iosMallGoodId", payMap.get("iosMallGoodId"));
            if (payMap.containsKey("payForToken")) {
                res.putAll((Map<? extends String, ?>) payMap.get("payForToken"));
            }
            res.put("msg", "下单成功");
            return res;
        }
        res.put("code", -1);
        res.put("msg", "支付失败");
        return res;
    }

    public static List<Map<String, Object>> getPayChannelNotEnoughTags(Integer channelId) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (channelId.equals(CommonConstant.WISDOM_COIN_CHANNEL_ID)) {
            Map<String, Object> temp = new HashMap<>();
//            temp.put("img", "http://p5q287kfg.bkt.clouddn.com/pay_channel_diacount.png");
            temp.put("img", "https://cdn.caiqr.com/add30.png");
            temp.put("ratio", "70:22");
            result.add(temp);
        }
        return result;
    }

    /**
     * <p>
     * <code>parseText</code> parses the given text as an XML document and
     * returns the newly created Document.
     * override DocumentHelper's parseText to dealwith xxe error
     * </p>
     *
     * @param text the XML text to be parsed
     * @return a newly parsed Document
     * @throws DocumentException if the document could not be parsed
     */
    public static Document parseText(String text) throws DocumentException {
        Document result = null;

        SAXReader reader = new SAXReader();
        try {
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        String encoding = getEncoding(text);

        InputSource source = new InputSource(new StringReader(text));
        source.setEncoding(encoding);

        result = reader.read(source);

        // if the XML parser doesn't provide a way to retrieve the encoding,
        // specify it manually
        if (result.getXMLEncoding() == null) {
            result.setXMLEncoding(encoding);
        }

        return result;
    }

    private static String getEncoding(String text) {
        String result = null;

        String xml = text.trim();

        if (xml.startsWith("<?xml")) {
            int end = xml.indexOf("?>");
            String sub = xml.substring(0, end);
            StringTokenizer tokens = new StringTokenizer(sub, " =\"\'");

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();

                if ("encoding".equals(token)) {
                    if (tokens.hasMoreTokens()) {
                        result = tokens.nextToken();
                    }

                    break;
                }
            }
        }

        return result;
    }

    public static <T extends JdPayBaseResponse> T parseJDResp(String rsaPubKey, String strDesKey, String xmlResp,
                                                              Class<T> cls) throws IllegalAccessException,
            InstantiationException {
        JdPayResponse jdPayResponse = JdPayXmlUtil.jdPayXml2Bean(xmlResp, JdPayResponse.class);
        T t = cls.newInstance();
        if (StringUtils.isNotEmpty(jdPayResponse.getEncrypt())) {
            String reqBody = XmlEncryptUtil.decrypt(rsaPubKey, strDesKey, jdPayResponse.getEncrypt());
            System.out.println("respXml:\n" + reqBody);
            t = JdPayXmlUtil.jdPayXml2Bean(reqBody, cls);
        }

        t.setMerchant(jdPayResponse.getMerchant());
        t.setVersion(jdPayResponse.getVersion());
        t.setResult(jdPayResponse.getResult());
        return t;
    }

    public static String getJDPayUrl(Map<String, Object> param) {
        StringBuilder result = new StringBuilder(PayConstant.H5_PAY_FORM_FORM_URL);
        if (param != null) {
            result.append(CommonConstant.COMMON_QUESTION_STR);
            int i = 0;
            for (String key : param.keySet()) {
                if (param.get(key) == null) {
                    continue;
                }
                if (i > 0 && i < param.size()) {
                    result.append(CommonConstant.COMMON_AND_STR);
                }
                if (StringUtils.isNotBlank(param.get(key).toString())) {
                    result.append(key).append(CommonConstant.COMMON_EQUAL_STR).append(param.get(key));
                }
                i++;
            }
        }
        return result.toString();
    }

    public static String getUserBankCardFromRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return "";
        }
        Map<String, Object> remarkMap = JSONObject.parseObject(remark, HashMap.class);
        if (remarkMap != null && remarkMap.containsKey("bankCard")) {
            return remarkMap.get("bankCard").toString();
        }
        return "";
    }

    public static Map<String, Object> getWeChatSubscription(Integer clientId, Integer versionCode) {
        Map<String, Object> result = new HashMap<>();
        String actionText = "";
        String actionUrl = "";
        if (CommonUtil.getIosReview(versionCode, clientId).equals(CommonConstant.IOS_REVIEW_STATUS_PASSED)) {
            actionUrl = ActivityIniCache.getActivityIniValue(ActivityIniConstant.WX_JSAPI_PAY_JUMP_URL, "https://m" +
                    ".caiqr.com/daily/zhycgongzhonghao/index.htm");
            actionText = "微信客服充值多送30%→";
        }

        result.put("actionText", "");
        result.put("actionUrl", "");
        return result;
    }

    public static String getDisableChannelIcon(Integer channelId, Integer versioCode) {
        if (versioCode < CommonConstant.VERSION_CODE_4_6_4) {
            return "";
        }
        if (channelId.equals(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID)) {
            return CommonUtil.getImgUrlWithDomain("alipayicon_black.png");
        } else if (channelId.equals(CommonConstant.WX_PAY_CHANNEL_ID)) {
            return CommonUtil.getImgUrlWithDomain("paychannelwechat_black.png");
        }
        return "";
    }

    public static void sortedPaymentList(List<Map<String, Object>> paymentList) {
        Collections.sort(paymentList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Integer isDefault1 = Integer.valueOf(o1.get("isDefault").toString());
                Integer isDefault2 = Integer.valueOf(o2.get("isDefault").toString());
                Integer status1 = Integer.valueOf(o1.get("status").toString());
                Integer status2 = Integer.valueOf(o2.get("status").toString());
                Integer weight1 = Integer.valueOf(o1.get("weight").toString());
                Integer weight2 = Integer.valueOf(o2.get("weight").toString());
                if (isDefault1.equals(isDefault2)) {
                    if (status1.equals(status2)) {
                        return weight2.compareTo(weight1);
                    } else {
                        return status2.compareTo(status1);
                    }
                } else {
                    return isDefault2.compareTo(isDefault1);
                }

            }
        });
    }

    public static String packageHaoDianFlowId(String flowId) {
        if (flowId.contains("zh")) {
            return flowId;
        }
        return "zh" + flowId;
    }

    public static String removeHaoDianFlowId(String flowId) {
        return flowId.replace("zh", "");
    }

    public static Long randomDiscountPrice(long price, Integer channelId) {
        if (!channelId.equals(CommonConstant.HAO_DIAN_PAY_CHANNEL_ID)) {
            return price;
        }
        Integer min = Integer.valueOf(DateUtil.getCurrentDay("mm"));
        Integer random = new Random(min).nextInt(3) + 1;
        return CommonUtil.subtract(price + "", random + "").longValue();
    }
}
