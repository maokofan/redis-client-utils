package maoko.redis.utils.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import maoko.common.model.net.CusHostAndPort;
import maoko.redis.utils.CacheFactory;
import maoko.redis.utils.RedisConf;
import maoko.redis.utils.entity.cluster.SvrNodeInfo;
import maoko.redis.utils.entity.cluster.StateType;
import maoko.redis.utils.except.CusException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

/**
 * 哨兵助手
 */
public class RedisSentineUtil {

	private static Pool<Jedis> pool = null;

	public static List<SvrNodeInfo> svrNodeInfos;

	/**
	 * 初始化
	 *
	 * @param redisConfig
	 */
	public static void initRedisClient(RedisPoolConfig redisConfig) {
		// 创建jedis池配置实例
		JedisPoolConfig config = RedisService.getconf(redisConfig.isCloud());
		List<CusHostAndPort> hosts = redisConfig.getHosts();
		svrNodeInfos = new ArrayList<SvrNodeInfo>(hosts.size());
		Set<String> sentinels = new HashSet<String>(hosts.size());
		for (CusHostAndPort host : hosts) {
			SvrNodeInfo node = new SvrNodeInfo(host.getAddrStr(), StateType.Normal);
			svrNodeInfos.add(node);
			sentinels.add(host.getAddrStr());
		}
		if (redisConfig.isSentinel()) {
			pool = new JedisSentinelPool(RedisConf.clusterName, sentinels, config, RedisConf.connectionTimeout);
		} else {
			// 根据配置实例化jedis池
			CusHostAndPort cha = redisConfig.getHosts().get(0);
			pool = new JedisPool(config, cha.getIP(), cha.getPort());
		}
	}

	/**
	 * 获得jedis对象
	 * 
	 * @throws Exception
	 */
	public static Jedis getJedisCli() throws CusException {
		Jedis jd = null;
		boolean error = false;
		try {
			if (pool != null)
				jd = pool.getResource();
		} catch (Exception e) {
			error = true;
			throw new CusException("缓存池中获取jedis客户端失败", e);
		} finally {
			if (error) {
				returnResource(jd);
				jd = null;
			}

		}
		return jd;
	}

	/** 归还jedis对象 */
	public static void returnResource(Jedis jedis) {
		try {
			if (null != jedis && (CacheFactory.redisConfig.isSentinel() || !CacheFactory.redisConfig.isCloud())) {
				jedis.close();
			}
		} catch (Exception e) {
		}
		jedis = null;
	}

	/**
	 * 关闭池子
	 */
	public static void closeClient() {
		if (pool != null)
			pool.close();
	}

}
