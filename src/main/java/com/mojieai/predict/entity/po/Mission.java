package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * MISSION
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class Mission {
    private Long missionId;
    private String keyInfo;
    private Integer missionType;
    private String remark;
    private Integer status;
    private String classId;
    private Timestamp createTime;
    private Timestamp updateTime;
    //唯一键约束KEY_INFO , TASK_TYPE

    public static int MISSION_TYPE_REFUND = 1;//退款
    public static int MISSION_TYPE_KILL_STATE_REFUND = 2;//杀号冷热态预测
    public static int MISSION_TYPE_FOOTBALL_WITHDRAW = 3;//足球推单收入
    public static int MISSION_TYPE_DISTRIBUTE_COUPON = 4;//派发优惠券
    public static int MISSION_TYPE_BANK_WITHDRAW = 5;//三方提现
    public static int MISSION_TYPE_SPORTS_VIP_REWARD = 6;//足彩会员奖励

    public static int MISSION_STATUS_INTI = 0;//初始
    public static int MISSION_STATUS_REFUND = 1;//已退款
    public static int MISSION_STATUS_NO_REFUND = 2;//不需要退
    public static int MISSION_STATUS_REFUND_WAITE = 3;//等待退款
    public static int MISSION_STATUS_WITHDRAW_CASH_WAITE = 4;//推单收入(余额)待进入现金账户
    public static int MISSION_STATUS_WITHDRAW_CASH_FINISH = 5;//推单收入已进入现金账户
    public static int MISSION_STATUS_WITHDRAW_WAIT_REFUND = 6;//推单收入等待退款
    public static int MISSION_STATUS_WITHDRAW_REFUND_FINISH = 7;//推单收入退款完成
    public static int MISSION_STATUS_COUPON_DISTRIBUTE = 8;//已派发

    public static int MISSION_STATUS_WITHDRAW_ACCEPTED = 9;//代付已受理
    public static int MISSION_STATUS_WITHDRAW_FINISH = 10;//代付成功(明确)
    public static int MISSION_STATUS_WITHDRAW_FAIL = 11;//代付失败（明确）


    public Mission(String keyInfo, Integer missionType, Integer status, Timestamp createTime, String remark, String
            classId) {
        this.keyInfo = keyInfo;
        this.missionType = missionType;
        this.status = status;
        this.createTime = createTime;
        this.remark = remark;
        this.classId = classId;
    }


    @Override
    public String toString() {
        return "Mission{" +
                "missionId=" + missionId +
                ", keyInfo='" + keyInfo + '\'' +
                ", missionType=" + missionType +
                ", remark='" + remark + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}