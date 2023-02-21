package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserWithdrawFlowIdSeq implements Serializable {
    private static final long serialVersionUID = -7782946350466351394L;

    private Long withdrawIdSequence;
    private String stub;
}
