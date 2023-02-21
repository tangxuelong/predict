package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserCouponFlowIdSeq implements Serializable {
    private static final long serialVersionUID = 3621818821419106347L;

    private Long couponFlowIdSeq;
    private String stub;
}
