package com.mojieai.predict.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Created by Singal
 */
@Data
@NoArgsConstructor
public class Game {
    private Long gameId;
    private String gameEn;
    private String gameName;
    private Integer periodLoaded;
    private Integer gameType;
    public static final int GAME_TYPE_COMMON = 1; // 大盘彩
    public static final int GAME_TYPE_HIGH_FREQUENCY = 2; // 高频彩

    private Integer usable;//彩种是否可用
    private Integer taskSwitch;//task是否启用
    private Integer taskTimeOffset;
}