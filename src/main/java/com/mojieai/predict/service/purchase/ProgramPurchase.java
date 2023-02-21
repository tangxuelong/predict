package com.mojieai.predict.service.purchase;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.PayConstant;
import com.mojieai.predict.constant.ResultConstant;
import com.mojieai.predict.dao.ProgramDao;
import com.mojieai.predict.entity.bo.PrePayCheck;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.bo.TradePayResult;
import com.mojieai.predict.entity.po.Program;
import com.mojieai.predict.entity.po.UserProgram;
import com.mojieai.predict.service.ProgramService;
import com.mojieai.predict.service.UserProgramService;
import com.mojieai.predict.util.ProgramUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProgramPurchase extends AbstractPurchase {
    @Autowired
    private ProgramService programService;
    @Autowired
    private UserProgramService userProgramService;
    @Autowired
    private ProgramDao programDao;

    @Override
    public PrePayCheck checkBeforePurchase(Long userId, String goodsId) {
        Map<String, Object> checkRes = programService.checkProgram(userId, goodsId);

        String msg = "";
        if (checkRes.containsKey("msg")) {
            msg = checkRes.get("msg").toString();
        }
        return new PrePayCheck(Integer.valueOf(checkRes.get("flag").toString()), msg);
    }

    @Override
    public Map<String, Object> cashPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer channelId, Integer bankId,
                                                 Integer clientId, String clientIp, Integer versionCode) {

        Map<String, Object> res = new HashMap<>();
        //1.取得商品信息
        Program program = programDao.getProgramById(prePayInfo.getGoodsId(), false);
        if (program == null) {
            res.put("code", -1);
            res.put("msg", "商品不存在");
            return res;
        }
        long price = getRealPayAmount(userId, prePayInfo, channelId);

        //2.购买方案流水
        UserProgram userProgram = userProgramService.produceUserProgram(userId, program, price);

        //3.获取三方结果来调起支付
        String payDesc = ProgramUtil.getProgramTypeCn(program.getProgramType()) + "，智慧指数 " + program.getWisdomScore();
        return getOutTradeInfo(userId, userProgram.getUserProgramId(), payDesc, price, CommonConstant.PAY_TYPE_CASH,
                channelId, price, clientIp, clientId, CommonConstant.PROGRAM_PURCHASE_CALL_BACK_METHOD,
                CommonConstant.PAY_OPERATE_TYPE_DEC, prePayInfo, bankId);
    }

    @Override
    public Map<String, Object> wisdomCoinPurchaseGoods(Long userId, PrePayInfo prePayInfo, Integer payChannelId) {
        Map<String, Object> res = new HashMap<>();
        res.put("code", -1);
        Program program = programDao.getProgramById(prePayInfo.getGoodsId(), false);
        long price = getRealPayAmount(userId, prePayInfo, CommonConstant.WISDOM_COIN_CHANNEL_ID);

        //1.初始化用户方案
        UserProgram userProgram = userProgramService.produceUserProgram(userId, program, price);
        if (userProgram.getIsPay().equals(CommonConstant.PROGRAM_IS_PAY_YES)) {
            res.put("code", 0);
            res.put("msg", "方案已购买");
            return res;
        }

        // 2.支付
        String payDesc = prePayInfo.getGoodsName();

        Map<String, Object> payInfo = getOutTradeInfo(userId, userProgram.getUserProgramId(), payDesc, price,
                CommonConstant.ACCOUNT_TYPE_WISDOM_COIN, payChannelId, price, null, null, null, CommonConstant
                        .PAY_OPERATE_TYPE_DEC, prePayInfo, null);

        if (ResultConstant.ERROR == Integer.valueOf(payInfo.get("code").toString())) {
            return payInfo;
        }

        // 3.支付成功 更新用户
        Boolean updateRes = userProgramService.updateUserSubscribeInfoAfterPayed(userId, program, userProgram
                .getUserProgramId());
        return wisdomCoinPayedHandleFlow(updateRes, payInfo);
    }
}
