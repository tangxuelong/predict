package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserDeviceInfo;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@TableShard(tableName = ConfigConstant.USER_DEVICE_INFO_TABLE_NAME, shardType = ConfigConstant.USER_DEVICE_INFO_SHARD_TYPE,
        shardBy = ConfigConstant.USER_DEVICE_INFO_SHARD_BY)
public interface UserDeviceInfoDao {

    UserDeviceInfo getUserDeviceInfoByDeviceId(String deviceId);

    Long getDeviceIdByUserId(Long userId);

    void insert(UserDeviceInfo userDeviceInfo);

    int update(UserDeviceInfo userDeviceInfo);

    List<UserDeviceInfo> getAllUserDeviceInfoByShardType(String deviceId);

    List<UserDeviceInfo> getNewDeviceCountByDate(Timestamp beginDate,Timestamp endDate);

    /* 今日活跃设备*/
    int getDayActiveDeviceCountByDate(Timestamp beginDate,Timestamp endDate);

    // 获取所有设备个数
    int getAllDeviceCount();

    // 获取今日IOS设备个数
    int getIosDayActiveDeviceCountByDate(Timestamp beginDate, Timestamp endDate);
}
