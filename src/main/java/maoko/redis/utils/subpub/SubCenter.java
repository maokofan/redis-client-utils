package maoko.redis.utils.subpub;

import maoko.common.log.IWriteLog;
import maoko.common.log.Log4j2Writer;
import maoko.common.tdPool.TdCachePoolExctor;
import maoko.redis.utils.CacheFactory;
import maoko.redis.utils.entity.cluster.SvrNodeState;
import maoko.redis.utils.except.CusException;
import maoko.redis.utils.ifs.ICache;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 订阅中心
 *
 * @author maoko
 * @date 2019/12/10 17:26
 */
public class SubCenter {
    private static final IWriteLog log = new Log4j2Writer(SubCenter.class);
    private static TdCachePoolExctor tdPool;// 消息订阅线程池
    private static ConcurrentMap<String, MesageSub> mesSubs;
    private ICache iCache;

    public static synchronized void init() throws CusException {
        tdPool = new TdCachePoolExctor();
        mesSubs = new ConcurrentHashMap<>();
    }

    public SubCenter(ICache iCache) {
        this.iCache = iCache;
    }

    /**
     * 开始订阅 非阻塞
     *
     * @param jedisPubSub
     * @param chanel
     */
    public void subChanel(JedisPubSub jedisPubSub, String chanel) throws CusException {
        if (mesSubs.containsKey(chanel))
            throw new CusException(chanel + "频道已经存在，请勿重复订阅！");
        MesageSub mesageSub = new MesageSub(jedisPubSub, chanel, new ISubRunnable() {
            @Override
            public SvrNodeState getSvrStates() {
                return iCache.getSvrStates();
            }

            @Override
            public void subChanel(JedisPubSub pubSub, String chanel)throws CusException {
                iCache.subScribe(pubSub, chanel);
            }
        });
        mesSubs.put(chanel, mesageSub);
        tdPool.execute(mesageSub);
    }

    /**
     * 停止所有频道订阅
     */
    public void stopAll() {
        log.info("ready for closing SubPub...");

        if (tdPool != null && mesSubs != null) {
            for (MesageSub mesageSub : mesSubs.values()) {
                try {
                    mesageSub.stopRun();
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
        if (tdPool != null)
            tdPool.shutdownNow();
        log.info("closing SubPub has compeleted");

    }

    /**
     * 停止指定频道订阅
     *
     * @param channel
     */
    public void stop(String channel) {
        log.info("ready for closing SubPub:{}", channel);
        MesageSub mesageSub = mesSubs.get(channel);
        if (mesageSub != null) {
            try {
                mesageSub.stopRun();
                log.info("closing SubPub: has compeleted", channel);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
