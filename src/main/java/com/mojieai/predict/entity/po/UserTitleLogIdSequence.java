package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserTitleLogIdSequence implements Serializable{
    private static final long serialVersionUID = -118250529059864238L;

    private Long titleLogIdSeq;
    private String stub;
}
