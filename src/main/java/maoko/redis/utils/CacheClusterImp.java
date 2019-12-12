package maoko.redis.utils;

import maoko.common.ExceptionUtil;
import maoko.common.StrConUtil;
import maoko.common.model.net.CusHostAndPort;
import maoko.redis.utils.entity.cluster.SvrNodeInfo;
import maoko.redis.utils.entity.cluster.StateType;
import maoko.redis.utils.entity.cluster.SvrNodeState;
import maoko.redis.utils.core.JedisClusterPipeline;
import maoko.redis.utils.core.RedisClusterUtil;
import maoko.redis.utils.core.RedisDistribtLock;
import maoko.redis.utils.core.RedisService;
import maoko.redis.utils.enm.DataOptType;
import maoko.redis.utils.entity.*;
import maoko.redis.utils.entity.MyTuple;
import maoko.redis.utils.except.CusException;
import maoko.redis.utils.except.ErrorCode;
import maoko.redis.utils.ifs.*;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * 集群缓存助手-分布式集群模式
 *
 * @author fanpei
 * @date 2017年9月5日下午7:25:30
 */
@Deprecated
public class CacheClusterImp implements ICache {
    private static final String OOMSTR = "OOM command not allowed when used memory > 'maxmemory'.";
    // private JedisClusterPipeline jcp;// 管道模式
    private JedisCluster jedisCmd;
    private boolean isInited;

    // [START] 公共模块
    @Override
    public void start() throws CusException {
        try {
            RedisClusterUtil.initRedisClient(CacheFactory.redisConfig);
            jedisCmd = RedisClusterUtil.GetjedisCluster();
            if (jedisCmd == null) {
                throw new CusException(ErrorCode.REDIS_NOT_INIT, "the redis not inited");
            }
            jedisCmd.get("");
        } catch (Exception e) {
            throw new CusException(ErrorCode.REDIS_NOT_INIT,
                    String.format("redis not inited: %s", ExceptionUtil.getCauseMessage(e)));
        }
        isInited = true;
    }

    @Override
    public void stop() {
        if (!isInited)
            return;
        try {
            RedisClusterUtil.GetjedisCluster().close();
        } catch (IOException e) {
            // throw new CusException(ErrorCode.RedisCacheError,
            // ExceptionUtil.getCauseMessage("关闭缓存发生错误 ", e));
        }

    }

    public List<CusHostAndPort> getNodes() {
        return RedisClusterUtil.getNodes();
    }

    @Override
    public boolean containKey(String key) throws CusException {
        boolean exists = false;
        try {
            exists = jedisCmd.exists(key);
        } catch (Exception e) {
            throw new CusException(String.format("检查一级key %s 是否存在于redis中发生错误", key), e);
        }
        return exists;
    }

    @Override
    public boolean containKey(byte[] rowkey, byte[] secKey) throws CusException {
        boolean exists = false;
        try {
            exists = jedisCmd.hexists(rowkey, secKey);
        } catch (Exception e) {
            throw new CusException(String.format("检查二级key %s 是否存在于redis中发生错误 ", secKey), e);
        }
        return exists;
    }

    @Override
    public void deleteDataByKey(String rowkey) throws CusException {

        // RedisClusterLock lock = new RedisClusterLock(jedis, rowkey);
        try {
            // lock.lock();
            jedisCmd.del(rowkey);
        } catch (Exception e) {
            String err = ExceptionUtil.getCauseMessage(String.format("从缓存中删除数据  %s 错误 ", rowkey), e);
            throw new CusException(ErrorCode.RedisCacheError, err);
        } finally {
            // lock.unlock();
        }
    }

    @Override
    public void deleteDataByKey(byte[] rowKey, byte[] secKey) throws CusException {
        try {
            jedisCmd.hdel(rowKey, secKey);
        } catch (Exception e) {
            String err = ExceptionUtil.getCauseMessage(String.format("从缓存中删除二级数据  %s 错误", secKey), e);
            throw new CusException(ErrorCode.RedisCacheError, err);

        } finally {
            // lock.unlock();
        }

    }

