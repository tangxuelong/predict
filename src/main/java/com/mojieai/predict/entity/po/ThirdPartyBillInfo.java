package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class ThirdPartyBillInfo implements Serializable {
    private static final long serialVersionUID = -2996453186488111984L;

    private Integer billId;
    private String rptDate;
    private Timestamp dealTime;
    private String mchId;
    private String thirdPartyId;
    private String orderId;
    private String status;
    private Double amount;
    private String refundThirdPartyId;
    private String refundId;
    private Double refundAmount;
    private String refundStatus;
    private String productName;
    private Double poundage;
    private Double poundageRate;
    private String businessType;
    private String remark;
    private Timestamp createTime;
    private Timestamp updateTime;
}
