package com.mojieai.predict.redis.base.impl;

import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.constant.RedisConstant;
import com.mojieai.predict.redis.base.BaseRedis;
import com.mojieai.predict.redis.base.RedisService;
import com.mojieai.predict.util.SerializeUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.*;

//redis基础操作类，业务组合操作请使用RedisUtilServiceImpl
@Service
public class RedisServiceImpl implements RedisService {
    private static final Logger log = LogConstant.commonLog;

    @Autowired
    private BaseRedis baseRedis;

    @Override
    public Long kryoZRemRangeByRank(String key, int start, int end) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = jedis.zremrangeByRank(keyBytes, start, end);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Boolean kryoZAddSet(String key, Long timeLine, Object value) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);

                Long numMembers = jedis.zadd(keyBytes, timeLine, valueBytes);
                return numMembers.equals(RedisConstant.REDIS_DEFAULT_RESULT_LONG);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public Boolean kryoZAddSet(String key, Map<Object, Long> scoreMembers) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Map<byte[], Double> scoreMemberMap = new HashMap<>();
                for (Map.Entry<Object, Long> entry : scoreMembers.entrySet()) {
                    byte[] valueBytes = SerializeUtil.KryoSerialize(entry.getKey());
                    scoreMemberMap.put(valueBytes, new Double(entry.getValue()));
                }
                Long numMembers = jedis.zadd(keyBytes, scoreMemberMap);
                return numMembers.equals(RedisConstant.REDIS_DEFAULT_RESULT);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public Boolean kryoZAddSetStr(String key, Map<String, Double> scoreMembers) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                Long numMembers = jedis.zadd(key, scoreMembers);
                return numMembers.equals(RedisConstant.REDIS_DEFAULT_RESULT);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }


    @Override
    public <T> List<T> kryoZRangeByScoreGet(String key, Long min, Long max, Class type) {
        List<T> commandResult = new RedisCommand<List<T>>() {
            @Override
            public List<T> execute(JedisCluster jedis) throws IOException {

                List<T> resultList = new ArrayList<>();
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);

                Set docs = jedis.zrangeByScore(keyBytes, min, max);
                for (Object valueBytes : docs) {
                    resultList.add((T) SerializeUtil.KryoDeserialize((byte[]) valueBytes, type));
                }
                return resultList;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    public <T> List<T> kryoZRangeByScoreGet(String key, Long min, Long max, int offset, int limit, Class type) {
        List<T> commandResult = new RedisCommand<List<T>>() {
            @Override
            public List<T> execute(JedisCluster jedis) throws IOException {

                List<T> resultList = new ArrayList<>();
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);

                Set docs = jedis.zrangeByScore(keyBytes, min, max, offset, limit);

                for (Object valueBytes : docs) {
                    resultList.add((T) SerializeUtil.KryoDeserialize((byte[]) valueBytes, type));
                }
                return resultList;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T> List<T> kryoZRevRangeByScoreGet(String key, Long min, Long max, Class type) {
        List<T> commandResult = new RedisCommand<List<T>>() {
            @Override
            public List<T> execute(JedisCluster jedis) throws IOException {

                List<T> resultList = new ArrayList<>();
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);

                Set docs = jedis.zrevrangeByScore(keyBytes, max, min);

                for (Object valueBytes : docs) {
                    resultList.add((T) SerializeUtil.KryoDeserialize((byte[]) valueBytes, type));
                }
                return resultList;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T> List<T> kryoZRevRangeByScoreGet(String key, Long min, Long max, int x, int y, Class type) {
        List<T> commandResult = new RedisCommand<List<T>>() {
            @Override
            public List<T> execute(JedisCluster jedis) throws IOException {

                List<T> resultList = new ArrayList<>();
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);

                Set docs = jedis.zrevrangeByScore(keyBytes, max, min, x, y);

                for (Object valueBytes : docs) {
                    resultList.add((T) SerializeUtil.KryoDeserialize((byte[]) valueBytes, type));
                }
                return resultList;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T> List<T> kryoZRevRange(String key, long start, long end, Class type) {
        List<T> commandResult = new RedisCommand<List<T>>() {
            @Override
            List<T> execute(JedisCluster jedis) throws IOException {
                List<T> resultList = new ArrayList<>();
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);

                Set docs = jedis.zrevrange(keyBytes, start, end);
                for (Object valueBytes : docs) {
                    resultList.add((T) SerializeUtil.KryoDeserialize((byte[]) valueBytes, type));
                }
                return resultList;
            }

            @Override
            Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T> T kryoGet(String key, Class<T> type) {
        T commandResult = new RedisCommand<T>() {
            @Override
            public T execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = jedis.get(keyBytes);
                return (T) SerializeUtil.KryoDeserialize(valueBytes, type);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Boolean kryoSet(String key, Object value) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Boolean result = RedisConstant.REDIS_DEFAULT_OK.equalsIgnoreCase(jedis.set(keyBytes, valueBytes));
                return result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public <T> Set<T> kryoSmembers(String key, Class type) {
        Set<T> commandResult = new RedisCommand<Set<T>>() {
            @Override
            public Set<T> execute(JedisCluster jedis) throws IOException {

                Set<T> results = new HashSet<>();
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);

                Set docs = jedis.smembers(keyBytes);

                for (Object valueBytes : docs) {
                    results.add((T) SerializeUtil.KryoDeserialize((byte[]) valueBytes, type));
                }
                return results;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Integer kryoSRem(String key, Object value) {
        Integer commandResult = new RedisCommand<Integer>() {
            @Override
            Integer execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Long res = jedis.srem(keyBytes, valueBytes);
                if (res != null && res > 0) {
                    return 1;
                }
                return 0;
            }

            @Override
            Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Long kryoZUnionStore(String key, String keyF, String KeyS) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] keyFBytes = keyF.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] keySBytes = KeyS.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = jedis.zunionstore(keyBytes, keyFBytes, keySBytes);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || null == keyF || null == KeyS;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Long kryoZCard(String key) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = jedis.zcard(keyBytes);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Long kryoZCount(String key, Long min, Long max) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);

                Long result = jedis.zcount(keyBytes, min.doubleValue(), max.doubleValue());
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Long kryoZIncrby(String key, Long score, Object value) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Long result = jedis.zincrby(keyBytes, score.doubleValue(), valueBytes).longValue();
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Boolean kryoSetEx(String key, int seconds, Object value) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Boolean result = RedisConstant.REDIS_DEFAULT_OK.equalsIgnoreCase(jedis.setex(keyBytes, seconds,
                        valueBytes));
                return result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public Long kryoSetNx(String key, Object value) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Long result = jedis.setnx(keyBytes, valueBytes);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Long llen(String key) {
        Long commandResult = new RedisCommand<Long>() {

            @Override
            Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                return jedis.llen(keyBytes);
            }

            @Override
            Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T, S> Long kryoHset(String key, T field, S value) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] fieldBytes = SerializeUtil.KryoSerialize(field);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Long result = jedis.hset(keyBytes, fieldBytes, valueBytes);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || null == value;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public <T> Boolean kryoHExists(String key, T field) {
        Boolean commendResult = new RedisCommand<Boolean>() {
            @Override
            Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] fieldBytes = SerializeUtil.KryoSerialize(field);
                return jedis.hexists(keyBytes, fieldBytes);
            }

            @Override
            Boolean checkCommandParam() {
                return key == null;
            }
        }.command();
        return commendResult;
    }

    @Override
    public <T> Boolean kryoHDel(String key, T field) {

        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] fieldBytes = SerializeUtil.KryoSerialize(field);
                Long result = 0L;
                if (jedis.exists(keyBytes)) {
                    result = jedis.hdel(keyBytes, fieldBytes);
                }
                return (result != null && result > 0L);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = false;
        }
        return commandResult;
    }

    @Override
    public <T, S> Boolean kryoHmset(String key, Map<T, S> fieldValueMap) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Map<byte[], byte[]> valueMap = new HashMap<byte[], byte[]>();
                for (Map.Entry<T, S> entry : fieldValueMap.entrySet()) {
                    T entryKey = entry.getKey();
                    S entryOb = entry.getValue();
                    byte[] fieldBytes = SerializeUtil.KryoSerialize(entryKey);
                    byte[] valueBytes = SerializeUtil.KryoSerialize(entryOb);
                    valueMap.put(fieldBytes, valueBytes);
                }
                Boolean result = RedisConstant.REDIS_DEFAULT_OK.equalsIgnoreCase(jedis.hmset(keyBytes, valueMap));
                return result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || null == fieldValueMap;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public <T, S> T kryoHget(String key, S field, Class<T> type) {
        T commandResult = new RedisCommand<T>() {
            @Override
            public T execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] fieldBytes = SerializeUtil.KryoSerialize(field);
                byte[] valueBytes = jedis.hget(keyBytes, fieldBytes);
                return (T) SerializeUtil.KryoDeserialize(valueBytes, type);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T> Set<T> kryoHKeys(String key, Class<T> type) {
        Set<T> commandResult = new RedisCommand<Set<T>>() {
            @Override
            Set<T> execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Set<byte[]> tempList = jedis.hkeys(keyBytes);
                Set<T> resultList = new HashSet<>();
                for (byte[] temp : tempList) {
                    resultList.add((T) SerializeUtil.KryoDeserialize(temp, type));
                }
                return resultList;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <S, T> Map<S, T> kryoHgetAll(String key, Class<S> s, Class<T> t) {
        Map<S, T> commandResult = new RedisCommand<Map<S, T>>() {
            @Override
            public Map<S, T> execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Map<byte[], byte[]> fieldValueBytes = jedis.hgetAll(keyBytes);
                if (fieldValueBytes.size() == 0 || fieldValueBytes == null) {
                    return null;
                }
                Map<S, T> resultMap = new HashMap<>();
                for (Map.Entry<byte[], byte[]> entry : fieldValueBytes.entrySet()) {
                    byte[] entryKey = entry.getKey();
                    byte[] entryValue = entry.getValue();
                    resultMap.put((S) SerializeUtil.KryoDeserialize(entryKey, s), (T) SerializeUtil
                            .KryoDeserialize(entryValue, t));
                }
                return resultMap;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Long incr(String key) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = jedis.incr(keyBytes);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Long incrBy(String key, Long increment) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = jedis.incrBy(keyBytes, increment);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Long hdel(String key, String field) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] fieldBytes = field.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = 0L;
                if (jedis.exists(keyBytes)) {
                    result = jedis.hdel(keyBytes, fieldBytes);
                }
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Boolean isKeyByteExist(String key) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                return jedis.exists(keyBytes);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public Boolean isKeyExist(String key) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                return jedis.exists(key);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public Long del(String key) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = 0L;
                if (jedis.exists(keyBytes)) {
                    result = jedis.del(keyBytes);
                }
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Long expire(String key, int seconds) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = jedis.expire(keyBytes, seconds);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Long ttl(String key) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Long result = jedis.ttl(keyBytes);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    //list相关操作
    @Override
    public Long kryoRPush(String key, Object value) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Long length = jedis.rpush(keyBytes, valueBytes);//返回list长度
                return length;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || value == null;
            }
        }.command();
        if (commandResult == null) {
            return 0L;
        }
        return commandResult;
    }

    @Override
    public Long kryoLPush(String key, Object value) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Long length = jedis.lpush(keyBytes, valueBytes);//返回list长度
                return length;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || value == null;
            }
        }.command();
        if (commandResult == null) {
            return 0L;
        }
        return commandResult;
    }

    @Override
    public Long kryoLPushStr(String key, String[] value) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                Long length = jedis.lpush(key, value);//返回list长度
                return length;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || value == null;
            }
        }.command();
        if (commandResult == null) {
            return 0L;
        }
        return commandResult;
    }

    @Override
    public Long kryoLrem(String key, long count, Object value) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);
                Long result = jedis.lrem(keyBytes, count, valueBytes);
                return result == null ? 0L : result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = 0L;
        }
        return commandResult;
    }

    @Override
    public Boolean kryoLtrim(String key, long start, long end) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Boolean result = RedisConstant.REDIS_DEFAULT_OK.equalsIgnoreCase(jedis.ltrim(keyBytes, start, end));
                return result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public Boolean kryoSAddSet(String key, Object value) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);

                Long numMembers = jedis.sadd(keyBytes, valueBytes);
                return numMembers.equals(RedisConstant.REDIS_DEFAULT_RESULT);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public Boolean kryoSAddSets(String key, Set<String> value) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                int i = 0;
                byte[][] valueByte3 = new byte[value.size()][1];
                for (String temp : value) {
                    valueByte3[i] = SerializeUtil.KryoSerialize(temp);
                    i++;
                }
                Long numMembers = jedis.sadd(keyBytes, valueByte3);
                return numMembers.equals(Long.valueOf(value.size()));
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public <T> T kryoSrandMember(String key, Class<T> type) {
        T commandResult = new RedisCommand<T>() {
            @Override
            public T execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = jedis.srandmember(keyBytes);
                return (T) SerializeUtil.KryoDeserialize(valueBytes, type);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Boolean kryoSismemberSet(String key, Object value) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);

                return jedis.sismember(keyBytes, valueBytes);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    @Override
    public Double kryoZScore(String key, Object value) {
        Double commandResult = new RedisCommand<Double>() {

            @Override
            Double execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(value);

                return jedis.zscore(keyBytes, valueBytes);
            }

            @Override
            Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            commandResult = null;
        }
        return commandResult;
    }

    @Override
    public <T> T kryoLPop(String key, Class<T> type) {
        T commandResult = new RedisCommand<T>() {
            @Override
            public T execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = jedis.lpop(keyBytes);
                return (T) SerializeUtil.KryoDeserialize(valueBytes, type);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T> T kryoRPop(String key, Class<T> type) {
        T commandResult = new RedisCommand<T>() {
            @Override
            public T execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = jedis.rpop(keyBytes);
                return (T) SerializeUtil.KryoDeserialize(valueBytes, type);
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T> List<T> kryoLRange(String key, Long start, Long end, Class<T> type) {
        List<T> commandResult = new RedisCommand<List<T>>() {
            @Override
            public List<T> execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                List<byte[]> tempList = jedis.lrange(keyBytes, start, end);
                List<T> resultList = new ArrayList<>();
                for (byte[] temp : tempList) {
                    resultList.add((T) SerializeUtil.KryoDeserialize(temp, type));
                }
                return resultList;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || start == null || end == null;
            }
        }.command();
        return commandResult;
    }

    @Override
    public List<String> lRange(String key, Long start, Long end) {
        List<String> commandResult = new RedisCommand<List<String>>() {
            @Override
            public List<String> execute(JedisCluster jedis) throws IOException {
                List<String> resultList = jedis.lrange(key, start, end);
                return resultList;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || start == null || end == null;
            }
        }.command();
        return commandResult;
    }

    @Override
    public <T> T kryoLindex(String key, Integer index, Class<T> type) {
        T commandResult = new RedisCommand<T>() {
            @Override
            T execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = jedis.lindex(keyBytes, index);
                return (T) SerializeUtil.KryoDeserialize(valueBytes, type);
            }

            @Override
            Boolean checkCommandParam() {
                return null == key;
            }
        }.command();

        return commandResult;
    }

    //SortedSet
    @Override
    public <T> List<T> kryoZRange(String key, Long start, Long end, Class<T> type) {
        List<T> commandResult = new RedisCommand<List<T>>() {
            @Override
            public List<T> execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                Set<byte[]> set = jedis.zrange(keyBytes, start, end);//LinkedHashSet
                List<T> resultList = new ArrayList<>();
                for (byte[] temp : set) {
                    resultList.add((T) SerializeUtil.KryoDeserialize(temp, type));
                }
                return resultList;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || start == null || end == null;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Long kryoZRank(String key, Object awardNumber) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(awardNumber);
                Long rank = jedis.zrank(keyBytes, valueBytes);
                return rank;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || awardNumber == null;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Long kryoZRevRank(String key, Object awardNumber) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(awardNumber);
                Long rank = jedis.zrevrank(keyBytes, valueBytes);
                return rank;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || awardNumber == null;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Long kryoZRem(String key, Object val) {
        Long commandResult = new RedisCommand<Long>() {
            @Override
            public Long execute(JedisCluster jedis) throws IOException {
                byte[] keyBytes = key.getBytes(RedisConstant.REDIS_DEFAULT_CHARSET);
                byte[] valueBytes = SerializeUtil.KryoSerialize(val);
                Long value = jedis.zrem(keyBytes, valueBytes);
                return value;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key || val == null;
            }
        }.command();
        return commandResult;
    }


    @Override
    public String get(String key) {
        String commandResult = new RedisCommand<String>() {
            @Override
            public String execute(JedisCluster jedis) throws IOException {
                String value = jedis.get(key);
                return value;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        return commandResult;
    }

    @Override
    public Boolean set(String key, String value) {
        Boolean commandResult = new RedisCommand<Boolean>() {
            @Override
            public Boolean execute(JedisCluster jedis) throws IOException {
                Boolean result = RedisConstant.REDIS_DEFAULT_OK.equalsIgnoreCase(jedis.set(key, value));
                return result;
            }

            @Override
            public Boolean checkCommandParam() {
                return null == key;
            }
        }.command();
        if (commandResult == null) {
            return Boolean.FALSE;
        }
        return commandResult;
    }

    /* 没事不要用*/
    @Override
    public TreeSet<String> keys(String pattern) {
        JedisCluster jedis = getJedisCluster();
        Map<String, JedisPool> clusterNodes = jedis.getClusterNodes();
        TreeSet<String> keys = new TreeSet<>();
        for (String k : clusterNodes.keySet()) {
            JedisPool jp = clusterNodes.get(k);
            Jedis connection = jp.getResource();
            try {
                keys.addAll(connection.keys(pattern));
            } catch (Exception e) {
                log.error("Getting keys error: {}", e);
            } finally {
                connection.close();//用完一定要close这个链接！！！
            }
        }
        return keys;
    }

    abstract class RedisCommand<T> {
        abstract T execute(JedisCluster jedis) throws IOException;

        abstract Boolean checkCommandParam();

        public T command() {
            if (checkCommandParam()) {
                return null;
            }
            JedisCluster jedisCluster = getJedisCluster();
            try {
                return execute(jedisCluster);
            } catch (Exception e) {
                log.warn("RedisCommand error", e);
            }
            return null;
        }
    }

    public JedisCluster getJedisCluster() {
        return baseRedis.getJedisCluster();
    }
}