    public int batchDeleteDatas(Collection<String> secKeys) throws CusException {
        int num = 0;
        try {
            JedisClusterPipeline jcp = JedisClusterPipeline.pipelined(jedisCmd);
            jcp.refreshCluster();
            for (String key : secKeys) {
                jcp.del(key);
            }
            List<Object> resluts = jcp.syncAndReturnAll();
            if (resluts != null) {
                num = resluts.size();
            }
        } catch (Exception e) {
            throw new CusException(ErrorCode.RedisCacheError,
                    ExceptionUtil.getCauseMessage(String.format("从缓存中批量删除keys数据错误"), e));
        } finally {
        }
        return num;
    }

    public void writeDataToSortZet(String key, double Socre, String value, int expireTime) throws CusException {
        try {
            if (1 == jedisCmd.zadd(key, Socre, value))
                throw new CusException(ErrorCode.RedisCacheError, "添加操作返回为False");
            setKeyExpire(key, expireTime);
        } catch (Exception e) {
            if (e instanceof JedisException) {
                JedisException je = (JedisException) e;
                if (OOMSTR.equals(je.getMessage())) {
                    throw new CusException(ErrorCode.REDIS_OOM, "数据达到redis上限，丢包处理，写入数据失败");
                } else
                    throw new CusException(ErrorCode.RedisCacheError,
                            ExceptionUtil.getCauseMessage(String.format("批处理写入数据  %s 至缓存错误", key), e));
            }
        }
    }

    public void writeDataToSortZet(String key, List<ScoreElement> scorEles, int expireTime) throws CusException {
        try {
            JedisClusterPipeline jcp = JedisClusterPipeline.pipelined(jedisCmd);
            jcp.refreshCluster();
            for (ScoreElement data : scorEles) {
                jcp.zadd(key, data.getSocre(), data.getValue());
            }
            jcp.syncAndReturnAll();
            setKeyExpire(key, expireTime);
        } catch (Exception e) {
            if (e instanceof JedisException) {
                JedisException je = (JedisException) e;
                if (OOMSTR.equals(je.getMessage())) {
                    throw new CusException(ErrorCode.REDIS_OOM, "数据达到redis上限，丢包处理，写入数据失败");
                } else
                    throw new CusException(ErrorCode.RedisCacheError,
                            ExceptionUtil.getCauseMessage(String.format("批处理写入数据  %s 至缓存错误", key), e));
            }
        } finally {
            // RedisClusterUtil.returnResource(jd);
        }
    }

    public Map<String, MyTuple> getAllDataFromSortZet(String key) throws CusException {
        Map<String, MyTuple> results = new HashMap<String, MyTuple>();
        Set<redis.clients.jedis.Tuple> redisResult = null;
        try {
            redisResult = jedisCmd.zrangeWithScores(key, 0, -1);
            if (redisResult != null && !redisResult.isEmpty()) {
                for (redis.clients.jedis.Tuple t : redisResult) {
                    MyTuple T = new MyTuple(t.getElement(), t.getScore());
                    results.put(Long.toString((long) t.getScore()), T);
                }
            }
        } catch (Exception e) {
            throw new CusException(ErrorCode.RedisCacheError,
                    ExceptionUtil.getCauseMessage(String.format("读取数据  %s 从缓存发生错误", key), e));
        }
        return results;
    }

    /*
     * 批处理插入数据
     *
     * @see CloudPanSys.Interface.Cache.ICache#writeData(java.lang.String,
     * java.util.List, int)
     */
    public int batchWriteDatas(Collection<ICacheKeyData> listdatas, int expireTime) throws CusException {
        int num = 0;
        try {
            JedisClusterPipeline jcp = JedisClusterPipeline.pipelined(jedisCmd);
            jcp.refreshCluster();
            Collection<byte[]> keys = new ArrayList<>();
            for (ICacheKeyData data : listdatas) {
                byte[] firstKeys = data.getFirstKeys();
                byte[] datas = data.getValueDatas();
                if (datas == null || firstKeys == null)
                    continue;
                if (!keys.contains(firstKeys))// this block has error
                    jcp.expire(firstKeys, expireTime);
                jcp.hset(firstKeys, data.getSencondKeys(), datas);
            }
            List<Object> resluts = jcp.syncAndReturnAll();
            if (resluts != null) {
                num = resluts.size() - 1;
            }
            // SetKeyExpire(key, expireTime);
        } catch (Exception e) {
            if (e instanceof JedisException) {
                JedisException je = (JedisException) e;
                if (OOMSTR.equals(je.getMessage())) {
                    throw new CusException(ErrorCode.REDIS_OOM, "数据达到redis上限，丢包处理，写入数据失败");
                } else
                    throw new CusException("批处理写入数据 至缓存错误", e);
            }
        } finally {
            // RedisClusterUtil.returnResource(jd);
        }
        return num;
    }

