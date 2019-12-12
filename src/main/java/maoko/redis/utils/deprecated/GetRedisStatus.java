package maoko.redis.utils.deprecated;

import java.util.ArrayList;
import java.util.List;
import maoko.common.model.net.CusHostAndPort;
import maoko.redis.utils.entity.cluster.SvrNodeInfo;
import maoko.redis.utils.entity.cluster.SvrNodeState;
import maoko.redis.utils.core.RedisClusterUtil;
import maoko.redis.utils.entity.cluster.StateType;
import maoko.redis.utils.core.RedisPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

@Deprecated
public class GetRedisStatus {

    private static Jedis jedis;

    public static SvrNodeState getRedisStatus() {
        SvrNodeState svr = new SvrNodeState();
        List<SvrNodeInfo> ni = getNodesStatus();
        svr.setNodes(ni);
        svr.setState(getClusterStatus());
        return svr;
    }

    @Deprecated
    public static List<SvrNodeInfo> getNodesStatus() {

        RedisPoolConfig redisConfig = null;
        List<CusHostAndPort> hosts = redisConfig.getHosts();
        List<SvrNodeInfo> nodes = new ArrayList<SvrNodeInfo>();

        if (null != hosts) {
            for (CusHostAndPort host : hosts) {
                SvrNodeInfo nodeinfo = new SvrNodeInfo();
                nodeinfo.setIpAddr(String.format("%s:%d", host.getIP(), host.getPort()));
                try {
                    jedis = new Jedis(host.getIP(), host.getPort());
                    jedis.ping();
                    nodeinfo.setState(StateType.Normal);
                } catch (Exception e) {
                    if (e.getMessage().equals("java.net.ConnectException: Connection refused: connect")) {
                        nodeinfo.setState(StateType.Trouble);
                    }
                } finally {
                    jedis.close();
                }
                nodes.add(nodeinfo);
            }
        }
        return nodes;

    }

    public static StateType getClusterStatus() {
        JedisCluster jedis = null;
        StateType type = StateType.Normal;
        try {
            jedis = RedisClusterUtil.GetjedisCluster();
            jedis.get("");
        } catch (Exception e) {
            if (e.getMessage().equals("CLUSTERDOWN The cluster is down")) {
                type = StateType.Trouble;
            }
        } finally {

        }
        return type;
    }

}
