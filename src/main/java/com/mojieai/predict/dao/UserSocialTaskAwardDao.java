package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserSocialTaskAward;

import java.util.List;

@TableShard(tableName = ConfigConstant.SOCIAL_USER_SOCIAL_TASK_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_USER_SOCIAL_TASK_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_USER_SOCIAL_TASK_SHARD_BY)
public interface UserSocialTaskAwardDao {

    UserSocialTaskAward getUserSocialTaskAwardById(String taskId, Long userId, boolean isLock);

    UserSocialTaskAward getUserSocialTaskAwardByUnitKey(Long gameId, String periodId, Long userId, Integer taskType);

    Integer updateTaskTimesById(String taskId, Long userId, Integer taskTimes, Integer isAward);

    Integer updateTaskTimesById(String taskId, Long userId, Integer taskTimes, Integer oldTaskTimes, Integer isAward);

    Integer updateTaskIsAward(String taskId, Long userId, Integer isAward, Integer lastIsAwardType);

    Integer insert(UserSocialTaskAward userSocialTaskAward);

    List<UserSocialTaskAward> getEarlistNumUserSocialTask(Long userId, Integer isAward, Integer limitNum);
}
