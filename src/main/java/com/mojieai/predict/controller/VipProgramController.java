package com.mojieai.predict.controller;

import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.service.ActivityBetService;
import com.mojieai.predict.service.LoginService;
import com.mojieai.predict.service.UserVipProgramService;
import com.mojieai.predict.service.VipProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by tangxuelong on 2017/11/22.
 */

@RequestMapping("/vip_program")
@Controller
public class VipProgramController extends BaseController {

    @Autowired
    private VipProgramService vipProgramService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserVipProgramService userVipProgramService;

    /**
     * 插入数据 管理后台
     *
     * @param price 单位元
     */
    @RequestMapping("/product_vip_program_no_sign")
    @ResponseBody
    public Object productVipProgram(@RequestParam Integer awardNum, @RequestParam Integer recommendNum, @RequestParam
            String price, @RequestParam String programInfo) {
        return buildSuccJson(vipProgramService.productVipProgram(awardNum, recommendNum, price, programInfo));
    }

    // 获取VIP列表数据
    @RequestMapping("/get_vip_program_list")
    @ResponseBody
    public Object getVipProgramList(@RequestParam String token) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("用户校验失败！");
        }

        return buildSuccJson(vipProgramService.getVipProgramList(userToken.getUserId()));
    }

    @RequestMapping("/exchange_sports_vip_program")
    @ResponseBody
    public Object exchangeSportsVipProgram(@RequestParam String token, @RequestParam String programId) {
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("用户校验失败！");
        }
        return buildSuccJson(userVipProgramService.vipExchangeVipProgram(userToken.getUserId(), programId));
    }

    @RequestMapping("/get_red_vip_program")
    @ResponseBody
    public Object getRedVipProgram(Long lastIndex) {
        return buildSuccJson(vipProgramService.getRedVipProgram(lastIndex));
    }
}
