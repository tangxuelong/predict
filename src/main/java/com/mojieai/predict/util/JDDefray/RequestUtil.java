package com.mojieai.predict.util.JDDefray;

import com.alibaba.fastjson.JSON;
import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.entity.bo.JDWithdrawCallBackParam;
import com.mojieai.predict.util.BeanMapUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class RequestUtil {
    /*******************测试使用，生产需要替换相应的值和文件*******************/
    //RSA加密使用
    private static final String pri = "server_online.pfx";//秘钥文件名（该文件包含公钥和私钥）
    private static final String pub = "npp_11_API2_pro.cer";//代付证书文件名

    //签名使用
    private static final String singKey = "0f13bb3b997cea68eac8f0313084166603c1afa082312a1f5132421909b7863e";//"1qa2ws3ed~!@360080000230629280";//签名key，测试环境测试的都是test，生产上一个会员对应一个key

    //des加密使用的秘钥，此秘钥经过base64,使用前请用BASE64反解密后再对数据进行加密，实际中一个会员ID对应一个秘钥 该秘钥对应会员360080002213400010
//	private static final String DES_encryptionDataKey="7F1F0DCE495ECD679BE3F7C229319EE92643687C0B679E9E";

//	//https请求使用的密钥库文件和密码(暂时不用)
    /*******************测试使用，生产需要替换相应的值和文件*******************/


    /**
     * 请求demo(带证书加密) 外部商户https参考使用
     *
     * @throws Exception
     */
    public String tradeRequestSSL(Map<String, String> paramMap, String url, String encryptType, String filePwd) throws
            Exception {
        //测试路径，请更改实际文件路径
        String rootPath = Thread.currentThread().getContextClassLoader().getResource(".").getPath() + "defrayRsa/";
        //获取路径
        System.out.println(rootPath);
        //请求数据，http请求时将此map中的数据放入Form表单中
        Map<String, String> requestMap = enctyptData(paramMap, encryptType, filePwd);
        System.out.println("------>encrypt_data=" + requestMap.get("encrypt_data"));
        //https请求
        WyHttpClientUtil util = new WyHttpClientUtil();
        String content = util.postSSLUrlWithParams(url, requestMap);
        System.out.println("------>response_data=" + content);
        return content;

    }

    /**
     * 请求demo 内部http请求参考使用
     *
     * @param paramMap
     * @param url
     * @return
     * @throws Exception
     */
    public String tradeRequest(Map<String, String> paramMap, String url, String filePwd) throws Exception {
        Map<String, String> requestMap = null;
        String payTool = paramMap.get("pay_tool");
        if ("TRAN".equals(payTool)) {//支付工具是代付（银行卡）的需做数据加密
            requestMap = enctyptData(paramMap, Contants.encryptType_DES, filePwd);
            System.out.println("------>encrypt_data=" + requestMap.get("encrypt_data"));
        } else if ("XJK".equals(payTool) || "ACCT".equals(payTool)) {//余额和小金库的支付工具不用加密，只做签名
            requestMap = enctyptData(paramMap, null, filePwd);
        } else {
            requestMap = enctyptData(paramMap, null, filePwd);//其他不用加密，只做签名
        }
        //http请求
        WyHttpClientUtil util = new WyHttpClientUtil();
        String content = util.postUrlWithParams(url, requestMap);
        System.out.println("------>response_data=" + content);
        return content;
    }

    /**
     * 加密数据  RSA或3DES
     *
     * @param paramMap
     * @param encrtyptType
     * @return
     * @throws Exception
     */
    private Map<String, String> enctyptData(Map<String, String> paramMap, String encrtyptType, String filePwd) throws
            Exception {
        Map<String, String> requestMap = new HashMap<String, String>();
        //签名
        String sign = EnctyptUtil.sign(paramMap, singKey);
        if (StringUtils.isEmpty(encrtyptType)) {
            paramMap.put("sign_type", Contants.singType);
            paramMap.put("sign_data", sign);//设置签名数据
            requestMap = paramMap;
        } else {
            requestMap.put("sign_type", Contants.singType);//签名类型
            requestMap.put("encrypt_type", encrtyptType);//加密类型
            requestMap.put("customer_no", paramMap.get("customer_no"));//提交者会员号
            requestMap.put("sign_data", sign);//设置签名数据
            if ("RSA".equals(encrtyptType)) {
                //测试路径，请更改实际文件路径
                String rootPath = Thread.currentThread().getContextClassLoader().getResource(".").getPath() + "defrayRsa/";//获取路径
                System.out.println(rootPath);
                //数据加密
                String res = EnctyptUtil.signEnvelop(paramMap, filePwd, rootPath + pri, rootPath + pub);
                requestMap.put("encrypt_data", res);//设置加密数据
            }
        }
        return requestMap;
    }


    /**
     * 请求返回数据后处理
     *
     * @param data
     * @return 返回验证签名通过后的数据
     * @throws UnsupportedEncodingException
     */
    public Map<String, String> verifySingReturnData(String data) throws UnsupportedEncodingException {
        //测试数据样例
//		data="{\"response_code\":\"ILLEGAL_ARGUMENT\",\"response_message\":\"提交的参数有误 (pay_tool is isRequired not null)\",\"sign_data\":\"C35A463E712232ED9A8ACB655AC1CB70653672489CC89B1007F9BE90BB8D79DC\",\"sign_type\":\"SHA-256\"}";
//		data = "{\"customer_no\":\"360080002191800017\",\"out_trade_no\":\"123456d733890\",\"response_code\":\"OUT_TRADE_NO_EXIST\",\"response_datetime\":\"20150629T172719\",\"response_message\":\"外部交易号已经存在\",\"sign_data\":\"FA63D5CE42730B7AB42C35072CDD1605C204D09C6E32D7A4BEDE4575DE2C9D3F\",\"sign_type\":\"SHA-256\",\"trade_amount\":\"2\",\"trade_currency\":\"CNY\",\"trade_no\":\"20150629100041000000236659\",\"trade_status\":\"FINI\"}";
        Map<String, String> map = new HashMap<String, String>();
        if (null != data && !"".equals(data)) {
            try {
                map = JSON.parseObject(data, Map.class);
            } catch (Exception e) {
                System.out.println("--->非json格式 data=" + data);
                map.put("response_code", data);
                return map;
            }
            //必须签名验证，确保数据一致性
            map = EnctyptUtil.verifySign(map, singKey);
        } else {
            map.put("response_code", JDDefrayCodeConst.RETURN_PARAM_NULL);
            map.put("response_message", "返回数据为空");
        }
        return map;
    }

    /**
     * 签名验证demo（通知接口接收到数据后需要验签）
     *
     * @param data
     * @return 返回验证签名通过后的数据
     * @throws UnsupportedEncodingException
     */
    public Map<String, String> verifySingNotify(JDWithdrawCallBackParam data) throws UnsupportedEncodingException {
        Map<String, String> mapStr = BeanMapUtil.beanToMapStr(data);

        //必须签名验证，确保数据一致性
        Map<String, String> map = EnctyptUtil.verifySign(mapStr, singKey);
        return map;

    }
}
