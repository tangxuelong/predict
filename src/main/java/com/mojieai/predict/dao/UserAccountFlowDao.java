package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserAccountFlow;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@TableShard(tableName = ConfigConstant.USER_ACCOUNT_FLOW_TABLE_NAME, shardType = ConfigConstant.USER_SHARD_TYPE,
        shardBy = ConfigConstant.USER_SHARD_BY)
public interface UserAccountFlowDao {
    UserAccountFlow getUserFlowByShardType(String flowId, Long userIdSuffix, Boolean isLock);

    UserAccountFlow getUserFlowCheck(String payId, Integer payType, Long userId, Boolean isLock);

    List<Map> getSumAmountByChannelId(Long userId, Timestamp beginTime, Timestamp endTime);

    Integer countUserFlow(Long userId);

    Integer sumAllCashFlow();

    Integer maxDayCashFlow(Timestamp beginDate,Timestamp endDate);

    PaginationList<UserAccountFlow> getUserFlowListByPage(Long userId, Integer payType, Integer page, Integer pageSize);

    void update(UserAccountFlow userAccountFlow);

    Integer updateFlowStatus(Long userId, String flowId, Integer setStatus, Integer oldStatus);

    Integer insert(UserAccountFlow userAccountFlow);

    Long getUserAllMoney(Long userId, Timestamp createTime);

    Long getTestUserMoney(Long userId, Timestamp beginTime, Timestamp endTime);

    Integer getUserRecentAccountFlow(Long userId, Integer payType, Integer payStatus);

    List<UserAccountFlow> getTestUserFlow(Long userId, Timestamp beginTime, Timestamp endTime);

    List<UserAccountFlow> getCashUserFlowByStatus(Long userPrx, Integer status);

    List<UserAccountFlow> getUserFlowByDate(Long userPrx, Timestamp startTime, Timestamp endTime);

    List<UserAccountFlow> getUserFlowByPayType(Long userIdSuffix, Integer payType, Integer status, Timestamp
            beginTime, Timestamp endTime);

    Integer getUserFlowCountByUserIdAndTime(Long userId, Timestamp beginTime, Timestamp endTime);

    /*********   otter   ***************/
    List<UserAccountFlow> getAllCashFlowFromOtter(Timestamp beginTime, Timestamp endTime);

    Integer getPayPersonCountFromOtter(Timestamp beginTime, Timestamp endTime);

    Long getRepurchaseAmountFromOtter(Timestamp beginTime, Timestamp endTime);

    Map<String, Object> getOrderNumAndAmountFromOtter(Timestamp beginTime, Timestamp endTime, Integer payType);

    Map<String,Object> getOldUserOrderFromOtter(Timestamp beginTime, Timestamp endTime);

    Integer getCountCashFlowFromOtterByDate(Timestamp beginDate, Timestamp endDate);
}
