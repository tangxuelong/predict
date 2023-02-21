package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Img implements Serializable {
    private static final long serialVersionUID = 6810934565830982491L;

    private Integer imgId;
    private String imgUrl;
    private String imgDesc;
}
