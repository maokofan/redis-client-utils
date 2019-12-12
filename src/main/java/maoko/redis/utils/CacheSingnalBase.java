package maoko.redis.utils;

import maoko.common.ExceptionUtil;
import maoko.common.model.enm.EHostType;
import maoko.redis.utils.entity.cluster.SvrNodeInfo;
import maoko.redis.utils.entity.cluster.StateType;
import maoko.redis.utils.except.ErrorCode;
import maoko.redis.utils.except.CusException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;

public class CacheSingnalBase {
    protected long setKeyExpire(JedisCommands jedisCmd, String key, int expireTime) throws CusException {
        long time = -1;
        try {
            if (expireTime > 0)
                time = jedisCmd.expire(key, expireTime);
        } catch (Exception e) {
            throw new CusException(ErrorCode.RedisCacheError,
                    String.format("设置key过期时间 失败,%s ", ExceptionUtil.getCauseMessage(e)));
        }
        return time;
    }

    protected long setKeyExpire(Jedis jedisCmd, byte[] keys, int expireTime) throws CusException {
        long time = -1;
        try {
            if (expireTime > 0)
                time = jedisCmd.expire(keys, expireTime);
        } catch (Exception e) {
            throw new CusException(ErrorCode.RedisCacheError,
                    String.format("设置key过期时间 失败,%s ", ExceptionUtil.getCauseMessage(e)));
        }
        return time;
    }

    protected SvrNodeInfo getNodeInfo(String info) {
        String name = info.substring(0, info.indexOf(":ip="));
        int addr_start = info.lastIndexOf("ip=");
        int addr_end = info.lastIndexOf(",state");
        String addr = info.substring(addr_start, addr_end);
        String[] addrs = addr.split(",");
        String host = addrs[0].replace("ip=", "") + ":" + addrs[1].replace("port=", "");
        String state = info.substring(info.lastIndexOf("state=") + "state=".length(), info.indexOf(",offset"));
        SvrNodeInfo node = new SvrNodeInfo(name, host, "online".equals(state) ? StateType.Normal : StateType.Trouble);
        node.setRole(EHostType.SLAVE);
        return node;
    }

}
