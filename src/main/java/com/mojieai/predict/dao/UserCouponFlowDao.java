package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserCouponFlow;

@TableShard(tableName = ConfigConstant.USER_COUPON_FLOW_TABLE_NAME, shardType = ConfigConstant
        .USER_COUPON_FLOW_SHARD_TYPE, shardBy = ConfigConstant.USER_COUPON_FLOW_SHARD_BY)
public interface UserCouponFlowDao {

    UserCouponFlow getUserCouponFlowById(Long userId, String couponFlowId);

    UserCouponFlow getUserCouponFlowByUniqueKey(Long userId, String exchangeId, Long couponId);

    Integer insert(UserCouponFlow userCouponFlow);
}
