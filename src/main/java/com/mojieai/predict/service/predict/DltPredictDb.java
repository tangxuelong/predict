package com.mojieai.predict.service.predict;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.constant.GameConstant;
import com.mojieai.predict.entity.po.Game;
import org.springframework.stereotype.Service;

@Service
public class DltPredictDb extends AbstractPredictDb {

    private String gameEn = GameConstant.DLT;

    @Override
    Game getGame() {
        return GameCache.getGame(gameEn);
    }
}
