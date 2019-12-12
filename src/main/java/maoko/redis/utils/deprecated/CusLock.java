package maoko.redis.utils.deprecated;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

/**
 * Jedis实现分布式锁
 * 
 * @author fanpei
 *
 */
@Deprecated
public class CusLock {
	private final JedisPool jedisPool;

	public CusLock(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	/**
	 * 获取分布式锁
	 * 
	 * @param lockName
	 *            竞争获取锁key
	 * @param acquireTimeoutInMS
	 *            获取锁超时时间
	 * @param lockTimeoutInMS
	 *            锁的超时时间
	 * @return 获取锁标识
	 */
	public String acquireLockWithTimeout(String lockName, long acquireTimeoutInMS, long lockTimeoutInMS) {
		Jedis jedis = null;
		boolean broken = false;
		String retIdentifier = null;
		try {
			jedis = jedisPool.getResource();
			String identifier = Long.toString(System.currentTimeMillis());
			String lockKey = "lock:" + lockName;
			int lockExpire = (int) (lockTimeoutInMS / 1000);

			long end = System.currentTimeMillis() + acquireTimeoutInMS;
			while (System.currentTimeMillis() < end) {
				if (jedis.setnx(lockKey, identifier) == 1) {
					jedis.expire(lockKey, lockExpire);
					retIdentifier = identifier;
				}
				if (jedis.ttl(lockKey) == -1) {
					jedis.expire(lockKey, lockExpire);
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		} catch (JedisException je) {
			if (jedis != null) {
				broken = true;
				jedisPool.returnBrokenResource(jedis);
			}
		} finally {
			if (jedis != null && !broken) {
				jedisPool.returnResource(jedis);
			}
		}
		return retIdentifier;
	}

	/**
	 * 释放锁
	 * 
	 * @param lockName
	 *            竞争获取锁key
	 * @param identifier
	 *            释放锁标识
	 * @return
	 */
	public boolean releaseLock(String lockName, String identifier) {
		Jedis conn = null;
		boolean broken = false;
		String lockKey = "lock:" + lockName;
		boolean retFlag = false;
		try {
			conn = jedisPool.getResource();
			while (true) {
				conn.watch(lockKey);
				if (identifier.equals(conn.get(lockKey))) {
					Transaction trans = conn.multi();
					trans.del(lockKey);
					List<Object> results = trans.exec();
					if (results == null) {
						continue;
					}
					retFlag = true;
				}
				conn.unwatch();
				break;
			}

		} catch (JedisException je) {
			if (conn != null) {
				broken = true;
				jedisPool.returnBrokenResource(conn);
			}
		} finally {
			if (conn != null && !broken) {
				jedisPool.returnResource(conn);
			}
		}
		return retFlag;
	}

	public Jedis getCurrentJedis() {
		return null;
	}
}