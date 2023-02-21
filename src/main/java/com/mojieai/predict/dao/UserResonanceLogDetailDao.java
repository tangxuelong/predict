package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserResonanceLogDetail;

public interface UserResonanceLogDetailDao {

    Integer updatePayStatusByResonanceLogId(String resonanceLogId, Integer setPayStatus, Integer originPayStatus,
                                            Long userId);

    Integer insert(UserResonanceLogDetail userResonanceLogDetail);
}
