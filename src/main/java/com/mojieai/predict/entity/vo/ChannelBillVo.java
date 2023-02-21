package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChannelBillVo {
    private String channelName;
    private String groupName = "智慧彩票";
    private String orderCharge;
    private String serviceCharge;
    private String arriveAmount;
    private String off_amount;
    private String off_poundage;
}
