package com.mojieai.predict.entity.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by tangxuelong on 2017/12/29.
 */
@NoArgsConstructor
@Data
public class GoldTask {
    private Integer taskTimes;
    private Integer taskAward;

    public static String TASK_TYPE_USER_DAILY_SIGN = "0";
    public static String TASK_TYPE_SOCIAL_KILL_SSQ = "1";
    public static String TASK_TYPE_SOCIAL_ENCIRCLE_SSQ = "2";
    public static String TASK_TYPE_UPLOAD_HEAD_IMG = "3";
    public static String TASK_TYPE_MODIFY_NICK_NAME = "4";
    public static String TASK_TYPE_ADD_SPORTS_RECOMMEND = "5";
    public static String TASK_TYPE_PURCHASE_RECOMMEND = "6";
    public static String TASK_TYPE_SOCIAL_KILL_DLT = "7";
    public static String TASK_TYPE_SOCIAL_ENCIRCLE_DLT = "8";

    public static Integer DIGIT_SOCIAL_DAILY_TASK_GROUP_CN = 0;
    public static Integer SET_UP_PERSON_INFO_TASK_GROUP_CN = 1;
    public static Integer SPORTS_SOCIAL_DAILY_TASK_GROUP_CN = 2;

    public static String TASK_EN_USER_DAILY_SIGN = "dailySign";
    public static String TASK_EN_SOCIAL_KILL_SSQ = "socialKillSsq";
    public static String TASK_EN_SOCIAL_ENCIRCLE_SSQ = "socialEncircleSsq";
    public static String TASK_EN_UPLOAD_HEAD_IMG = "upHeadImg";
    public static String TASK_EN_MODIFY_NICK_NAME = "modifyName";
    public static String TASK_EN_ADD_SPORTS_RECOMMEND = "addSportsRecommend";
    public static String TASK_EN_PURCHASE_RECOMMEND = "purchaseSportsRecommend";
    public static String TASK_EN_SOCIAL_KILL_DLT = "socialKillDlt";
    public static String TASK_EN_SOCIAL_ENCIRCLE_DLT = "socialEncircleDlt";

    public GoldTask(Integer taskTimes, Integer taskAward) {
        this.taskTimes = taskTimes;
        this.taskAward = taskAward;
    }

    public static String getTaskName(String type) {
        if (TASK_TYPE_UPLOAD_HEAD_IMG.equals(type)) {
            return "上传头像";
        } else if (TASK_TYPE_MODIFY_NICK_NAME.equals(type)) {
            return "修改昵称";
        }
        return "";
    }

    public static String getTaskEn(String type) {
        if (type.equals(TASK_TYPE_USER_DAILY_SIGN)) {
            return "dailySign";
        } else if (type.equals(TASK_TYPE_SOCIAL_KILL_SSQ)) {
            return "socialKillSsq";
        } else if (type.equals(TASK_TYPE_SOCIAL_ENCIRCLE_SSQ)) {
            return "socialEncircleSsq";
        } else if (type.equals(TASK_TYPE_UPLOAD_HEAD_IMG)) {
            return "upHeadImg";
        } else if (type.equals(TASK_TYPE_MODIFY_NICK_NAME)) {
            return "modifyName";
        } else if (type.equals(TASK_TYPE_ADD_SPORTS_RECOMMEND)) {
            return "addSportsRecommend";
        } else if (type.equals(TASK_TYPE_PURCHASE_RECOMMEND)) {
            return "purchaseSportsRecommend";
        }
        return "";
    }

    public static String getGroupCn(Integer groupType) {
        if (groupType.equals(DIGIT_SOCIAL_DAILY_TASK_GROUP_CN)) {
            return "数字彩日常";
        } else if (groupType.equals(SET_UP_PERSON_INFO_TASK_GROUP_CN)) {
            return "完善任务";
        } else if (groupType.equals(SPORTS_SOCIAL_DAILY_TASK_GROUP_CN)) {
            return "足彩日常";
        }
        return "";
    }
}
