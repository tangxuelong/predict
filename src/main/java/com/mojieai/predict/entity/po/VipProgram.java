package com.mojieai.predict.entity.po;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.*;

/**
 * 会员专区
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class VipProgram {
    private String programId; //方案ID
    private Integer awardNum;  //
    private Integer recommendNum; //推荐指数
    private Timestamp endTime; //截止时间
    private Long price; // 价格
    private String iconImg; //图标
    private String programInfo;//推荐内容
    private Integer isRight; //是否正确
    private Integer status;
    private Integer calMatchCount;
    private String matchIds;
    private Timestamp createTime;
    private Timestamp updateTime;

    public VipProgram(Integer awardNum, Integer recommendNum, String price, String programInfo) {
        this.programId = String.valueOf(System.currentTimeMillis());
        this.awardNum = awardNum;
        this.recommendNum = recommendNum;
        this.price = CommonUtil.convertYuan2Fen(price).longValue();
        this.createTime = DateUtil.getCurrentTimestamp();
        this.updateTime = DateUtil.getCurrentTimestamp();
        this.programInfo = programInfo;
        this.isRight = CommonConstant.VIP_PROGRAM_IS_RIGHT_INIT;
        this.status = CommonConstant.VIP_PROGRAM_STATUS_INIT;
        this.calMatchCount = 0;
        SortedSet<Integer> matchIdSet = new TreeSet<>();
        String[] matchInfos = programInfo.split(",");
        for (String matchInfo : matchInfos) {
            matchIdSet.add(Integer.valueOf(matchInfo.split(":")[0]));
        }
        StringBuilder sb = new StringBuilder();
        Iterator it = matchIdSet.iterator();
        while (it.hasNext()) {
            sb.append(it.next() + " ");
        }
        this.matchIds = sb.toString().trim().replaceAll(" ", ",");

    }
}