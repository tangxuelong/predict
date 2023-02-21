package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.SysCardBin;

public interface SysCardBinDao {

    SysCardBin getSysCardBinByBankCardPrefix(String bankCardPrefix);
}
