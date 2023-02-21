package com.mojieai.predict.redis.base;

import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

//redis基础操作类，业务组合操作请使用RedisUtilService
public interface RedisService {

    Long kryoZRemRangeByRank(String key, int start, int end);

    Boolean kryoZAddSet(String key, Long timeline, Object value);

    Boolean kryoZAddSet(String key, Map<Object, Long> scoreMembers);

    Boolean kryoZAddSetStr(String key, Map<String, Double> scoreMembers);

    <T> List<T> kryoZRangeByScoreGet(String key, Long min, Long max, Class type);

    <T> List<T> kryoZRangeByScoreGet(String key, Long min, Long max, int x, int y, Class type);

    <T> List<T> kryoZRevRangeByScoreGet(String key, Long min, Long max, Class type);

    <T> List<T> kryoZRevRangeByScoreGet(String key, Long min, Long max, int x, int y, Class type);

    <T> List<T> kryoZRevRange(String key, long start, long end, Class type);

    Boolean kryoSismemberSet(String key, Object value);

    Double kryoZScore(String key, Object value);

    <T> T kryoSrandMember(String key, Class<T> type);

    Boolean kryoSAddSet(String key, Object value);

    Boolean kryoSAddSets(String key, Set<String> value);

    Boolean kryoSet(String key, Object value);

    <T> Set<T> kryoSmembers(String key, Class type);

    Integer kryoSRem(String key, Object value);

    Long kryoZUnionStore(String key, String keyF, String KeyS);

    Long kryoZCard(String key);

    Long kryoZCount(String key, Long min, Long max);

    Long kryoZIncrby(String key, Long score, Object value);

    Boolean kryoSetEx(String key, int seconds, Object value);

    Long kryoSetNx(String key, Object value);

    Long llen(String key);

    <T> T kryoGet(String key, Class<T> type);

    <T, S> Long kryoHset(String key, T field, S value);

    <T> Boolean kryoHExists(String key, T field);

    <T> Boolean kryoHDel(String key, T field);

    <T, S> Boolean kryoHmset(String key, Map<T, S> fieldValueMap);

    <T, S> T kryoHget(String key, S field, Class<T> type);

    <T> Set<T> kryoHKeys(String key, Class<T> type);

    <S, T> Map<S, T> kryoHgetAll(String key, Class<S> s, Class<T> t);

    Long incr(String key);

    Long incrBy(String key, Long increment);

    Long hdel(String key, String field);

    Boolean isKeyExist(String key);

    Boolean isKeyByteExist(String key);

    Long del(String key);

    Long expire(String key, int seconds);

    Long ttl(String key);

    Long kryoRPush(String key, Object value);

    Long kryoLPush(String key, Object value);

    Long kryoLPushStr(String key, String[] value);

    Long kryoLrem(String key, long count, Object value);

    Boolean kryoLtrim(String key, long start, long end);

    <T> T kryoLPop(String key, Class<T> type);

    <T> T kryoRPop(String key, Class<T> type);

    <T> List<T> kryoLRange(String key, Long start, Long end, Class<T> type);

    List<String> lRange(String key, Long start, Long end);

    <T> T kryoLindex(String key, Integer index, Class<T> type);

    <T> List<T> kryoZRange(String key, Long start, Long end, Class<T> type);

    Long kryoZRank(String key, Object awardNumber);

    Long kryoZRevRank(String key, Object awardNumber);

    Long kryoZRem(String key, Object value);

    String get(String key);

    Boolean set(String key, String value);

    TreeSet<String> keys(String pattern);

    JedisCluster getJedisCluster();
}
