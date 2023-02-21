package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.ActivityAwardLevelDao;
import com.mojieai.predict.dao.ActivityProgramDao;
import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.entity.po.ActivityAwardLevel;
import com.mojieai.predict.entity.po.ActivityProgram;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ActivityProgramDaoImpl extends BaseDao implements ActivityProgramDao {
    @Override
    public ActivityProgram getActivityProgramByProgramId(Integer programId, Boolean isLock) {
        Map<String, Object> params = new HashMap<>();
        params.put("programId", programId);
        params.put("isLock", isLock);
        return sqlSessionTemplate.selectOne("ActivityProgram.getActivityProgramByProgramId", params);
    }

    @Override
    public List<ActivityProgram> getActivityPrograms(String periodId) {
        Map<String, Object> params = new HashMap<>();
        params.put("periodId", periodId);
        return sqlSessionTemplate.selectList("ActivityProgram.getActivityPrograms", params);
    }

    @Override
    public void update(ActivityProgram activityProgram) {
        sqlSessionTemplate.update("ActivityProgram.update", activityProgram);
    }

    @Override
    public void insert(ActivityProgram activityProgram) {
        sqlSessionTemplate.update("ActivityProgram.insert", activityProgram);
    }
}
