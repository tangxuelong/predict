package com.mojieai.predict.enums;

import com.alibaba.fastjson.JSONObject;
import com.mojieai.predict.cache.ActivityIniCache;
import com.mojieai.predict.constant.ActivityIniConstant;
import com.mojieai.predict.entity.bo.GoldTask;

import java.util.Map;

public enum GoldCoinTaskEnum {
    UPLOAD_HEAD_IMG_TASK(GoldTask.TASK_TYPE_UPLOAD_HEAD_IMG, GoldTask.SET_UP_PERSON_INFO_TASK_GROUP_CN, GoldTask
            .TASK_EN_UPLOAD_HEAD_IMG),
    MODIFY_NICK_NAME_TASK(GoldTask.TASK_TYPE_MODIFY_NICK_NAME, GoldTask.SET_UP_PERSON_INFO_TASK_GROUP_CN, GoldTask
            .TASK_EN_MODIFY_NICK_NAME),
    ADD_SPORTS_RECOMMEND_TASK(GoldTask.TASK_TYPE_ADD_SPORTS_RECOMMEND, GoldTask.SPORTS_SOCIAL_DAILY_TASK_GROUP_CN,
            GoldTask.TASK_EN_ADD_SPORTS_RECOMMEND),
    PURCHASE_SPORTS_RECOMMEND_TASK(GoldTask.TASK_TYPE_PURCHASE_RECOMMEND, GoldTask.SPORTS_SOCIAL_DAILY_TASK_GROUP_CN,
            GoldTask.TASK_EN_PURCHASE_RECOMMEND),
    SSQ_SOCIAL_KILL_NUM_TASK(GoldTask.TASK_TYPE_SOCIAL_KILL_SSQ, GoldTask.DIGIT_SOCIAL_DAILY_TASK_GROUP_CN,
            GoldTask.TASK_EN_SOCIAL_KILL_SSQ),
    SSQ_SOCIAL_ENCIRCLE_NUM_TASK(GoldTask.TASK_TYPE_SOCIAL_ENCIRCLE_SSQ, GoldTask.DIGIT_SOCIAL_DAILY_TASK_GROUP_CN,
            GoldTask.TASK_EN_SOCIAL_ENCIRCLE_SSQ),
    DLT_SOCIAL_KILL_NUM_TASK(GoldTask.TASK_TYPE_SOCIAL_KILL_DLT, GoldTask.DIGIT_SOCIAL_DAILY_TASK_GROUP_CN,
            GoldTask.TASK_EN_SOCIAL_KILL_DLT),
    DLT_SOCIAL_ENCIRCLE_NUM_TASK(GoldTask.TASK_TYPE_SOCIAL_ENCIRCLE_DLT, GoldTask.DIGIT_SOCIAL_DAILY_TASK_GROUP_CN,
            GoldTask.TASK_EN_SOCIAL_ENCIRCLE_DLT);

    GoldCoinTaskEnum(String taskType, Integer groupType, String taskEn) {
        this.taskType = taskType;
        this.groupType = groupType;
        this.taskEn = taskEn;
    }

    private String taskType;
    private String taskEn;
    private Integer groupType;

    public String getTaskType() {
        return taskType;
    }

    public Integer getGroupType() {
        return groupType;
    }

    public String getTaskEn() {
        return taskEn;
    }

    public static GoldCoinTaskEnum getGoldCoinTaskEnumByType(String taskType) {
        for (GoldCoinTaskEnum goldCoinTaskEnum : GoldCoinTaskEnum.values()) {
            if (goldCoinTaskEnum.getTaskType().equals(taskType)) {
                return goldCoinTaskEnum;
            }
        }
        return null;
    }

    public GoldTask getGoldTask() {
        Map<String, Object> tasksConfig = JSONObject.parseObject(ActivityIniCache.getActivityIniValue
                (ActivityIniConstant.TASK_LIST_MAP));
        if (tasksConfig != null && tasksConfig.containsKey(getTaskType())) {
            Map<String, Object> encircleAward = (Map<String, Object>) tasksConfig.get(getTaskType());
            return new GoldTask((Integer) encircleAward.get("taskTimes"), (Integer) encircleAward.get("taskAward"));
        }
        return null;
    }
}
