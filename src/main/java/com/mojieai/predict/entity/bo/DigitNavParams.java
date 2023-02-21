package com.mojieai.predict.entity.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class DigitNavParams implements Serializable {
    private static final long serialVersionUID = -9082952863394941121L;

    private String ssqNavIds;
    private String dltNavIds;
    private String fc3dNavIds;
}
