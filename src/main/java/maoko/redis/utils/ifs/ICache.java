package maoko.redis.utils.ifs;

import maoko.redis.utils.entity.cluster.SvrNodeState;
import maoko.redis.utils.enm.DataOptType;
import maoko.redis.utils.entity.MyCusor;
import maoko.redis.utils.entity.HostMemInfo;
import maoko.redis.utils.except.CusException;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 缓存操作接口
 *
 * @author fanpei
 */
public interface ICache {

    /**
     * 启动redi缓存服务
     *
     * @return
     */
    void start() throws CusException;

    /**
     * 停止缓存任务
     *
     * @return
     */
    void stop();

    /**
     * 获取key过期时间
     *
     * @param key
     * @return
     */
    long getKeyTTL(byte[] key) throws CusException;

    /**
     * 是否包含此key
     *
     * @param rowkey
     * @return true:包含 false:不包含
     */
    boolean containKey(String rowkey) throws CusException;

    boolean containKey(byte[] rowkeys) throws CusException;

    /**
     * 是否包含此二级secKey
     *
     * @param rowkey
     * @param secKey 二级key
     * @return true:包含 false:不包含
     */
    boolean containKey(byte[] rowkey, byte[] secKey) throws Exception;

    /**
     * 写入数据
     *
     * @param key        一级key
     * @param datas      二进制数组
     * @param expireTime 过期时间 单位：秒 <0不限时间
     * @throws CusException
     */
    void writeData(String key, byte[] datas, int expireTime) throws CusException;

    /**
     * 写入数据的同时设置数据的过期时间
     *
     * @param keys       一级key
     * @param secKeys    二级key
     * @param datas      二进制数组
     * @param expireTime 过期时间 单位：秒 <0不限时间
     * @throws CusException
     */
    void writeData(byte[] keys, byte[] secKeys, byte[] datas, int expireTime) throws CusException;

    /**
     * 批处理添加数据
     *
     * @param listdatas
     * @param expireTime 过期时间 单位：秒 <0不限时间
     * @return 成功插入数据的个数
     * @throws CusException
     */
    int batchWriteDatas(Collection<ICacheKeyData> listdatas, int expireTime) throws CusException;

    /**
     * 堆栈批量写入
     *
     * @param listdatas
     * @param expireTime
     * @return
     * @throws CusException
     */
    int batchWriteDatas_stacks(Collection<ICacheKeyData> listdatas, int expireTime) throws CusException;

    /**
     * 有序集合批量插入
     *
     * @param fileKey
     * @param fileIndexKey
     * @param listdatas
     * @param expireTime   文件信息过期时间
     * @return
     * @throws CusException
     */
    int batchWriteDatas_Zset(byte[] fileKey, byte[] fileIndexKey, Collection<ICacheKeyData> listdatas, int expireTime)
            throws CusException;

    /**
     * 获取数据
     *
     * @param key 一级key
     * @return
     * @throws CusException
     */
    byte[] getData(String key) throws CusException;

    /**
     * 获取数据
     *
     * @param key    一级key
     * @param secKey 二级key
     * @return
     * @throws CusException
     */
    byte[] getData(byte[] key, byte[] secKey) throws CusException;

    /**
     * 批处理获取数据
     *
     * @param keyBytes
     * @param secKeys
     * @return
     * @throws CusException
     */
    Map<ICacheSecnKey, Response<byte[]>> batchGetDatas(byte[] keyBytes, Collection<ICacheSecnKey> secKeys)
            throws CusException;

    /**
     * 批处理获取数据
     *
     * @param keyBytes
     * @param secKeys
     * @return
     * @throws CusException
     */
    List<byte[]> batchGetDatas(byte[] keyBytes, byte[]... secKeys) throws CusException;

    /**
     * 阻塞出栈获取数据 brpop
     *
     * @param keyBytes
     * @param timeout  超时时间 单位 秒
     * @return
     * @throws CusException
     */
    List<byte[]> batchGetDatas_Stacks(byte[] keyBytes, int timeout) throws CusException;

    /**
     * 扫描有序集合
     *
     * @param keyBytes
     * @param start    起始分数
     * @param end      结束分数
     * @return
     * @throws CusException
     */
    Set<Tuple> getAllDatas_Zset(byte[] keyBytes, long start, long end) throws CusException;

    /**
     * 获取指定key所有值 hgetall
     *
     * @param keyBytes
     * @return
     * @throws CusException
     */
    Map<byte[], byte[]> batchGetDatas_All(byte[] keyBytes) throws CusException;

