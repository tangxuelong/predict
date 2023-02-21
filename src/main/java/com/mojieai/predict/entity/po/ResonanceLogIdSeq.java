package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ResonanceLogIdSeq implements Serializable{
    private static final long serialVersionUID = 3181270783144639664L;

    private Long logIdSeq;
    private String stub;
}
