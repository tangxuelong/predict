package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserBuyRecommendIdSeq implements Serializable {
    private static final long serialVersionUID = -8437926354648700095L;

    private Long logIdSeq;
    private String stub;
}
