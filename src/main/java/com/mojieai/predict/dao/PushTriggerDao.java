package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.PushTrigger;

import java.util.List;

public interface PushTriggerDao {
    List<PushTrigger> getAllPushRecords();

    List<PushTrigger> getAllNeedPushRecords();

    void update(PushTrigger pushTrigger);

    void insert(PushTrigger pushTrigger);
}
