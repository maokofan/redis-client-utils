package maoko.redis.utils.core;

import maoko.common.model.net.CusHostAndPort;
import maoko.redis.utils.RedisConf;
import maoko.redis.utils.entity.cluster.SvrNodeInfo;
import maoko.redis.utils.entity.RandomArry;
import maoko.redis.utils.entity.cluster.StateType;
import maoko.redis.utils.except.ErrorCode;
import maoko.redis.utils.except.CusException;
import redis.clients.jedis.*;
import redis.clients.util.Slowlog;

import java.util.*;

public class RedisClusterUtil {

	// public static Map<String, JedisPool> nodeMap;
	public static List<SvrNodeInfo> svrNodeInfos;
	public static Map<String, SvrNodeInfo> NodeInfoMaps;
	@SuppressWarnings("unused")
	private static TreeMap<Long, String> slotHostMap;
	private static RandomArry<JedisPool> jedisPools;
	private static JedisCluster jedisCluster = null;
	private static List<CusHostAndPort> hosts;

	public static JedisCluster GetjedisCluster() {
		return jedisCluster;
	}

	/**
	 * 初始化连接池和Redis集群
	 * 
	 * @throws Exception
	 */
	public static void initRedisClient(RedisPoolConfig redisConfig) throws Exception {
		try {
			JedisPoolConfig config = RedisService.getconf(redisConfig.isCloud());
			HashSet<HostAndPort> nodes = new HashSet<HostAndPort>();
			hosts = redisConfig.getHosts();
			NodeInfoMaps = new HashMap<String, SvrNodeInfo>(hosts.size());
			svrNodeInfos = new ArrayList<SvrNodeInfo>(hosts.size());
			for (CusHostAndPort host : hosts) {
				SvrNodeInfo node = new SvrNodeInfo(host.getAddrStr(), StateType.Trouble);
				NodeInfoMaps.put(host.getAddrStr(), node);
				svrNodeInfos.add(node);
				// 添加多个主机和端口到集合中
				nodes.add(new HostAndPort(host.getIP(), host.getPort()));
			}

			jedisCluster = new JedisCluster(nodes, RedisConf.TIMEOUT, 2, config);
			initPipeMap();

		} catch (Exception e) {
			throw new CusException(ErrorCode.REDIS_NOT_INIT, String.format("the redis inited fail:%s", e.getCause()));
		}
	}

	public static List<CusHostAndPort> getNodes() {
		return hosts;
	}

	/**
	 * 初始化管道
	 */
	private static void initPipeMap() {

		Map<String, JedisPool> nodeMap = jedisCluster.getClusterNodes();
		if (nodeMap != null) {
			jedisPools = new RandomArry<JedisPool>(nodeMap.size());
			for (JedisPool jedis : nodeMap.values()) {
				jedisPools.add(jedis);
			}

			slotHostMap = getSlotHostMap();
		}

	}

	/**
	 * 随即获取连接
	 * 
	 * @return
	 */
	private static Jedis getJedis() {
		Jedis jedis = jedisPools.getRadomObj().getResource();
		return jedis;
	}

	private static TreeMap<Long, String> getSlotHostMap() {
		TreeMap<Long, String> tree = new TreeMap<Long, String>();
		Jedis jedis = null;
		try {
			jedis = getJedis();
			List<Object> list = jedis.clusterSlots();
			for (Object object : list) {
				@SuppressWarnings("unchecked")
				List<Object> list1 = (List<Object>) object;
				@SuppressWarnings("unchecked")
				List<Object> master = (List<Object>) list1.get(2);
				String hostAndPort = new String((byte[]) master.get(0)) + ":" + master.get(1);
				tree.put((Long) list1.get(0), hostAndPort);
				tree.put((Long) list1.get(1), hostAndPort);
			}
		} catch (Exception e) {

		} finally {
			returnResource(jedis);
		}
		return tree;
	}

	/**
	 * 获取jedis对象
	 * 
	 * @param key
	 * @return
	 */
	public static Jedis getJedis(String key) {

		Jedis jedis = null;
		/*
		 * int getNum = 0; while (true) { try {
		 * 
		 * // 获取槽号 int slot = JedisClusterCRC16.getSlot(key);
		 * 
		 * // 获取到对应的Jedis对象 Map.Entry<Long, String> entry =
		 * slotHostMap.lowerEntry(Long.valueOf(slot));
		 * 
		 * if (entry == null) return null; jedis =
		 * nodeMap.get(entry.getValue()).getResource(); if (jedis == null) { if
		 * (getNum > 1)// 超过两次不再继续 break;
		 * 
		 * Thread.sleep(200); initPipeMap(); getNum++; continue; } else break; }
		 * catch (Exception e) { log.warn(e);
		 * 
		 * } }
		 */

		return jedis;
	}

	/**
	 * 释放Jedis资源
	 * 
	 * @param jedis
	 */
	public static void returnResource(Jedis jedis) {
		RedisSentineUtil.returnResource(jedis);
	}

	// redis状态信息

	// 获取redis 服务器信息
	@Deprecated
	public static String getRedisInfo() {

		Jedis jedis = null;
		try {
			jedis = getJedis();
			Client client = jedis.getClient();
			client.info();
			String info = client.getBulkReply();
			return info;
		} finally {
			returnResource(jedis);
		}
	}

	// 获取指定redis 服务器信息
	public static String getRedisInfo(Jedis jedis, boolean returnSource) {
		try {
			Client client = jedis.getClient();
			client.info();
			String info = client.getBulkReply();
			return info;
		} finally {
			if (returnSource)
				returnResource(jedis);
		}
	}

	public static String getClusterNodes() {

		Jedis jedis = null;
		try {
			jedis = getJedis();
			Client client = jedis.getClient();
			client.clusterNodes();
			String info = client.getBulkReply();
			return info;
		} finally {
			returnResource(jedis);
		}
	}

	public static String getClusterInfo() {

		Jedis jedis = null;
		try {
			jedis = getJedis();
			Client client = jedis.getClient();
			client.clusterInfo();
			String info = client.getBulkReply();
			return info;
		} finally {
			returnResource(jedis);
		}
	}

	// 获取日志列表
	public static List<Slowlog> getLogs(long entries) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			List<Slowlog> logList = jedis.slowlogGet(entries);
			return logList;
		} finally {
			returnResource(jedis);
		}
	}

	// 获取日志条数
	public static Long getLogsLen() {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			long logLen = jedis.slowlogLen();
			return logLen;
		} finally {
			returnResource(jedis);
		}
	}

	// 清空日志
	public static String logEmpty() {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			return jedis.slowlogReset();
		} finally {
			returnResource(jedis);
		}
	}

	// 获取占用内存大小
	public static Long dbSize() {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			// TODO 配置redis服务信息
			Client client = jedis.getClient();
			client.dbSize();
			return client.getIntegerReply();
		} finally {
			returnResource(jedis);
		}
	}

}
