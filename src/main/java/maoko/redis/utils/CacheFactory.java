package maoko.redis.utils;


import maoko.redis.conf.ConfigUtil;
import maoko.redis.utils.core.RedisDistribtLock;
import maoko.redis.utils.core.RedisPoolConfig;
import maoko.redis.utils.except.CusException;
import maoko.redis.utils.ifs.ICache;
import maoko.redis.utils.subpub.SubCenter;

public class CacheFactory {
    public static RedisPoolConfig redisConfig;

    /**
     * 创建实例
     *
     * @return
     */
    public static synchronized ICache createCacheOpt() throws CusException {
        ConfigUtil.GetInstance().ReadConfig();
        redisConfig = ConfigUtil.GetInstance().getRedisconf();
        RedisDistribtLock.init();
        ICache iCache;
        if (redisConfig.isCloud()) {
            throw new IllegalArgumentException("目前不支持分布式集群模式,需完善CacheClusterImp的开发");
            // return new CacheClusterImp();
        } else {
            iCache = new CacheSingleImp();
        }
        SubCenter.init(iCache);//开启订阅
        return iCache;
    }
}
