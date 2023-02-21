package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserVipProgramIdSeq implements Serializable {
    private static final long serialVersionUID = -2839146646964424006L;

    private Long vipProgramIdSeq;
    private String stub;

}
