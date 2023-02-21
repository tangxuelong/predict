package com.mojieai.predict.dao.impl;

import com.mojieai.predict.dao.BaseDao;
import com.mojieai.predict.dao.DanguanProgramCardsDao;
import com.mojieai.predict.dao.DanguanProgramDao;
import com.mojieai.predict.entity.po.DanguanProgram;
import com.mojieai.predict.entity.po.DanguanProgramCards;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DanguanProgramCardsDaoImpl extends BaseDao implements DanguanProgramCardsDao {
    @Override
    public List<DanguanProgramCards> getDanguanProgramCards() {
        return sqlSessionTemplate.selectList("DanguanProgramCards.getDanguanProgramCards");
    }

    @Override
    public DanguanProgramCards getDanguanProgramCardByCardId(Integer cardId) {
        Map<String, Object> params = new HashMap<>();
        params.put("cardId", cardId);
        return sqlSessionTemplate.selectOne("DanguanProgramCards.getDanguanProgramCardByCardId", params);
    }

    @Override
    public void update(DanguanProgramCards danguanProgramCards) {
        sqlSessionTemplate.update("DanguanProgramCards.update", danguanProgramCards);
    }

    @Override
    public void insert(DanguanProgramCards danguanProgramCards) {
        sqlSessionTemplate.insert("DanguanProgramCards.insert", danguanProgramCards);
    }
}
