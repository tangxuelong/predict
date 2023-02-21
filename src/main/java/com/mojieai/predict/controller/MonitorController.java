package com.mojieai.predict.controller;

import com.mojieai.predict.cache.IniCache;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.IniDao;
import com.mojieai.predict.util.HttpServiceUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequestMapping("/monitor")
@Controller
public class MonitorController extends BaseController {
    private final Logger log = LogConstant.commonLog;

    @Autowired
    private IniDao iniDao;

    @RequestMapping("/dbMonitor")
    @ResponseBody
    public Object monitorDB() {
        long start = System.currentTimeMillis();
        List<Integer> result = iniDao.monitorDB();
        long end = System.currentTimeMillis();
        if (end - start >= 5000l) {
            monitorCall("dbBlock", "10119");
            log.error("mysql is block.please check it.");
        }
        return buildSuccJson(result);
    }

    private void monitorCall(String monitorType, String defaultNumber) {
        String callKey = IniCache.getIniValue("callKey", "96ca7692285b4a2aeba7b73cc25b397a");
        String callPhone = IniCache.getIniValue("callPhone", "13811590825");
        String callNumber = IniCache.getIniValue(monitorType, defaultNumber);
        HttpServiceUtils.sendRequest("http://op.juhe.cn/yuntongxun/voice?valicode=" +
                callNumber + "&to=" + callPhone + "&playtimes=1&dtype=json&key=" + callKey);
    }
}
