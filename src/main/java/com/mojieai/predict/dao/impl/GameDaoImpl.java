package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.GameDao;
import com.mojieai.predict.entity.po.Game;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GameDaoImpl extends BaseDao implements GameDao {
    @Override
    public List<Game> getAllGame() {
        return sqlSessionTemplate.selectList("Game.getAllGame");
    }

    @Override
    public void insert(Game game) {
        sqlSessionTemplate.insert("Game.insert", game);
    }

    @Override
    public void update(Game game) {
        sqlSessionTemplate.update("Game.update", game);
    }

}