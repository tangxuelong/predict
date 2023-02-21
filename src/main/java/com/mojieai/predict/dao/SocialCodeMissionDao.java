package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.Mission;
import com.mojieai.predict.entity.po.SocialCodeMission;

import java.sql.Timestamp;
import java.util.List;

public interface SocialCodeMissionDao {
    void insert(SocialCodeMission mission);

    void insertBatch(List<SocialCodeMission> missions);

    Mission getTaskById(Long missionId);

    int updateMissionStatus(Long missionId, Integer setStatus, Integer status);

    //below method are used for test purpose
    SocialCodeMission getMissionByKeyInfo(String keyInfo, Integer type);

    List<SocialCodeMission> getSlaveMissionsByDate(Timestamp date);

    void insert2Bak(SocialCodeMission missions);

    void deleteCompleteMissionById(Long missionId);

    SocialCodeMission getSlaveBakMissionById(Long missionId);

    List<Long> getSlaveMissionIdsByTaskType(Integer missionType, Integer status, String gameEn, Timestamp date);
}
