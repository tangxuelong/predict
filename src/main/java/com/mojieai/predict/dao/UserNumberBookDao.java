package com.mojieai.predict.dao;

import com.mojieai.predict.annotation.TableShard;
import com.mojieai.predict.constant.ConfigConstant;
import com.mojieai.predict.entity.bo.PaginationList;
import com.mojieai.predict.entity.po.UserNumBookIdSequence;
import com.mojieai.predict.entity.po.UserNumberBook;

import java.util.List;

@TableShard(tableName = ConfigConstant.SOCIAL_USER_NUMBER_BOOK_TABLE_NAME, shardType = ConfigConstant
        .SOCIAL_USER_NUMBER_BOOK_SHARD_TYPE, shardBy = ConfigConstant.SOCIAL_USER_NUMBER_BOOK_SHARD_BY)
public interface UserNumberBookDao {

    Integer getUserNumBookCount(long gameId, Long userId);

    String getUserMostRomoteDateId(long gameId, Long userId);

    List<String> getCurrentPageLastPeriodId(long gameId, String lastNumId, Long userId, Integer periodSize);

    List<UserNumberBook> getUserNumsByUserIdAndLastNumId(long gameId, Long userId, String lastNumId, String periodId);

    PaginationList<UserNumberBook> getUserNumsByUserId(long gameId, Long userId, Integer currentPage, Integer pageSize);

    Integer updateUserNumEnable(String numId, Long userId, Integer isEnable);

    Integer insert(UserNumberBook userNumberBook);

    List<UserNumberBook> getOneTaleAllDataByPeriodId(Long userId, long gameId, String periodId);

    void updateUserNumBookNumsAndAwardDesc(String numId, Long userId, String nums, String awardDesc);
}
