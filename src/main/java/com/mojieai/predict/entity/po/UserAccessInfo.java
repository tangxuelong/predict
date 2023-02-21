package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账户余额
 *
 * @author tangxuelong
 */
@Data
@NoArgsConstructor
public class UserAccessInfo {
    private Integer accessId;
    private String accessApi;
}