package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.po.UserProgram;

import java.util.List;

@TableShard(tableName = ConfigConstant.USER_PROGRAM_TABLE_NAME, shardType = ConfigConstant.USER_PROGRAM_SHARD_TYPE,
        shardBy = ConfigConstant.USER_PROGRAM_SHARD_BY)
public interface UserProgramDao {

    UserProgram getUserProgramByUserProgramId(String userProgramId, boolean isLock);

    UserProgram getUserProgramByProgramId(Long userId, String programId, Long payPrice);

    List<UserProgram> getAllUserProgramByProgramId(String programId, Integer isPay, Long userIdPre);

    // 全部纪录
    List<UserProgram> getUserPrograms(Long gameId, Long userId);

    // 中奖纪录
    List<UserProgram> getUserAwardProgram(Long gameId, Long userId, Integer isAward);

    List<UserProgram> getUserProgramsByLastPeriodId(long gameId, String maxPeriodId, String minPeriodId, Long userId);

    List<String> getUserProgramPagePeriodId(long gameId, String lastPeriodId, Long userId, int count);

    void update(UserProgram userProgram);

    int insert(UserProgram userProgram);

    int updateUserProgramPayStatus(String userProgramId, int isPayStatus);

    int updateUserProgramRefundStatus(String userProgramId, Integer refundStatus);
}
