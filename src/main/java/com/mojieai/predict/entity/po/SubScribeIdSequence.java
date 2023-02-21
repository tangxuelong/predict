package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class SubScribeIdSequence implements Serializable {
    private static final long serialVersionUID = -5045447602209064850L;

    private Long logIdSeq;
    private String stub;
}
