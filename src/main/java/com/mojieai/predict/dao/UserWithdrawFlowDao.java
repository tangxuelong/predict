package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserWithdrawFlow;

import java.sql.Timestamp;
import java.util.List;

@TableShard(tableName = ConfigConstant.USER_WITHDRAW_FLOW_TABLE_NAME, shardType = ConfigConstant
        .USER_WITHDRAW_FLOW_SHARD_TYPE, shardBy = ConfigConstant.USER_WITHDRAW_FLOW_SHARD_BY)
public interface UserWithdrawFlowDao {

    UserWithdrawFlow getUserWithdrawFlowById(Long userId, String withdrawId, Boolean isLock);

    PaginationList<UserWithdrawFlow> getUserWithdrawFlow(Long userId, Integer pageSize, Integer page);

    Long getUserWithdrawSumByTime(Long userId, Timestamp beginTime, Timestamp endTime);

    Integer updateWithdrawFlowStatusAndRemark(Long userId, String withdrawId, Integer newStatus, Integer oldStatus,
                                              String remark, Boolean saveWithdrawTime);

    Integer updateWithdrawFlowStatus(Long userId, String withdrawId, Integer newStatus, Integer oldStatus, Boolean
            saveWithdrawTime);

    Integer insert(UserWithdrawFlow userWithdrawFlow);

    //otter
    List<UserWithdrawFlow> getAllWithdrawOrderByStatusFromOtter(Integer withdrawStatus);

    Long getUserWithdrawTotalAmountByOtter(Timestamp beginTimeT, Timestamp endTimeT);
}
