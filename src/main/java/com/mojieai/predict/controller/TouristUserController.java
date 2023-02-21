package com.mojieai.predict.controller;

import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.service.TouristUserService;
import com.mojieai.predict.util.qiniu.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/touristUser")
public class TouristUserController extends BaseController {

    @Autowired
    private TouristUserService touristUserService;

    @RequestMapping("/checkDeviceId")
    @ResponseBody
    public Object checkDeviceId(@RequestParam String deviceId, @RequestAttribute String clientType) {
        Map result = new HashMap();
        //1.check 游客模式是否开启
        String touristModle = ActivityIniCache.getActivityIniValue(ActivityIniConstant.TOURIST_MODLE_SWITCH, "off");
        if (StringUtils.isBlank(touristModle) || !touristModle.equals(ActivityIniConstant.TOURIST_MODLE_SWITCH_ON)) {
//            result.put("ifNeedLogin", true);
            return buildErrJson("请登录");
        }
        result = touristUserService.checkDeviceId(deviceId, clientType);
        return buildSuccJson(result);
    }
}