    @Override
    public void writeData(String key, byte[] datas, int expireTime) throws CusException {
        byte[] keys = key.getBytes();
        try {
            if (datas == null)
                datas = "".getBytes();
            if (!"OK".equals(jedisCmd.set(keys, datas)))
                throw new CusException(ErrorCode.RedisCacheError, "添加操作返回为False");
            setKeyExpire(key, expireTime);

        } catch (Exception e) {
            if (e instanceof JedisException) {
                JedisException je = (JedisException) e;
                if (OOMSTR.equals(je.getMessage())) {
                    throw new CusException(ErrorCode.REDIS_OOM, "数据达到redis上限，丢包处理，写入数据失败");
                } else
                    throw new CusException(ErrorCode.RedisCacheError,
                            ExceptionUtil.getCauseMessage(String.format("写入数据  %s 至缓存错误", key), e));
            }

        } finally {
            // lock.unlock();
        }
    }

    @Override
    public void writeData(byte[] key, byte[] SecKey, byte[] datas, int expireTime) throws CusException {

    }

    public void writeData(String key, String SecKey, byte[] datas) throws CusException {
        int expireTime = -1;
        writeDataAndExpire(key, SecKey, datas, expireTime);
    }

    private void writeDataAndExpire(String key, String SecKey, byte[] datas, int expireTime) throws CusException {
        try {
            Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
            if (datas == null)
                datas = "".getBytes();
            map.put(SecKey.getBytes(), datas);
            if (!"OK".equals(jedisCmd.hmset(key.getBytes(), map)))
                throw new CusException(ErrorCode.RedisCacheError, "添加操作返回为False");
            setKeyExpire(key, expireTime);

        } catch (Exception e) {
            if (e instanceof JedisException) {
                JedisException je = (JedisException) e;
                if (OOMSTR.equals(je.getMessage())) {
                    throw new CusException(ErrorCode.REDIS_OOM, "数据达到redis上限，丢包处理，写入数据失败");
                } else
                    throw new CusException(ErrorCode.RedisCacheError, je.getMessage());
            }
        } finally {
        }
    }

    @Override
    public Map<ICacheSecnKey, Response<byte[]>> batchGetDatas(byte[] keyBytes, Collection<ICacheSecnKey> secKeys)
            throws CusException {
        /*
         * Map<String, byte[]> resluts = null; Jedis jedisCmd = null; try { jedisCmd =
         * RedisSingle_SentineUtil.getJedisCli(); Pipeline jcp = jedisCmd.pipelined();
         * HashMap<String, Response<byte[]>> newMap = new HashMap<String,
         * Response<byte[]>>(secKeys.size()); for (ICacheSecnKey feild : secKeys) {
         * newMap.put(feild.getKey(), jcp.hget(keyBytes, feild.getKeyBytes())); }
         * jcp.sync();
         *
         * if (!newMap.isEmpty()) { resluts = new LinkedHashMap<String,
         * byte[]>(newMap.size()); for (ICacheSecnKey feild : secKeys) { byte[] datas =
         * newMap.get(feild.getKey()).get(); if (null != datas)
         * resluts.put(feild.getKey(), datas); } } } catch (Exception e) { throw new
         * CusException(ErrorCode.RedisCacheError,
         * ExceptionUtil.getCauseMessage(String.format("从缓存批处理获取数据%s失败 "), e)); }
         * finally { RedisSingle_SentineUtil.returnResource(jedisCmd); }
         */
        return null;
    }

