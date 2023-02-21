package com.mojieai.predict.util.JDBill;

import com.alibaba.fastjson.JSONObject;
import com.jd.jr.pay.gate.signature.util.BASE64;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.ThirdPartyBillInfoDao;
import com.mojieai.predict.entity.po.ThirdPartyBillInfo;
import com.mojieai.predict.util.CSVFileUtil;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBillUtil {

    protected Logger log = LogConstant.commonLog;
    private String url = "https://bapi.jdpay.com/api/download.do";//对账下载请求地址
    private String merchantNo;
    private String owner;
    private String md5Key;//秘钥，由商户在京东平台申请配置

    public JDBillUtil(String merchantNo, String owner, String md5Key) {
        this.merchantNo = merchantNo;
        this.owner = owner;
        this.md5Key = md5Key;
    }

    public String downloadJDBillFile(String billDate) {
        String downloadFilepath = "";

        String path = "0001/0001";//具体格式请参考对账接口说明文档
        String filename = billDate + "ordercreate_" + merchantNo + ".zip";//对账下载文件名，格式：日期 + 对账类型_ + 二级商户号.zip，对账类型参考对账接口说明文档
        String data = "{'name':'" + filename + "','path':'" + path + "'}";
        try {
            data = BASE64.encode(data.getBytes());//data进行BASE64
            String md5 = MD5.md5(data + md5Key, "");
            Map<String, String> params = new HashMap<String, String>();
            params.put("md5", md5);
            params.put("data", data);
            params.put("owner", owner);//owner为 商户号（8位或9位）

            //获得项目绝对路径
            String realPath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
            //对账文件的存储路径
            downloadFilepath = realPath + "downloadFile";
            WebUtils.download(url, params, 5000, 5000, downloadFilepath + "/" + filename);
        } catch (Exception e) {
            log.error("京东对账文件下载异常", e);
            return "";
        }
        return downloadFilepath + "/" + filename;
    }

    public Boolean saveJDBillFile2DB(String fileName, String mchId, ThirdPartyBillInfoDao thirdPartyBillInfoDao) {
        String realPath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
        //对账文件的存储路径
        String downloadFilepath = realPath + "downloadFile";
        String parentPath = fileName.substring(0, fileName.lastIndexOf("."));
        String filePath = downloadFilepath + "/" + parentPath + "/" + fileName;
        List<String> fileContents = CSVFileUtil.readFile(filePath, "GBK");

        return saveJDBillFile2DB(mchId, thirdPartyBillInfoDao, fileContents);
    }

    public Boolean saveJDBillFile2DB(String mchId, ThirdPartyBillInfoDao thirdPartyBillInfoDao, List<String>
            fileContents) {
        if (fileContents == null || fileContents.size() < 2) {
            return Boolean.FALSE;
        }
        for (int i = 1; i < fileContents.size(); i++) {
            String content = fileContents.get(i);
            String[] contentArr = content.split(CommonConstant.COMMA_SPLIT_STR);
            ThirdPartyBillInfo thirdPartyBillInfo = null;
            if (contentArr.length > 17) {
                thirdPartyBillInfo = get18ThirdPartyBillInfo(contentArr);
            } else {
                thirdPartyBillInfo = get17ThirdPartyBillInfo(contentArr);
            }

            thirdPartyBillInfo.setMchId(mchId);
            thirdPartyBillInfo.setPoundageRate(0.06);
            try {
                thirdPartyBillInfoDao.insert(thirdPartyBillInfo);
            } catch (Exception e) {
                log.error(e);
            }
        }

        return Boolean.FALSE;
    }

    private ThirdPartyBillInfo get17ThirdPartyBillInfo(String[] contentArr) {
        ThirdPartyBillInfo thirdPartyBillInfo = new ThirdPartyBillInfo();
        String dealTimeStr = contentArr[8];
        Timestamp dealTime = DateUtil.formatString(dealTimeStr, "yyyy-MM-dd HH:mm:ss");
        thirdPartyBillInfo.setDealTime(dealTime);
        thirdPartyBillInfo.setRptDate(DateUtil.formatDate(dealTime, "yyyyMMdd"));
        thirdPartyBillInfo.setOrderId(CommonUtil.removeJDBillQuotationMark(contentArr[0]));
        thirdPartyBillInfo.setBusinessType(CommonUtil.removeJDBillQuotationMark(contentArr[4]));
        thirdPartyBillInfo.setAmount(Double.valueOf(contentArr[2].replaceAll("-", "")));
        thirdPartyBillInfo.setPoundage(Double.valueOf(CommonUtil.removeJDBillQuotationMark(contentArr[11])));
        Map<String, Object> remarkMap = new HashMap<>();
        remarkMap.put("accountName", CommonUtil.removeQuotationMark(contentArr[12]));
        remarkMap.put("payType", CommonUtil.removeJDBillQuotationMark(contentArr[3]));
        remarkMap.put("thirdMemo", CommonUtil.removeQuotationMark(contentArr[14]));
        remarkMap.put("thirdMemo2", CommonUtil.removeQuotationMark(contentArr[15]));
        remarkMap.put("thirdFinishTime", CommonUtil.removeJDBillQuotationMark(contentArr[9]));
        remarkMap.put("bankName", contentArr[15]);
        thirdPartyBillInfo.setRemark(JSONObject.toJSONString(remarkMap));
        String thirdPartyId = CommonUtil.removeJDBillQuotationMark(contentArr[16]).trim();
        if (thirdPartyId.equals("-1")) {
            thirdPartyId = CommonUtil.removeJDBillQuotationMark(contentArr[1]);
        }
        thirdPartyBillInfo.setThirdPartyId(thirdPartyId);
        thirdPartyBillInfo.setStatus(contentArr[5]);
        thirdPartyBillInfo.setRefundAmount(Double.valueOf(contentArr[6]));
        thirdPartyBillInfo.setRefundId(CommonUtil.removeJDBillQuotationMark(contentArr[10]));
        return thirdPartyBillInfo;
    }

    private ThirdPartyBillInfo get18ThirdPartyBillInfo(String[] contentArr) {
        ThirdPartyBillInfo thirdPartyBillInfo = new ThirdPartyBillInfo();
        String dealTimeStr = contentArr[8];
        Timestamp dealTime = DateUtil.formatString(dealTimeStr, "yyyy-MM-dd HH:mm:ss");
        thirdPartyBillInfo.setDealTime(dealTime);
        thirdPartyBillInfo.setRptDate(DateUtil.formatDate(dealTime, "yyyyMMdd"));
        thirdPartyBillInfo.setOrderId(CommonUtil.removeJDBillQuotationMark(contentArr[0]));
        thirdPartyBillInfo.setBusinessType(CommonUtil.removeJDBillQuotationMark(contentArr[4]));
        thirdPartyBillInfo.setAmount(Double.valueOf(contentArr[2].replaceAll("-", "")));
        thirdPartyBillInfo.setPoundage(Double.valueOf(CommonUtil.removeJDBillQuotationMark(contentArr[11])));
        Map<String, Object> remarkMap = new HashMap<>();
        remarkMap.put("accountName", CommonUtil.removeQuotationMark(contentArr[12]));
        remarkMap.put("payType", CommonUtil.removeJDBillQuotationMark(contentArr[3]));
        remarkMap.put("thirdMemo", CommonUtil.removeQuotationMark(contentArr[14]));
        remarkMap.put("thirdMemo2", CommonUtil.removeQuotationMark(contentArr[15]));
        remarkMap.put("thirdFinishTime", CommonUtil.removeJDBillQuotationMark(contentArr[9]));
        remarkMap.put("bankName", contentArr[16]);
        remarkMap.put("jdExchangeNo", CommonUtil.removeJDBillQuotationMark(contentArr[18]));
        thirdPartyBillInfo.setRemark(JSONObject.toJSONString(remarkMap));
        String thirdPartyId = CommonUtil.removeJDBillQuotationMark(contentArr[17]);
        if (StringUtils.isBlank(thirdPartyId) || thirdPartyId.equals("-1")) {
            thirdPartyId = CommonUtil.removeJDBillQuotationMark(contentArr[18]);
        }
        thirdPartyBillInfo.setThirdPartyId(thirdPartyId);
        thirdPartyBillInfo.setStatus(contentArr[5]);
        thirdPartyBillInfo.setRefundAmount(Double.valueOf(contentArr[6]));
        thirdPartyBillInfo.setRefundId(CommonUtil.removeJDBillQuotationMark(contentArr[10]));
        return thirdPartyBillInfo;
    }

}
