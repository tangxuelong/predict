package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserInfo;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by tangxuelong on 2017/7/8.
 */
@TableShard(tableName = ConfigConstant.USER_INFO_TABLE_NAME, shardType = ConfigConstant.USER_INFO_SHARD_TYPE,
        shardBy = ConfigConstant.USER_INFO_SHARD_BY)
public interface UserInfoDao {
    UserInfo getUserInfo(Long userId);

    UserInfo getUserInfo(Long userId, boolean lock);

    List<UserInfo> getUserInfoByNickNameFromOtter(String nickName);

    void insert(UserInfo userInfo);

    int update(UserInfo userInfo);

    int updateDeviceId(String deviceId, Long userId);

    Integer updateRemark(Long userId, String setRemark, String originRemark);

    // 获取所有用户信息
    List<UserInfo> geAllUserInfos();

    //时间段
    List<UserInfo> geUserInfosByDate(Timestamp beginDate,Timestamp endDate);

    // 获取所有用户数量
    Integer getCountAllUserInfos();

    // 新增用户数量
    Integer getTodayNewUserCount(Timestamp beginDate,Timestamp endDate);
}
