package com.mojieai.predict.service.purchase;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.UserVipProgramDao;
import com.mojieai.predict.dao.VipProgramDao;
import com.mojieai.predict.entity.bo.PrePayCheck;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.UserVipProgram;
import com.mojieai.predict.entity.po.VipProgram;
import com.mojieai.predict.service.UserVipProgramService;
import com.mojieai.predict.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SportVipProgramPurchase extends AbstractPurchase {

    @Autowired
    private VipProgramDao vipProgramDao;
    @Autowired
    private UserVipProgramDao userVipProgramDao;
    @Autowired
    private UserVipProgramService userVipProgramService;

    @Override
    public PrePayCheck checkBeforePurchase(Long userId, String goodsId) {
        PrePayCheck prePayCheck = new PrePayCheck(ResultConstant.PAY_SUCCESS_CODE, "");
        VipProgram vipProgram = vipProgramDao.getVipProgramByProgramId(goodsId, false);
        if (vipProgram == null) {
            prePayCheck.setCode(ResultConstant.ERROR);
            prePayCheck.setMsg("方案不存在");
            return prePayCheck;
        }

        if (DateUtil.compareDate(vipProgram.getEndTime(), DateUtil.getCurrentTimestamp())) {
            prePayCheck.setCode(ResultConstant.ERROR);
            prePayCheck.setMsg("方案已过期");
            return prePayCheck;
        }

        UserVipProgram userVipProgram = userVipProgramDao.getUserVipProgramByUnkey(userId, goodsId);
        if (userVipProgram != null && userVipProgram.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            prePayCheck.setCode(ResultConstant.ERROR);
            prePayCheck.setMsg("方案已购买");
            return prePayCheck;
        }

        return prePayCheck;
    }

    @Override
    public Map<String, Object> cashPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer channelId, Integer
            bankId, Integer clientId, String clientIp, Integer versionCode) {
        //1.取得商品信息
        VipProgram program = vipProgramDao.getVipProgramByProgramId(prePayInfo.getGoodsId(), false);

        //2.购买方案流水
        UserVipProgram userVipProgram = userVipProgramService.produceUserVipProgramLog(userId, prePayInfo.getGoodsId
                (), CommonConstant.USER_VIP_PROGRAM_PAY_TYPE_PAY);

        //3.获取三方结果来调起支付
        String payDesc = prePayInfo.getGoodsName();
        return getOutTradeInfo(userId, userVipProgram.getPrePayId(), payDesc, program.getPrice(), CommonConstant
                .PAY_TYPE_CASH, channelId, program.getPrice(), clientIp, clientId, CommonConstant
                .SPORT_VIP_PROGRAM_CALL_BACK_METHOD, CommonConstant.PAY_OPERATE_TYPE_DEC, prePayInfo, bankId);
    }

    @Override
    public Map<String, Object> wisdomCoinPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer payChannelId) {
        Map<String, Object> res = new HashMap<>();
        //1.取得商品信息
        VipProgram program = vipProgramDao.getVipProgramByProgramId(prePayInfo.getGoodsId(), false);
        if (program == null) {
            res.put("code", -1);
            res.put("msg", "方案不存在");
            return res;
        }

        //1.初始化用户方案
        UserVipProgram userVipProgram = userVipProgramService.produceUserVipProgramLog(userId, prePayInfo.getGoodsId
                (), CommonConstant.USER_VIP_PROGRAM_PAY_TYPE_PAY);
        if (userVipProgram.getIsPay() != null && userVipProgram.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            res.put("code", -1);
            res.put("msg", "方案已购买");
            return res;
        }

        // 2.支付
        String payDesc = prePayInfo.getGoodsName();
        Map<String, Object> payInfo = getOutTradeInfo(userId, userVipProgram.getPrePayId(), payDesc, program.getPrice
                        (), CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, program.getPrice(), null, null, null,
                CommonConstant.PAY_OPERATE_TYPE_DEC, prePayInfo, null);

        if (ResultConstant.ERROR == Integer.valueOf(payInfo.get("code").toString())) {
            return payInfo;
        }

        // 3.支付成功 更新用户订阅
        Boolean updateRes = userVipProgramService.updateUserProgramLogAfterPayed(userId, userVipProgram.getPrePayId());
        return wisdomCoinPayedHandleFlow(updateRes, payInfo);
    }
}
