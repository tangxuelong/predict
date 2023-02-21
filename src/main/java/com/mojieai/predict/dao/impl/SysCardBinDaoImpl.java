package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.SysCardBinDao;
import com.mojieai.predict.entity.po.SysCardBin;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class SysCardBinDaoImpl extends BaseDao implements SysCardBinDao {

    @Override
    public SysCardBin getSysCardBinByBankCardPrefix(String bankCardPrefix) {
        Map<String, Object> param = new HashMap<>();
        param.put("bankCardPrefix", bankCardPrefix);
        return sqlSessionTemplate.selectOne("SysCardBin.getSysCardBinByBankCardPrefix", param);
    }
}