    /**
     * 获取指定key字段数量
     *
     * @param key
     * @return
     * @throws CusException
     */
    long getKeyLen(String key) throws CusException;

    /**
     * 获取指定有序集合key字段数量
     *
     * @param keyBytes
     * @return
     * @throws CusException
     */
    long getKeySetLen(byte[] keyBytes) throws CusException;

    long getKeySetLen(String key) throws CusException;

    /**
     * 获取keyBytes字段数量
     *
     * @param keyBytes
     * @return
     * @throws CusException
     */
    long getKeyLen(byte[] keyBytes) throws CusException;

    /**
     * 获取key哈希字段数量
     *
     * @param key
     * @return
     * @throws CusException
     */
    long getKeyHLen(String key) throws CusException;

    /**
     * 获取keyBytes字段数量
     *
     * @param keyBytes
     * @return
     * @throws CusException
     */
    long getKeyHLen(byte[] keyBytes) throws CusException;

    long getKeyHashLen(byte[] key) throws CusException;

    long getKeyHashLen(String key) throws CusException;

    /**
     * 扫描获取hash表
     *
     * @param keyBytes
     * @param count    扫描获取最大个数 为0时 获取所有
     * @return
     * @throws CusException
     */
    List<Entry<byte[], byte[]>> getAllDatas_Scan(byte[] keyBytes, MyCusor myCusor, int count) throws CusException;

    /**
     * 删除缓存
     *
     * @param rowKey 缓存key
     */
    void deleteDataByKey(String rowKey) throws CusException;

    /**
     * 删除缓存
     *
     * @param rowKey 缓存key
     */
    void deleteDataByKey(byte[] rowKey) throws CusException;

    /**
     * 删除二级key缓存
     *
     * @param rowKey 缓存key
     * @param secKey 二级key
     */
    void deleteDataByKey(byte[] rowKey, byte[] secKey) throws CusException;

    /**
     * 批量删除一级key数据
     *
     * @param keys
     * @return
     * @throws CusException
     */
    int batchDeleteDatas(Collection<String> keys) throws CusException;

    /**
     * 批量删除二级key数据
     *
     * @param rowKey
     * @param secKeys
     * @throws Exception
     */
    int batchDeleteDatas(byte[] rowKey, Collection<ICacheSecnKey> secKeys) throws CusException;

    /**
     * 批量删除zset数据
     *
     * @param keyBytes
     * @param start
     * @param end
     * @return
     * @throws CusException
     */
    long batchDeleteDatas(byte[] keyBytes, long start, long end) throws CusException;

    /**
     * 设置key过期时间
     *
     * @param key
     * @param times 单位秒
     * @throws CusException
     */
    long setKeyExpire(String key, int times) throws CusException;

    long setKeyExpire(byte[] keys, int expireTime) throws CusException;

    /**
     * 通过一级Key获取所有二级Key
     *
     * @param rowKey
     * @return
     * @throws CusException
     */
    List<String> getAllSecondKeyByKey(String rowKey) throws CusException;

    /**
     * 订阅【阻塞】
     *
     * @param jedispubSub 订阅发布器
     * @param channel     频道名称
     */
    void subScribe(JedisPubSub jedispubSub, String channel) throws CusException;

    /**
     * 发布
     *
     * @param channel 频道
     * @param message 消息内容
     */
    void pubScribe(String channel, String message) throws CusException;

    /**
     * 批量发布
     *
     * @param messages
     * @return
     * @throws CusException
     */
    int batchPubScribe(Collection<IPubData> messages) throws CusException;

    /**
     * 获取缓存集群内存状态
     *
     * @return
     */
    Map<String, HostMemInfo> getCacheMemoryCon() throws CusException;

    /**
     * 获取客户端所有key
     *
     * @param pattern 匹配符
     * @return
     */
    List<String> getClientsAllFilterKey(String pattern) throws CusException;

    /**
     * 获取锁
     *
     * @param lockey
     * @param timeout 超时时间 秒
     */
    ILock getLock(String lockey, int timeout);

    /**
     * 为指定的值加上值
     *
     * @param type
     * @param keys
     * @param feilds
     * @param value
     * @return
     * @throws CusException
     */
    long hincrBy(DataOptType type, byte[] keys, byte[] feilds, long value) throws CusException;

    /**
     * 获取集群状态和信息
     *
     * @return
     */
    SvrNodeState getSvrStates();

}

