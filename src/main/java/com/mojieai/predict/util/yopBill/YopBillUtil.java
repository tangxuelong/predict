package com.mojieai.predict.util.yopBill;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.ThirdPartyBillInfoDao;
import com.mojieai.predict.entity.po.ThirdPartyBillInfo;
import com.mojieai.predict.util.CSVFileUtil;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class YopBillUtil {
    protected Logger log = LogConstant.commonLog;

    private String merchantNo;
    private String partPath;

    public YopBillUtil(String merchantNo, String partPath) {
        this.merchantNo = merchantNo;
        this.partPath = partPath;
    }

    public String getDateYopBillFileFromHttp(Map<String, String> yopParam) {
        //获得项目绝对路径
        String realPath = Thread.currentThread().getContextClassLoader().getResource(".").getPath();
        //对账文件的存储路径
        String downloadFilepath = realPath + "downloadFile";

        return yosFile(yopParam, downloadFilepath, this.merchantNo, this.partPath);
    }

    private String yosFile(Map<String, String> params, String path, String merchantNo, String partPath) {
        CustomStdApi apidApi = new CustomStdApi(partPath);
        InputStream inputStream = null;
        OutputStream outputStream = null;
        String method = params.get("method");
        String dateday = params.get("dateday");
        String datemonth = params.get("datemonth");
        String dataType = params.get("dataType");

        String fileName = "";
        String filePath = "";
        try {
            if (method.equals(CommonConstant.TRADEDAYDOWNLOAD)) {
                System.out.println("1");
                inputStream = apidApi.tradeDayBillDownload(merchantNo, dateday);
                fileName = merchantNo + "tradeday-" + dateday + ".csv";

            } else if (method.equals(CommonConstant.TRADEMONTHDOWNLOAD)) {
                System.out.println("2");
                inputStream = apidApi.tradeMonthBillDownload(merchantNo, datemonth);
                fileName = merchantNo + "trademonth-" + datemonth + ".csv";

            } else if (method.equals(CommonConstant.REMITDAYDOWNLOAD)) {
                System.out.println("2");
                inputStream = apidApi.remitDayBillDownload(merchantNo, dateday, dataType);
                fileName = merchantNo + "remitday-" + dataType + "-" + dateday + ".csv";
            }
            filePath = path + File.separator + fileName;
            System.out.println("filePath=====" + filePath);
            outputStream = new FileOutputStream(new File(filePath));
            byte[] bs = new byte[1024];
            int readNum;
            while ((readNum = inputStream.read(bs)) != -1) {
                outputStream.write(bs, 0, readNum);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    public Boolean saveDownloadFile2DB(String filePath, ThirdPartyBillInfoDao thirdPartyBillInfoDao, int i) {
        List<String> allString = CSVFileUtil.readFile(filePath, "UTF-8");
        return analysisYopCsvFile(allString, filePath, merchantNo, thirdPartyBillInfoDao, i);
    }

    private Boolean analysisYopCsvFile(List<String> allString, String path, String mchId, ThirdPartyBillInfoDao
            thirdPartyBillInfoDao, int position) {
        Boolean res = Boolean.FALSE;
        if (allString.size() < 5) {
            log.info(path + " is empty");
            if (allString.get(1).contains("SUCCESS")) {
                return Boolean.TRUE;
            }
            return res;
        }
        int saveCount = 0;
        for (int i = 5; i < allString.size(); i++) {
            String tempLine = allString.get(i);
            String[] billLineArr = tempLine.split(CommonConstant.COMMA_SPLIT_STR);
            ThirdPartyBillInfo thirdPartyBillInfo = new ThirdPartyBillInfo();
            String dealTimeStr = CommonUtil.removeQuotationMark(billLineArr[0]);
            Timestamp dealTime = DateUtil.formatString(dealTimeStr, "yyyy-MM-dd HH:mm:ss");
            thirdPartyBillInfo.setDealTime(dealTime);
            thirdPartyBillInfo.setRptDate(DateUtil.formatDate(dealTime, "yyyyMMdd"));
            thirdPartyBillInfo.setOrderId(CommonUtil.removeQuotationMark(billLineArr[2]));
            thirdPartyBillInfo.setBusinessType(CommonUtil.removeQuotationMark(billLineArr[3]));
            thirdPartyBillInfo.setAmount(Double.valueOf(CommonUtil.removeQuotationMark(billLineArr[4])));
            thirdPartyBillInfo.setPoundage(Double.valueOf(CommonUtil.removeQuotationMark(billLineArr[5]).replaceAll
                    ("-", "")));
            Map<String, Object> remarkMap = new HashMap<>();
            remarkMap.put("productType", CommonUtil.removeQuotationMark(billLineArr[6]));
            remarkMap.put("payType", CommonUtil.removeQuotationMark(billLineArr[7]));
            remarkMap.put("thirdMemo", CommonUtil.removeQuotationMark(billLineArr[8]));
            thirdPartyBillInfo.setRemark(JSONObject.toJSONString(remarkMap));
            thirdPartyBillInfo.setThirdPartyId(CommonUtil.removeQuotationMark(billLineArr[9]));
            thirdPartyBillInfo.setMchId(mchId);
            thirdPartyBillInfo.setStatus("success");
            thirdPartyBillInfo.setPoundageRate(0.005);

            try {
                if (thirdPartyBillInfoDao.insert(thirdPartyBillInfo) > 0) {
                    saveCount++;
                }
            } catch (DuplicateKeyException e) {
                saveCount++;
            }
        }
        if (saveCount == allString.size() - 5) {
            res = Boolean.TRUE;
        }
        return res;
    }

}