    @Override
    public byte[] getData(byte[] key, byte[] secKey) throws CusException {
        byte[] returnObj = null;
        try {
            List<byte[]> objs = jedisCmd.hmget(key, secKey);
            if (objs != null && !objs.isEmpty()) {
                for (byte[] bs : objs) {
                    if (bs != null && bs.length > 0) {
                        returnObj = bs;
                    }
                }
            }
        } catch (Exception e) {
            throw new CusException(ErrorCode.RedisCacheError,
                    ExceptionUtil.getCauseMessage(String.format("从缓存获对象 %s-%s 失败 ", key, secKey), e));
        }
        return returnObj;
    }

    @Override
    public byte[] getData(String key) throws CusException {
        byte[] returnObj = null;
        try {
            returnObj = jedisCmd.get(key.getBytes());
        } catch (Exception e) {
            throw new CusException(ErrorCode.RedisCacheError,
                    ExceptionUtil.getCauseMessage(String.format("从缓存获对象  %s 失败 ", key), e));
        }
        return returnObj;
    }

    @Override
    public List<String> getAllSecondKeyByKey(String rowKey) throws CusException {
        List<String> pos = new ArrayList<String>();
        Set<String> secStrs = null;
        try {
            secStrs = jedisCmd.hkeys(rowKey);
        } catch (Exception e) {
            throw new CusException(ErrorCode.RedisCacheError,
                    ExceptionUtil.getCauseMessage(String.format("获取文件  %s 已有缓存区段失败", rowKey), e));
        }

        pos.addAll(secStrs);

        return pos;
    }

    private SvrNodeState States = new SvrNodeState();

