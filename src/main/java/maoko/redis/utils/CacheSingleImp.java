package maoko.redis.utils;


import maoko.common.ExceptionUtil;
import maoko.common.StrConUtil;
import maoko.common.StringUtil;
import maoko.redis.utils.core.RedisDistribtLock;
import maoko.redis.utils.core.RedisSentineUtil;
import maoko.redis.utils.core.RedisService;
import maoko.redis.utils.ifs.ICache;
import maoko.redis.utils.ifs.ICacheSecnKey;
import maoko.redis.utils.ifs.ILock;
import maoko.redis.utils.ifs.IPubData;
import maoko.redis.utils.entity.cluster.SvrNodeInfo;
import maoko.redis.utils.entity.cluster.StateType;
import maoko.redis.utils.entity.cluster.SvrNodeState;
import maoko.redis.utils.enm.DataOptType;
import maoko.redis.utils.except.ErrorCode;
import maoko.redis.utils.entity.MyCusor;
import maoko.redis.utils.ifs.ICacheKeyData;
import maoko.redis.utils.entity.HostMemInfo;
import maoko.redis.utils.except.CusException;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;
import java.util.Map.Entry;

/**
 * 单节点 包含哨兵集群模式
 * 
 * @author fanpei
 *
 * @date 2017年9月5日下午7:25:08
 */
public class CacheSingleImp extends CacheSingnalBase implements ICache {

	private static SvrNodeState States;
	private boolean isInited;

	public CacheSingleImp() {
		States = new SvrNodeState();
	}

	@Override
	public void start() throws CusException {
		try {
			RedisSentineUtil.initRedisClient(CacheFactory.redisConfig);
			RedisSentineUtil.returnResource(RedisSentineUtil.getJedisCli());
		} catch (Exception e) {
			throw new CusException(ErrorCode.REDIS_NOT_INIT, e);
		}
		isInited = true;
	}

	@Override
	public void stop() {
		if (!isInited)
			return;
		RedisSentineUtil.closeClient();
	}

