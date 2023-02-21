package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserCouponIdSequence implements Serializable {
    private static final long serialVersionUID = -8444277193058067018L;

    private Long couponIdSeq;
    private String stub;
}