    @Override
    public SvrNodeState getSvrStates() {
        List<SvrNodeInfo> infos = RedisClusterUtil.svrNodeInfos;
        boolean clusterNormal;
        try {
            clusterNormal = RedisService.clusterNormal();
            States.setState(clusterNormal ? StateType.Normal : StateType.Trouble);
            try {
                if (clusterNormal) {
                    infos = RedisService.getClusterNodes();
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
            clusterNormal = false;
        }

        States.setNodes(infos);
        return States;
    }

    @Override
    public List<String> getClientsAllFilterKey(String pattern) {
        String searchKey = String.format("%s*", pattern);
        Set<String> keysSet = new HashSet<String>();
        List<JedisPool> clients = new ArrayList<JedisPool>();
        Map<String, JedisPool> nodes = jedisCmd.getClusterNodes();
        if (nodes != null && nodes.values() != null)
            clients.addAll(nodes.values());

        if (clients != null) {
            for (JedisPool jedisPool : clients) {
                Jedis client = jedisPool.getResource();
                if (client != null) {
                    try {
                        keysSet.addAll(client.keys(searchKey));
                    } catch (Exception e) {
                    } finally {
                        RedisClusterUtil.returnResource(client);
                    }
                }
            }
        }
        return new ArrayList<String>(keysSet);
    }

    @Override
    public long setKeyExpire(String key, int expireTime) throws CusException {
        long time = 0;
        try {
            if (expireTime > 0)
                jedisCmd.expire(key.getBytes(), expireTime);

        } catch (Exception e) {
            throw new CusException(ErrorCode.RedisCacheError,
                    String.format("设置key过期时间 失败,%s ", ExceptionUtil.getCauseMessage(e)));
        }
        return time;
    }

    @Override
    public void subScribe(JedisPubSub jedispubSub, String channel) throws CusException {
        try {
            jedisCmd.subscribe(jedispubSub, channel);
        } catch (Exception e) {
            throw new CusException(ExceptionUtil.getCauseMessage(StrConUtil.conectStr("订阅频道[", channel, "]发生错误:"), e));
        }
    }

    @Override
    public void pubScribe(String channel, String message) throws CusException {
        try {
            jedisCmd.publish(channel, message);
        } catch (Exception e) {
            throw new CusException(
                    ExceptionUtil.getCauseMessage(StrConUtil.conectStr("在频道[", channel, "]发布消息发生错误:"), e));
        }

    }

    @Override
    public Map<String, HostMemInfo> getCacheMemoryCon() throws CusException {
        Map<String, HostMemInfo> mcs = null;
        Map<String, JedisPool> nodes = jedisCmd.getClusterNodes();
        if (nodes == null || nodes.values() == null || nodes.values().isEmpty())// 数据为空返回
            return mcs;

        mcs = new HashMap<String, HostMemInfo>(nodes.values().size());
        Iterator<Entry<String, JedisPool>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, JedisPool> e = it.next();
            Jedis client = e.getValue().getResource();
            if (client != null) {
                mcs.put(e.getKey(), RedisService.getMemeryInfo(client, true));
            }
        }
        return mcs;
    }

    @Override
    public ILock getLock(String lockey, int timeout) {
        return new RedisDistribtLock(jedisCmd, lockey, timeout);
    }

    public String getClientAddr(String key) {
        String addr = null;
        JedisClusterPipeline jcp = JedisClusterPipeline.pipelined(jedisCmd);
        Client client = jcp.getClient(key);
        if (client != null)
            addr = String.format("%s:%d", client.getHost(), client.getPort());
        return addr;
    }

    @Override
    public long hincrBy(DataOptType type, byte[] keys, byte[] feilds, long value) throws CusException {
        long reslut = 0;
        try {
            reslut = jedisCmd.hincrBy(keys, feilds, value);
        } catch (Exception e) {
            if (e instanceof JedisException) {
                JedisException je = (JedisException) e;
                if (OOMSTR.equals(je.getMessage())) {
                    throw new CusException(ErrorCode.REDIS_OOM, "数据达到redis上限，丢包处理，写入数据失败");
                } else
                    throw new CusException(ErrorCode.RedisCacheError, ExceptionUtil.getCauseMessage("写入数据至缓存错误", e));
            }

        } finally {
            // lock.unlock();
        }
        return reslut;
    }

    @Override
    public int batchPubScribe(Collection<IPubData> messages) throws CusException {

        return 0;
    }

    @Override
    public int batchDeleteDatas(byte[] rowKey, Collection<ICacheSecnKey> secKeys) throws CusException {

        return 0;
    }

    @Override
    public List<byte[]> batchGetDatas(byte[] keyBytes, byte[]... secKeys) throws CusException {

        return null;
    }

    @Override
    public int batchWriteDatas_stacks(Collection<ICacheKeyData> listdatas, int expireTime) throws CusException {

        return 0;
    }

    @Override
    public List<byte[]> batchGetDatas_Stacks(byte[] keyBytes, int timeout) throws CusException {

        return null;
    }

    @Override
    public Map<byte[], byte[]> batchGetDatas_All(byte[] keyBytes) throws CusException {

        return null;
    }

    @Override
    public void deleteDataByKey(byte[] rowKey) throws CusException {


    }

    @Override
    public long getKeyLen(String key) throws CusException {

        return 0;
    }

    @Override
    public long getKeyLen(byte[] keyBytes) throws CusException {

        return 0;
    }

    @Override
    public long getKeyHLen(String key) throws CusException {

        return 0;
    }

    @Override
    public long getKeyHLen(byte[] keyBytes) throws CusException {

        return 0;
    }

    @Override
    public List<Entry<byte[], byte[]>> getAllDatas_Scan(byte[] keyBytes, MyCusor myCusor, int count) throws CusException {

        return null;
    }

    @Override
    public long getKeySetLen(byte[] keyBytes) throws CusException {

        return 0;
    }

    @Override
    public long getKeySetLen(String key) throws CusException {

        return 0;
    }

    @Override
    public long batchDeleteDatas(byte[] keyBytes, long start, long end) throws CusException {

        return 0;
    }

    @Override
    public Set<redis.clients.jedis.Tuple> getAllDatas_Zset(byte[] keyBytes, long start, long end) throws CusException {

        return null;
    }

    @Override
    public long getKeyHashLen(byte[] key) throws CusException {

        return 0;
    }

    @Override
    public long getKeyHashLen(String key) throws CusException {

        return 0;
    }

    @Override
    public int batchWriteDatas_Zset(byte[] fileKey, byte[] fileIndexKey, Collection<ICacheKeyData> listdatas,
                                    int expireTime) throws CusException {

        return 0;
    }

    @Override
    public long getKeyTTL(byte[] key) throws CusException {

        return 0;
    }

    @Override
    public long setKeyExpire(byte[] keys, int expireTime) throws CusException {

        return 0;
    }

    @Override
    public boolean containKey(byte[] rowkeys) throws CusException {

        return false;
    }

}
