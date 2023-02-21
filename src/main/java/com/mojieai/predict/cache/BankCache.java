package com.mojieai.predict.cache;

import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.entity.bo.BankInfo;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankCache {

    private static final Logger log = LogConstant.commonLog;

    private static List<BankInfo> bankList = new ArrayList<>();
    private static Map<String, BankInfo> bankMap = new HashMap<>();

    public void init() {
        log.info("init BankCache");
        refresh();
    }

    public void refresh() {
        List<Map<String, Object>> banks = ActivityIniCache.getActivityIniListValueByWeight
                (ActivityIniConstant.JING_DONG_BANK_LIST);
        if (banks != null && banks.size() > 0) {
            for (Map<String, Object> bank : banks) {
                BankInfo bankInfo = new BankInfo(bank.get("bankEn").toString(), bank.get("bank_short_name").toString
                        (), bank.get("bank_img_url").toString());
                bankList.add(bankInfo);
                bankMap.put(bankInfo.getBankEn(), bankInfo);
            }
        }
    }

    public static List<BankInfo> getJDSupportBank() {
        return bankList;
    }

    public static BankInfo getBankInfoByEn(String bankEn) {
        if (!bankMap.containsKey(bankEn)) {
            return null;
        }
        return bankMap.get(bankEn);
    }
}
