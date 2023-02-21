package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.Game;

import java.util.List;

public interface GameDao {
    List<Game> getAllGame();

    void insert(Game game);

    void update(Game game);
}