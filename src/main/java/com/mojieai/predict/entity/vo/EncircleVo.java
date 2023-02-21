package com.mojieai.predict.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EncircleVo {
    private Integer encircleCount;
    private String encircleCountName;
    private Integer[] killNumCounts;
}
