package maoko.redis.utils.core;

import maoko.common.StringUtil;
import maoko.redis.utils.RedisConf;
import maoko.redis.utils.ifs.ILock;
import maoko.redis.utils.except.ErrorCode;
import maoko.redis.utils.except.CusException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.exceptions.JedisException;

import java.util.concurrent.ThreadLocalRandom;

public class RedisDistribtLock implements ILock {

	// 加锁标志
	private static final String LOCKED = "TRUE";

	// 纳秒
	private static final long ONE_MILLI_NANOS = 1;

	// 默认超时时间（秒）
	private static final int DEFAULT_TIME_OUT = 60;

	// 锁的超时时间（秒），过期删除
	private static final int EXPIRE = 30 * 1;// 锁过期60秒
	private static final String CUSLOCK_FLAG = "RL";
	private static ThreadLocalRandom random;

	private String key;
	// 锁状态标志
	private boolean islocked;
	private JedisCommands jedisCommond;
	private int timeOut;

	public static void init() {
		random = ThreadLocalRandom.current();
	}

	@Deprecated
	public RedisDistribtLock(JedisCommands jedisCmd, String rowkey, String sendey, String thirdKey) {
		this.key = new StringBuilder().append(rowkey).append(CUSLOCK_FLAG).append(sendey).append(thirdKey).toString();// 自动产生key
		jedisCommond = jedisCmd;
		// random = new Random();
	}

	@Deprecated
	public RedisDistribtLock(JedisCommands jedisCmd, String rowkey, String other) {
		this.key = new StringBuilder().append(rowkey).append(CUSLOCK_FLAG).append(other).toString();// 自动产生key
		jedisCommond = jedisCmd;
		// random = new Random();
	}

	@Deprecated
	public RedisDistribtLock(JedisCommands jedisCmd, String rowkey) {
		this.key = new StringBuilder().append(rowkey).append(CUSLOCK_FLAG).toString();// 自动产生key
		jedisCommond = jedisCmd;
		// random = new Random();
	}

	public RedisDistribtLock(JedisCommands jedisCmd, String rowkey, int timeout) {
		this.key = new StringBuilder().append(rowkey).append(CUSLOCK_FLAG).toString();
		jedisCommond = jedisCmd;
		islocked = false;
		this.timeOut = timeout > DEFAULT_TIME_OUT ? timeout : DEFAULT_TIME_OUT;
	}

	public boolean lock() throws Exception {
		return lock(timeOut);
	}

	// 无论是否加锁成功，必须调用
	public void unlock() {
		try {
			jedisCommond.del(key);
		} catch (Exception e) {
		} finally {
			RedisSentineUtil.returnResource((Jedis) jedisCommond);
		}
	}

	/**
	 * @param t
	 *            单位:秒
	 * @return
	 * @throws Exception
	 */
	private boolean lock(long t) throws Exception {
		long nano = System.nanoTime();
		long myt = t * ONE_MILLI_NANOS * 1000000000;
		try {
			while ((System.nanoTime() - nano) < myt) {

				if (jedisCommond.setnx(key, LOCKED) == 1) {
					jedisCommond.expire(key, EXPIRE);
					islocked = true;
					return islocked;
				}

				// 短暂休眠，nano避免出现活锁
				Thread.sleep(3, random.nextInt(500));
			}
		} catch (JedisException e) {
			if (RedisConf.OOMSTR.equals(e.getMessage())) {
				throw new CusException(ErrorCode.REDIS_OOM, "【加锁失败】数据达到redis上限，缓存空间不足");
			}
		} catch (Exception e) {
			throw new CusException(StringUtil.getMsgStr("redis加锁失败:{}", key), e);
		}
		return false;
	}

}
