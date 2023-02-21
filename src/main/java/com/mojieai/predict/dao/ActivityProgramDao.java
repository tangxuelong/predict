package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.ActivityProgram;

import java.util.List;

public interface ActivityProgramDao {
    ActivityProgram getActivityProgramByProgramId(Integer programId,Boolean isLock);

    List<ActivityProgram> getActivityPrograms(String periodId);

    void update(ActivityProgram activityProgram);

    void insert(ActivityProgram activityProgram);
}
