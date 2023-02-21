package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.DetailMatchInfo;
import com.mojieai.predict.entity.po.IndexMatchRecommend;
import com.mojieai.predict.entity.po.UserSportSocialRecommend;
import com.mojieai.predict.entity.vo.SportSocialRankVo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Created by tangxuelong on 2017/11/7.
 */
public interface SportSocialService {

    // 获取推荐信息
    Map<String, Object> getRecommendInfo(String matchId, Long userId, Integer playType);

    //保存推荐信息
    void saveUserRecommend(UserSportSocialRecommend userSportSocialRecommend, String clientIp, Integer clientId);

    // 生成推荐ID
    String generateRecommendId(Long userId);

    // 获取商品价格
    Long getRecommendPriceById(Integer itemId);

    // 获取比赛的截止时间
    Timestamp getMatchEndTime(String matchId);

    // 比赛结束更新排行榜
    void cronUpdateRankEndMatch();

    // 定时更新 更新时间为昨天的 用户数据
    void updateUserRankYesterday();

    // 每天更新数据到缓存
    void updateUserRankRedis();

    // 获取用户的排行榜vo
    SportSocialRankVo getSportSocialRankVo(Long userId);

    void calculateCancelMatch();

    // 排行榜接口
    Map<String, Object> getSportSocialRankList(Long userId, Integer rankType, Integer playType, Integer nextPage);

    // 排行榜指定个数数据
    List<Object> getSportSocialRankList(Integer rankType, Integer playType, Integer limit);

    Boolean addUserRecommendAndIndex(UserSportSocialRecommend recommend, IndexMatchRecommend recommendIndex);

    Integer getUserRecommend(Long userId, Timestamp beginOfToday, Timestamp endOfToday);

    void updateUserRankToDB(Long userId);

    Integer getAwardAmountByScore(DetailMatchInfo detailMatchInfo, Integer playType, String recommend, String handicap);

    Integer getUserRecommendIsRight(UserSportSocialRecommend recommend, DetailMatchInfo matchDetail);

    Integer followMatch(Long userId, String matchId);

    /* 检查用户是否关注这场比赛*/
    Integer checkUserFollowMatch(Long userId, String matchId);

    Map<String, Object> getMJMatchTag();

    Map<String, Object> getMJLeagueMatchList(String leagueId);

    Map<String, Object> getMJLeagueGroupMatch(String groupId);
}
