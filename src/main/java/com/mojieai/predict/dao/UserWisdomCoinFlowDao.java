package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserWisdomCoinFlow;

import java.sql.Timestamp;

@TableShard(tableName = ConfigConstant.USER_WISDOM_COIN_FLOW_TABLE_NAME, shardType = ConfigConstant
        .USER_WISDOM_COIN_FLOW_SHARD_TYPE, shardBy = ConfigConstant.USER_WISDOM_COIN_FLOW_SHARD_BY)
public interface UserWisdomCoinFlowDao {

    PaginationList<UserWisdomCoinFlow> getUserWisdomCoinFlowsByPage(Long userId, Integer pageSize, Integer page);

    UserWisdomCoinFlow getUserWisdomCoinFlowByFlowId(String businessFlowId, Long userId);

    Integer insert(UserWisdomCoinFlow userWisdomCoinFlow);

    Integer updateUserWisdomFlowIsPay(String flowId, Long userId, Integer isPay);

    Integer saveUserWisdomCoinAccountFlowId(String flowId, Long userId, String exchangeFlowId);

    Long getUserWisdomCoinFlowSumByStatusByOtter(Timestamp beginTime, Timestamp endTime, Integer exchangeType);
}
