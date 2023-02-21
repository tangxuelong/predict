package com.mojieai.predict.dao;

import com.mojieai.predict.entity.po.DanguanProgram;
import com.mojieai.predict.entity.po.DanguanProgramCards;

import java.util.List;

public interface DanguanProgramCardsDao {
    List<DanguanProgramCards> getDanguanProgramCards();

    DanguanProgramCards getDanguanProgramCardByCardId(Integer cardId);

    void update(DanguanProgramCards danguanProgramCards);

    void insert(DanguanProgramCards danguanProgramCards);
}
