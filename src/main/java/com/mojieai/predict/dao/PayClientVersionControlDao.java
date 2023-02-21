package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PayClientVersionControl;

import java.util.List;

public interface PayClientVersionControlDao {
    List<PayClientVersionControl> getAllPayClientVersionControl();

    PayClientVersionControl getPayClientVersionControl(Integer clientId, Integer channelId, Integer versionCode);

    void update(PayClientVersionControl payClientVersionControl);

    void insert(PayClientVersionControl payClientVersionControl);
}
