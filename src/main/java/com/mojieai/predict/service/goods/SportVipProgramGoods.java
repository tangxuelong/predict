package com.mojieai.predict.service.goods;

import com.mojieai.predict.dao.VipProgramDao;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.VipProgram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SportVipProgramGoods extends AbstractGoods {

    @Autowired
    private VipProgramDao vipProgramDao;

    @Override
    public PrePayInfo getPrePayInfo(String goodsId) {
        VipProgram vipProgram = vipProgramDao.getVipProgramByProgramId(goodsId, false);
        if (vipProgram == null) {
            return null;
        }

        String goodsName = "会员专区";
        return new PrePayInfo(goodsId, goodsName, vipProgram.getPrice(), 100);
    }

}
