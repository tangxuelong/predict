package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.Mission;

import java.sql.Timestamp;
import java.util.List;

public interface MissionDao {
    Integer insert(Mission mission);

    void insertBatch(List<Mission> missions);

    Mission getTaskById(Long missionId);

    Mission getPartMissionByKeyInfo(String partMissionId);

    List<Mission> getSlaveMissionByClassId(String classId, Integer missionType, Integer missionStatus);

    Integer getCountByClassIdAndStatus(String classId, int missionType, int missionStatus);

    int updateMissionStatus(Long missionId, Integer setStatus, Integer status);

    //below method are used for test purpose
    Mission getMissionByKeyInfo(String keyInfo, Integer type);

    List<Mission> getSlaveMissionsByDate(Timestamp date);

    void insert2Bak(Mission missions);

    void deleteCompleteMissionById(Long missionId);

    Mission getSlaveBakMissionById(Long missionId);

    List<Long> getSlaveMissionIdsByTaskType(Integer missionType, Integer status);

    List<Long> getMonitorRefundMission();

    Integer updateMissionStatusByPartKey(String partKey, Integer missionStatusRefundWaite, Integer missionStatusInti);
}
