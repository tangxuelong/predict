package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserActive implements Serializable {
    private static final long serialVersionUID = -5090502887903923539L;

    private Long userId;
    private Integer activeDate;

    public UserActive(Long userId, Integer activeDate) {
        this.userId = userId;
        this.activeDate = activeDate;
    }
}
