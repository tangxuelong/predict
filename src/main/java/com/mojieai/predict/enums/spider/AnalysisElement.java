package com.mojieai.predict.enums.spider;

import com.mojieai.predict.entity.po.AwardInfo;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;

public interface AnalysisElement {
    /* 解析doc*/
    Map<String, Object> analysisDocument(long gameId, String periodId, Document doc);
}
