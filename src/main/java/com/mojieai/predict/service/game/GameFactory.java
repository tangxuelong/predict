package com.mojieai.predict.service.game;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.exception.BusinessException;
import com.mojieai.predict.util.SpringContextHolder;

public class GameFactory {
    private static GameFactory instance = new GameFactory();

    private GameFactory() {
    }

    public static GameFactory getInstance() {
        return instance;
    }

    public AbstractGame getGameBean(String gameEn) {
        AbstractGame ag = SpringContextHolder.getBean(gameEn + "Game");
        if (ag == null) {
            throw new BusinessException("游戏工厂中的对象不存在:" + gameEn);
        }
        return ag;
    }

    public AbstractGame getGameBean(Long gameId) {
        return getGameBean(GameCache.getGame(gameId).getGameEn());
    }
}
