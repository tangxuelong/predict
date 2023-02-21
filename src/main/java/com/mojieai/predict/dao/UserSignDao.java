package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserSign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TableShard(tableName = ConfigConstant.USER_SIGN_TABLE_NAME, shardType = ConfigConstant.USER_SIGN_SHARD_TYPE,
        shardBy = ConfigConstant.USER_SIGN_SHARD_BY)
public interface UserSignDao {

    UserSign getUserSignByUserIdAndDate(Long userId, String signDate, Integer signType);

    List<UserSign> getAllNeedRewardSign(Long userId);

    Integer getUserSignCountByIntervalDate(Long userId, Integer signType, String beginDate, String endDate);

    Integer updateUserSignRewardStatus(Long userId, String signDate, Integer signType, Integer ifReward);

    Integer updateSignRewardStatusBySignCode(Long userId, Long signCode, Integer ifReward);

    Integer insert(UserSign userSign);
}
