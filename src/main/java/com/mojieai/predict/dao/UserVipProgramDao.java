package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.UserVipProgram;
import com.mojieai.predict.entity.po.VipProgram;

import java.util.List;

public interface UserVipProgramDao {

    UserVipProgram getUserVipProgramByPk(String prePayId);

    UserVipProgram getUserVipProgramByUnkey(Long userId, String programId);

    List<UserVipProgram> getUserVipProgram(Long userId);

    Integer update(UserVipProgram userVipProgram);

    Integer updatePayedStatus(String prePayId, Integer setPayStatus, Integer oldPayStatus);

    Integer insert(UserVipProgram userVipProgram);
}
