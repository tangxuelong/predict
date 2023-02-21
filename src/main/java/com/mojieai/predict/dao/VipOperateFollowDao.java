package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.constant.VipMemberConstant;
import com.mojieai.predict.entity.po.VipOperateFollow;
import com.mojieai.predict.service.impl.PurchaseStatisticBaseDao;

@TableShard(tableName = ConfigConstant.VIP_OPERATE_FOLLOW_TABLE_NAME, shardType = ConfigConstant
        .VIP_OPERATE_FOLLOW_SHARD_TYPE, shardBy = ConfigConstant.VIP_OPERATE_FOLLOW_SHARD_BY)
public interface VipOperateFollowDao extends PurchaseStatisticBaseDao {

    VipOperateFollow getVipFollowByVipOperateCode(String vipOperateCode, Long userId);

    VipOperateFollow getVipFollowByFollowIdForUpdate(String vipOperateFollowId, boolean lock);

    Integer insert(VipOperateFollow vipOperateFollow);

    Integer updateVipOpreateFollowIsPay(String vipOperateCode, Integer isPay, String exchangeFlowId);
}
