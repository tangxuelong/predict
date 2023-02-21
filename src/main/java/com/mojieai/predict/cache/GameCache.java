package com.mojieai.predict.cache;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.GameDao;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.enums.CommonStatusEnum;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Singal
 */
public class GameCache {
    private static final Logger log = LogConstant.commonLog;

    private static Map<Long, Game> gameMap = new HashMap<>();
    private static Map<String, Game> gameEnMap = new HashMap<>();

    @Autowired
    private GameDao gameDao;

    private GameCache() {
    }

    public void init() {
        log.info("init Game");
        refresh();
    }

    public void refresh() {
        Map<Long, Game> tempGameMap = new HashMap<>();
        Map<String, Game> tempGameEnMap = new HashMap<>();
        List<Game> gameList = gameDao.getAllGame();
        if (gameList != null) {
            for (Game game : gameList) {
                tempGameMap.put(game.getGameId(), game);
                tempGameEnMap.put(game.getGameEn(), game);
            }
        }
        gameMap = tempGameMap;
        gameEnMap = tempGameEnMap;
        log.info("refresh " + (gameList == null ? 0 : gameList.size()) + " game");
    }

    public static Game getGame(Long gameId) {
        return gameMap.get(gameId);
    }

    public static Game getGame(String gameEn) {
        return gameEnMap.get(gameEn);
    }

    public static Map<Long, Game> getAllGameMap() {
        Map<Long, Game> resultMap = new HashMap<>();
        for (Map.Entry<Long, Game> entry : gameMap.entrySet()) {
            if (entry.getValue().getTaskSwitch() == CommonStatusEnum.YES.getStatus()) {
                resultMap.put(entry.getKey(), entry.getValue());
            }
        }
        return resultMap;
    }
}
