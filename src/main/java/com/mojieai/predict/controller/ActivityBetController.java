package com.mojieai.predict.controller;

import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.entity.po.UserToken;
import com.mojieai.predict.entity.vo.UserLoginVo;
import com.mojieai.predict.service.ActivityBetService;
import com.mojieai.predict.service.ActivityService;
import com.mojieai.predict.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/22.
 */

@RequestMapping("/activityBet")
@Controller
public class ActivityBetController extends BaseController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private ActivityBetService activityBetService;
    @Autowired
    private UserAuthController userAuthController;

    // 投注支付接口
    @RequestMapping("/world_cup_activity_bonus")
    @ResponseBody
    public Object world_cup_activity_bonus(@RequestParam String token, @RequestParam String match_id, @RequestParam
            String bet_info, @RequestParam Integer bet_type, @RequestParam Integer bet_mult, @RequestParam(required =
            false) Integer isAward) {
        // 用户校验
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("user check error");
        }
        Integer code = activityBetService.betMatchItem(userToken.getUserId(), match_id, bet_info, bet_mult, bet_type,
                isAward);
        return buildJson(code, "");
    }

    // 预约活动
    @RequestMapping("/world_cup_activity_appointment")
    @ResponseBody
    public Object world_cup_activity_appointment(@RequestParam String token, @RequestParam Integer activityId) {
        // 用户校验
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("user check error");
        }
        activityBetService.userAppointment(userToken.getUserId(), activityId);
        return buildSuccJson();
    }

    // 公用抽奖活动业务方逻辑
    @RequestMapping("/world_cup_activity_transmit_user_prize_info")
    @ResponseBody
    public Object world_cup_activity_transmit_user_prize_info(@RequestParam String token, @RequestParam Integer
            activityId, @RequestParam Integer prize_id, @RequestParam String unique_id) {
        // 用户校验
        UserToken userToken = loginService.checkToken(token);
        if (null == userToken) {
            return buildErrJson("user check error");
        }
        Integer code = activityBetService.confirmAwardGoods(userToken.getUserId(), activityId, prize_id, unique_id);
        return buildJson(code, "");
    }

}
