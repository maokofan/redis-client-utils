package maoko.redis.utils.subpub;

import maoko.redis.utils.entity.cluster.SvrNodeState;
import maoko.redis.utils.except.CusException;
import redis.clients.jedis.JedisPubSub;

/**
 * @author maoko
 * @date 2019/12/13 13:43
 */
public interface ISubRunnable {
    SvrNodeState getSvrStates();

    void subChanel(JedisPubSub pubSub, String chanel)throws CusException;
}
