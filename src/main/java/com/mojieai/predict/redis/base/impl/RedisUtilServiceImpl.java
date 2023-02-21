package com.mojieai.predict.redis.base.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.redis.base.BaseRedis;
import com.mojieai.predict.redis.base.RedisUtilService;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//redis业务组合操作工具类
@Service
public class RedisUtilServiceImpl implements RedisUtilService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private BaseRedis baseRedis;
}