	@Override
	public boolean containKey(String key) throws CusException {
		boolean exists = false;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			exists = jedisCmd.exists(key);
		} catch (Exception e) {
			throw new CusException(String.format("检查一级key %s 是否存在于redis中发生错误", key), e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return exists;
	}

	@Override
	public boolean containKey(byte[] rowkeys) throws CusException {
		boolean exists = false;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			exists = jedisCmd.exists(rowkeys);
		} catch (Exception e) {
			throw new CusException("检查一级key byte[] 是否存在于redis中发生错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return exists;
	}

	@Override
	public boolean containKey(byte[] rowkey, byte[] secKey) throws Exception {
		boolean exists = false;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			exists = jedisCmd.hexists(rowkey, secKey);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}

		return exists;
	}

	@Override
	public void deleteDataByKey(String rowkey) throws CusException {

		// RedisClusterLock lock = new RedisClusterLock(jedis, rowkey);
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			// lock.lock();
			jedisCmd.del(rowkey);
		} catch (Exception e) {
			String err = ExceptionUtil.getCauseMessage(String.format("从缓存中删除数据  %s 错误 ", rowkey), e);
			throw new CusException(ErrorCode.RedisCacheError, err);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
	}

	@Override
	public void deleteDataByKey(byte[] rowkey) throws CusException {

		// RedisClusterLock lock = new RedisClusterLock(jedis, rowkey);
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			// lock.lock();
			jedisCmd.del(rowkey);
		} catch (Exception e) {
			throw new CusException("从缓存中删除数据 错误 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
	}

	@Override
	public void deleteDataByKey(byte[] rowKey, byte[] secKey) throws CusException {
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			jedisCmd.hdel(rowKey, secKey);
		} catch (Exception e) {
			String err = ExceptionUtil.getCauseMessage(String.format("从缓存中删除二级数据  %s 错误", secKey), e);
			throw new CusException(ErrorCode.RedisCacheError, err);

		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
	}

	/**
	 * 批处理删除数据
	 * 
	 * @param keyBytes
	 * @param secKeys
	 */
	public int batchDeleteDatas(byte[] keyBytes, Collection<ICacheSecnKey> secKeys) throws CusException {
		int num = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Pipeline jcp = jedisCmd.pipelined();
			for (ICacheSecnKey key : secKeys) {
				jcp.hdel(keyBytes, key.getSecondKeyBytes());
			}
			List<Object> resluts = jcp.syncAndReturnAll();
			if (resluts != null) {
				num = resluts.size();
				num = num < 0 ? 0 : num;
			}
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return num;
	}

	public int batchDeleteDatas(Collection<String> secKeys) throws CusException {
		int num = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Pipeline jcp = jedisCmd.pipelined();
			for (String key : secKeys) {
				jcp.del(key);
			}
			List<Object> resluts = jcp.syncAndReturnAll();
			if (resluts != null) {
				num = resluts.size();
				num = num < 0 ? 0 : num;
			}
		} catch (Exception e) {
			throw new CusException(ErrorCode.RedisCacheError, ExceptionUtil.getCauseMessage("从缓存中批量删除keys数据错误", e));
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return num;
	}

	@Override
	public long batchDeleteDatas(byte[] keyBytes, long start, long end) throws CusException {
		long num = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			num = jedisCmd.zremrangeByScore(keyBytes, start, end);
		} catch (Exception e) {
			throw new CusException("从缓存中批量删除keys数据错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return num;
	}

	/*
	 * 批处理插入数据
	 * 
	 * @see CloudPanSys.Interface.Cache.ICache#writeData(java.lang.String,
	 * java.util.List, int)
	 */
	@Override
	public int batchWriteDatas(Collection<ICacheKeyData> listdatas, int expireTime) throws CusException {
		Jedis jedisCmd = null;
		byte[] keys = null;
		int num = 0;
		try {
			if (listdatas == null || listdatas.isEmpty())
				throw new NullPointerException("待添加的数据为空");

			jedisCmd = RedisSentineUtil.getJedisCli();
			Pipeline jcp = jedisCmd.pipelined();

			for (ICacheKeyData data : listdatas) {
				byte[] firstKeys = data.getFirstKeys();
				if (keys == null)
					keys = firstKeys;
				byte[] datas = data.getValueDatas();
				jcp.hset(firstKeys, data.getSencondKeys(), datas);
			}
			List<Object> resluts = jcp.syncAndReturnAll();
			num = resluts != null ? resluts.size() : 0;
			setKeyExpire(jedisCmd, keys, expireTime);
		} catch (JedisException je) {
			if (RedisConf.OOMSTR.equals(je.getMessage())) {
				throw new CusException(ErrorCode.REDIS_OOM, RedisConf.OVERFLOW);
			}
		} catch (Exception e) {
			throw new CusException("批处理写入数据 至缓存错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}

		return num;
	}

	@Override
	public int batchWriteDatas_stacks(Collection<ICacheKeyData> listdatas, int expireTime) throws CusException {
		Jedis jedisCmd = null;
		int num = 0;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Pipeline jcp = jedisCmd.pipelined();
			Set<String> keys = new HashSet<>();
			for (ICacheKeyData data : listdatas) {
				byte[] firstKeys = data.getFirstKeys();
				byte[] datas = data.getValueDatas();
				jcp.lpush(firstKeys, datas);
				if (!keys.contains(data.getFirstKeyStr())) {
					keys.add(data.getFirstKeyStr());
				}
			}
			List<Object> resluts = jcp.syncAndReturnAll();
			num = resluts != null ? resluts.size() : 0;
			if (expireTime > 0) {
				for (String key : keys) {
					jedisCmd.expire(key, expireTime);
				}
				keys = null;
			}
		} catch (JedisException je) {
			if (RedisConf.OOMSTR.equals(je.getMessage())) {
				throw new CusException(ErrorCode.REDIS_OOM, RedisConf.OVERFLOW);
			}
		} catch (Exception e) {
			throw new CusException("批处理写入数据 至缓存错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return num;
	}

	@Override
	public int batchWriteDatas_Zset(byte[] fileKey, byte[] fileIndexKey, Collection<ICacheKeyData> listdatas,
			int expireTime) throws CusException {
		Jedis jedisCmd = null;
		int num = 0;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Pipeline jcp = jedisCmd.pipelined();
			for (ICacheKeyData data : listdatas) {
				jcp.hset(fileKey, data.getSencondKeys(), data.getValueDatas());
				jcp.zadd(fileIndexKey, data.getScore(), data.getNextIndexBytes());
			}

			List<Object> resluts = jcp.syncAndReturnAll();
			num = resluts != null ? resluts.size() : 0;
			if (expireTime > 0) {
				jedisCmd.expire(fileIndexKey, expireTime);
				jedisCmd.expire(fileKey, expireTime);
			}
		} catch (JedisException je) {
			if (RedisConf.OOMSTR.equals(je.getMessage())) {
				throw new CusException(ErrorCode.REDIS_OOM, RedisConf.OVERFLOW);
			}
		} catch (Exception e) {
			throw new CusException("批处理写入数据 至缓存错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return num;
	}

	@Override
	public void writeData(String key, byte[] datas, int expireTime) throws CusException {
		Jedis jedisCmd = null;
		byte[] keys = key.getBytes();
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			if (datas == null)
				datas = "".getBytes();
			if (!"OK".equals(jedisCmd.set(keys, datas)))
				throw new CusException(ErrorCode.RedisCacheError, "添加操作返回为False");
			setKeyExpire(jedisCmd, keys, expireTime);
		} catch (JedisException e) {
			if (RedisConf.OOMSTR.equals(e.getMessage())) {
				throw new CusException(ErrorCode.REDIS_OOM, RedisConf.OVERFLOW);
			} else
				throw new CusException(ErrorCode.RedisCacheError, StringUtil.getMsgStr("写入数据  {} 至缓存错误 {}", key, e));
		} catch (Exception e) {
			throw new CusException(ErrorCode.RedisCacheError, StringUtil.getMsgStr("写入数据  %s 至缓存错误", key, e));
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
	}

	@Override
	public void writeData(byte[] keys, byte[] secKeys, byte[] datas, int expireTime) throws CusException {

		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Map<byte[], byte[]> map = new HashMap<>();
			byte[] mydatas = datas == null ? StringUtil.getUtf8Bytes("") : datas;
			map.put(secKeys, mydatas);
			if (!"OK".equals(jedisCmd.hmset(keys, map)))
				throw new CusException(ErrorCode.RedisCacheError, "添加操作返回为False");
			setKeyExpire(jedisCmd, keys, expireTime);

		} catch (JedisException e) {
			if (RedisConf.OOMSTR.equals(e.getMessage())) {
				throw new CusException(ErrorCode.REDIS_OOM, RedisConf.OVERFLOW);
			} else
				throw new CusException(ErrorCode.RedisCacheError, e.getMessage());
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
	}

	@Override
	public long setKeyExpire(String key, int expireTime) throws CusException {
		long reslut = -1;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			reslut = setKeyExpire(jedisCmd, key, expireTime);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return reslut;
	}

	@Override
	public long setKeyExpire(byte[] keys, int expireTime) throws CusException {
		long reslut = -1;
		if (expireTime <= 0)
			return reslut;

		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			reslut = jedisCmd.expire(keys, expireTime);

		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return reslut;
	}

	@Override
	public List<byte[]> batchGetDatas(byte[] keyBytes, byte[]... secKeys) throws CusException {
		List<byte[]> resluts = null;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			resluts = jedisCmd.hmget(keyBytes, secKeys);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return resluts;
	}

	@Override
	public Map<ICacheSecnKey, Response<byte[]>> batchGetDatas(byte[] keyBytes, Collection<ICacheSecnKey> secKeys)
			throws CusException {
		Map<ICacheSecnKey, Response<byte[]>> resluts = null;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Pipeline jcp = jedisCmd.pipelined();
			resluts = new HashMap<>(secKeys.size());
			for (ICacheSecnKey feild : secKeys) {
				resluts.put(feild, jcp.hget(keyBytes, feild.getSecondKeyBytes()));
			}
			jcp.sync();
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return resluts;
	}

	@Override
	public List<byte[]> batchGetDatas_Stacks(byte[] keyBytes, int timeout) throws CusException {
		Jedis jedisCmd = null;
		List<byte[]> resluts;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			resluts = jedisCmd.brpop(timeout, keyBytes);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return resluts;
	}

	@Override
	public Map<byte[], byte[]> batchGetDatas_All(byte[] keyBytes) throws CusException {
		Jedis jedisCmd = null;
		Map<byte[], byte[]> resluts;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			resluts = jedisCmd.hgetAll(keyBytes);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return resluts;
	}

	@Override
	public byte[] getData(byte[] key, byte[] secKey) throws CusException {
		Jedis jedisCmd = null;
		byte[] returnObj = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			List<byte[]> objs = jedisCmd.hmget(key, secKey);
			if (objs != null && !objs.isEmpty()) {
				for (byte[] bs : objs) {
					if (bs != null) {
						returnObj = bs;
					}
				}
			}
		} catch (Exception e) {
			throw new CusException("从缓存获对象失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return returnObj;
	}

	@Override
	public byte[] getData(String key) throws CusException {
		byte[] returnObj = null;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			returnObj = jedisCmd.get(key.getBytes());
		} catch (Exception e) {
			throw new CusException(ErrorCode.RedisCacheError,
					ExceptionUtil.getCauseMessage(String.format("从缓存获对象  %s 失败 ", key), e));
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return returnObj;
	}

	@Override
	public long getKeyLen(String key) throws CusException {
		long len = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			len = jedisCmd.llen(key);
		} catch (Exception e) {
			throw new CusException("从缓存获对象长度失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return len;
	}

	@Override
	public long getKeyLen(byte[] keyBytes) throws CusException {
		long len = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			len = jedisCmd.llen(keyBytes);
		} catch (Exception e) {
			throw new CusException("从缓存获对象长度失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return len;
	}

	@Override
	public long getKeyHLen(String key) throws CusException {
		long len = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			len = jedisCmd.hlen(key);
		} catch (Exception e) {
			throw new CusException("从缓存获对象长度失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return len;
	}

	@Override
	public long getKeyHLen(byte[] keyBytes) throws CusException {
		long len = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			len = jedisCmd.hlen(keyBytes);
		} catch (Exception e) {
			throw new CusException("从缓存获对象长度失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return len;
	}

	@Override
	public long getKeySetLen(byte[] keyBytes) throws CusException {
		long len = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			len = jedisCmd.zcard(keyBytes);
		} catch (Exception e) {
			throw new CusException("从缓存获对象长度失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return len;
	}

	@Override
	public long getKeySetLen(String key) throws CusException {
		long len = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			len = jedisCmd.zcard(key);
		} catch (Exception e) {
			throw new CusException("从缓存获对象长度失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return len;
	}

	@Override
	public long getKeyHashLen(String key) throws CusException {
		long len = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			len = jedisCmd.hlen(key);
		} catch (Exception e) {
			throw new CusException("从缓存获对象长度失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return len;
	}

	@Override
	public long getKeyHashLen(byte[] keyBytes) throws CusException {
		long len = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			len = jedisCmd.hlen(keyBytes);
		} catch (Exception e) {
			throw new CusException("从缓存获对象长度失败 ", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return len;
	}

	@Override
	public List<Entry<byte[], byte[]>> getAllDatas_Scan(byte[] keyBytes, MyCusor myCusor, int count) throws CusException {
		List<Entry<byte[], byte[]>> results = null;
		ScanResult<Entry<byte[], byte[]>> gets = null;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			ScanParams params = new ScanParams();
			if (count > 0) {
				params.count(count);
				gets = jedisCmd.hscan(keyBytes, ScanParams.SCAN_POINTER_START_BINARY, params);
			} else {
				gets = jedisCmd.hscan(keyBytes, ScanParams.SCAN_POINTER_START_BINARY);
			}
			if (gets != null) {
				results = gets.getResult();
				myCusor.setValue(gets.getCursorAsBytes());
			}

		} catch (Exception e) {
			throw new CusException("批量scan所有数据错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return results;
	}

	@Override
	public Set<Tuple> getAllDatas_Zset(byte[] keyBytes, long start, long end) throws CusException {
		Set<Tuple> resluts = null;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			resluts = jedisCmd.zrangeByScoreWithScores(keyBytes, start, end);
		} catch (Exception e) {
			throw new CusException("批量zrang有数据错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return resluts;
	}

	@Override
	public List<String> getAllSecondKeyByKey(String rowKey) throws CusException {
		List<String> keys = null;
		Set<String> secStrs = null;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			secStrs = jedisCmd.hkeys(rowKey);
			if (secStrs != null && !secStrs.isEmpty())
				keys = new ArrayList<String>(secStrs);

		} catch (Exception e) {
			throw new CusException(ErrorCode.RedisCacheError,
					ExceptionUtil.getCauseMessage(String.format("获取文件  %s 已有缓存区段失败", rowKey), e));
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}

		return keys;
	}

	@Override
	public SvrNodeState getSvrStates() {
		boolean clusterNormal = false;
		Jedis jedisCmd = null;
		List<SvrNodeInfo> svrNodeInfos = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Client client = jedisCmd.getClient();
			client.info();
			String info = client.getBulkReply();
			svrNodeInfos = new ArrayList<SvrNodeInfo>();
			SvrNodeInfo master = new SvrNodeInfo(client.getHost() + ":" + client.getPort(), StateType.Normal);
			master.setName("master");
			svrNodeInfos.add(master);// master

			int index = info.lastIndexOf("# Replication");
			String replia = info.substring(index);
			info = null;
			String[] lines = replia.split("\r\n");
			for (int i = 3; i < lines.length; i++) {
				String detail = lines[i];
				if (detail.startsWith("slave")) {
					svrNodeInfos.add(getNodeInfo(detail));
				} else
					break;
			}
			clusterNormal = true;

		} catch (Exception e) {
			clusterNormal = false;
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
			if (clusterNormal)
				States.setNodes(svrNodeInfos);
		}
		States.setState(clusterNormal ? StateType.Normal : StateType.Trouble);
		return States;
	}

	@Override
	public List<String> getClientsAllFilterKey(String pattern) throws CusException {
		String searchKey = String.format("%s*", pattern);
		ArrayList<String> keysSet = null;
		Jedis jedisCmd = RedisSentineUtil.getJedisCli();
		try {
			Set<String> keys = jedisCmd.keys(searchKey);
			if (keys != null && !keys.isEmpty())
				keysSet = new ArrayList<String>(keys);
		} catch (Exception e) {
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return keysSet;
	}

	@Override
	public void subScribe(JedisPubSub jedispubSub, String channel) throws CusException {
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			jedisCmd.subscribe(jedispubSub, channel);
		} catch (Exception e) {
			throw new CusException(StrConUtil.conectStr("订阅频道[", channel, "]发生错误:"), e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
	}

	@Override
	public void pubScribe(String channel, String message) throws CusException {
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			jedisCmd.publish(channel, message);
		} catch (Exception e) {
			throw new CusException(StrConUtil.conectStr("在频道[", channel, "]发布消息发生错误:"), e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
	}

	@Override
	public int batchPubScribe(Collection<IPubData> messages) throws CusException {
		int num = 0;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Pipeline jcp = jedisCmd.pipelined();
			for (IPubData iPubData : messages) {
				jcp.publish(iPubData.getChanal(), iPubData.getMessage());
			}
			List<Object> resluts = jcp.syncAndReturnAll();
			if (resluts != null) {
				num = resluts.size();
				num = num < 0 ? 0 : num;
			}
		} catch (Exception e) {
			throw new CusException("批处理发布消息发生异常", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return num;
	}

	@Override
	public Map<String, HostMemInfo> getCacheMemoryCon() throws CusException {
		Map<String, HostMemInfo> mcs = null;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			Client cli = jedisCmd.getClient();
			String addr = String.format("%s:%d", cli.getHost(), cli.getPort());
			mcs = new HashMap<>(1);
			mcs.put(addr, RedisService.getMemeryInfo(jedisCmd, false));
		} catch (Exception e) {
			throw new CusException("获取缓存内存错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return mcs;
	}

	@Override
	public ILock getLock(String lockey, int timeout) {
		RedisDistribtLock lock = null;
		Jedis jedisCmd = null;
		boolean needReturn = false;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			lock = new RedisDistribtLock(jedisCmd, lockey, timeout);
		} catch (Exception e) {
			needReturn = true;
		} finally {
			if (needReturn)
				RedisSentineUtil.returnResource(jedisCmd);
		}
		return lock;
	}

	@Override
	public long hincrBy(DataOptType type, byte[] keys, byte[] feilds, long value) throws CusException {
		long reslut = 0;
		Jedis jedisCmd = RedisSentineUtil.getJedisCli();
		long myValue = value;
		try {
			if (DataOptType.Delete == type) {
				myValue = 0 - value;
				if (!jedisCmd.hexists(keys, feilds))
					return 0;
			}
			reslut = jedisCmd.hincrBy(keys, feilds, myValue);
		} catch (JedisException je) {
			je.printStackTrace();
			if (RedisConf.OOMSTR.equals(je.getMessage())) {
				throw new CusException(ErrorCode.REDIS_OOM, RedisConf.OVERFLOW);
			} else
				throw new CusException("原子增加或减少数据错误", je);
		} catch (Exception e) {

			throw new CusException("原子增加或减少数据错误", e);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return reslut;
	}

	public long incrBy(String key, long value) throws CusException {
		long reslut = 0;
		Jedis jedisCmd = RedisSentineUtil.getJedisCli();
		try {
			reslut = jedisCmd.incrBy(key, value);
		} catch (Exception e) {
			if (e instanceof JedisException) {
				JedisException je = (JedisException) e;
				if (RedisConf.OOMSTR.equals(je.getMessage())) {
					throw new CusException(ErrorCode.REDIS_OOM, RedisConf.OVERFLOW);
				} else
					throw new CusException(ErrorCode.RedisCacheError,
							ExceptionUtil.getCauseMessage(String.format("写入数据  %s 至缓存错误", key), e));
			}

		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return reslut;
	}

	@Override
	public long getKeyTTL(byte[] key) throws CusException {
		long ttl = -2;
		Jedis jedisCmd = null;
		try {
			jedisCmd = RedisSentineUtil.getJedisCli();
			ttl = jedisCmd.ttl(key);
		} finally {
			RedisSentineUtil.returnResource(jedisCmd);
		}
		return ttl;
	}

}
