package com.mojieai.predict.service.goods;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.dao.ProgramDao;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.Program;
import com.mojieai.predict.util.ProgramUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProgramGoods extends AbstractGoods {

    @Autowired
    private ProgramDao programDao;

    @Override
    public PrePayInfo getPrePayInfo(String goodsId) {
        Program program = programDao.getProgramById(goodsId, false);
        if (program == null) {
            return null;
        }
        String goodsName = ProgramUtil.getProgramTypeCn(program.getProgramType()) + "，智慧指数" + program.getWisdomScore();
        PrePayInfo prePayInfo = new PrePayInfo(goodsId, goodsName, program.getPrice(), program.getVipDiscount());
        prePayInfo.setVipPrice(program.getVipPrice());
        prePayInfo.setVipDiscountWay(CommonConstant.VIP_DISCOUNT_WAY_LOW_PAY);
        prePayInfo.setIosMallId(program.getIosMallId());
        prePayInfo.setVipIosMallId(program.getVipIosMallId());
        return prePayInfo;
    }
}
