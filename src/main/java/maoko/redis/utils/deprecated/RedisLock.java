package maoko.redis.utils.deprecated;

import java.util.Random;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

@Deprecated
public class RedisLock {
	// 加锁标志
	public static final String LOCKED = "TRUE";
	public static final long ONE_MILLI_NANOS = 1000000L;
	// 默认超时时间（毫秒）
	public static final long DEFAULT_TIME_OUT = 60 * 1000;
	public static JedisPool pool;
	public static final Random r = new Random();
	// 锁的超时时间（秒），过期删除
	public static final int EXPIRE = 60 * 5;// 锁过期5分钟

	static {
		pool = null;
	}
	private Jedis jedis;

	public Pipeline getPipelined() {
		if (jedis != null) {
			return jedis.pipelined();
		}
		return null;
	}

	public Jedis getJedis() {

		return jedis;

	}

	private String key;
	// 锁状态标志
	private boolean islocked = false;

	public RedisLock() {
		this.key = Long.toString(System.currentTimeMillis());

		this.jedis = pool.getResource();
	}

	private boolean lock(long timeout) {
		long nano = System.nanoTime();
		timeout *= ONE_MILLI_NANOS;
		try {
			while ((System.nanoTime() - nano) < timeout) {
				if (jedis.setnx(key, LOCKED) == 1) {
					jedis.expire(key, EXPIRE);
					islocked = true;
					return islocked;
				}
				// 短暂休眠，nano避免出现活锁
				Thread.sleep(3, r.nextInt(500));
			}
		} catch (Exception e) {
		}
		return false;
	}

	public boolean lock() {
		return lock(DEFAULT_TIME_OUT);
	}

	// 无论是否加锁成功，必须调用
	public void unlock() {
		try {
			if (islocked)
				jedis.del(key);
		} finally {
			if (jedis != null)
				jedis.close();
			// RedisUtil.returnResource(jedis);
		}
	}
}
