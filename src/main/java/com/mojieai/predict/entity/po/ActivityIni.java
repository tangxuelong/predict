package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局配置表对应的ini
 *
 * @author Singal
 */
@Data
@NoArgsConstructor
public class ActivityIni {
    private String iniName;
    private String iniDesc;
    private String iniValue;
}