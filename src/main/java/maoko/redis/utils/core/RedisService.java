package maoko.redis.utils.core;

import maoko.common.gson.JSONUtil;
import maoko.common.model.enm.EHostType;
import maoko.common.model.net.CusHostAndPort;
import maoko.redis.utils.RedisConf;
import maoko.redis.utils.entity.cluster.SvrNodeInfo;
import maoko.redis.utils.entity.cluster.StateType;
import maoko.redis.utils.entity.*;
import maoko.redis.utils.except.CusException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Slowlog;

import java.text.SimpleDateFormat;
import java.util.*;

public class RedisService {

    public static JedisPoolConfig getconf(boolean isCluster) {
        JedisPoolConfig config = new JedisPoolConfig();
        // 自动测试池中的空闲连接是否都是可用连接
        config.setTestWhileIdle(RedisConf.testWhileIdle);
        config.setTestOnReturn(RedisConf.Test_ON_Return);
        config.setTestOnBorrow(RedisConf.Test_ON_Borrow);

        config.setMaxIdle(RedisConf.MAX_IDLE);
        config.setMinIdle(RedisConf.MIN_IDLE);
        config.setMaxWaitMillis(RedisConf.MAX_WAIT_TIMEOUT);
        config.setTimeBetweenEvictionRunsMillis(RedisConf.timeBetweenEvictionRunsMillis);
        config.setMinEvictableIdleTimeMillis(RedisConf.MinEvictableIdleTimeMillis);
        config.setNumTestsPerEvictionRun(RedisConf.NumTestsPerEvictionRun);
        config.setSoftMinEvictableIdleTimeMillis(RedisConf.softMinEvictableIdleTimeMillis);
        config.setMaxTotal(RedisConf.MAX_TOTAL);
        if (!isCluster) {
        } else {
        }
        return config;
    }

    @Deprecated
    public static List<RedisGuide> getRedisInfo() {
        // 获取redis服务器信息
        String info = RedisClusterUtil.getRedisInfo();
        List<RedisGuide> ridList = new ArrayList<RedisGuide>();
        String[] strs = info.split("\n");
        RedisGuide rif = null;
        if (strs != null && strs.length > 0) {
            for (int i = 0; i < strs.length; i++) {
                rif = new RedisGuide();
                String s = strs[i];
                String[] str = s.split(":");
                if (str != null && str.length > 1) {
                    String key = str[0];
                    String value = str[1];
                    rif.setKey(key);
                    rif.setValue(value);
                    ridList.add(rif);
                }
            }
        }
        return ridList;
    }

    // 获取redis日志列表
    public static List<RedisExcuteLog> getLogs(long entries) {
        List<Slowlog> list = RedisClusterUtil.getLogs(entries);
        List<RedisExcuteLog> opList = null;
        RedisExcuteLog op = null;
        boolean flag = false;
        if (list != null && list.size() > 0) {
            opList = new LinkedList<RedisExcuteLog>();
            for (Slowlog sl : list) {
                String args = JSONUtil.genJsonStr(sl.getArgs());
                if (args.equals("[\"PING\"]") || args.equals("[\"SLOWLOG\",\"get\"]") || args.equals("[\"DBSIZE\"]") || args.equals("[\"INFO\"]")) {
                    continue;
                }
                op = new RedisExcuteLog();
                flag = true;
                op.setId(sl.getId());
                op.setExecuteTime(getDateStr(sl.getTimeStamp() * 1000));
                op.setUsedTime(sl.getExecutionTime() / 1000.0 + "ms");
                op.setArgs(args);
                opList.add(op);
            }
        }
        if (flag)
            return opList;
        else
            return null;
    }

    // 获取日志总数
    public static Long getLogLen() {
        return RedisClusterUtil.getLogsLen();
    }

    // 清空日志
    public static String logEmpty() {
        return RedisClusterUtil.logEmpty();
    }

    // 获取当前数据库中key的数量
    public static Map<String, Object> getKeysSize() {
        long dbSize = RedisClusterUtil.dbSize();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("create_time", new Date().getTime());
        map.put("dbSize", dbSize);
        return map;
    }

    // 获取当前redis使用内存大小情况
    public static HostMemInfo getMemeryInfo(Jedis jedis, boolean returnSource) {

        String[] strs = RedisClusterUtil.getRedisInfo(jedis, returnSource).split("\n");
        CusHostAndPort host = new CusHostAndPort(jedis.getClient().getHost(), jedis.getClient().getPort());
        MemInfo mc = new MemInfo();
        byte getNum = 0;
        for (int i = 0; i < strs.length; i++) {
            String s = strs[i];
            String[] detail = s.split(":");
            switch (detail[0]) {
                case "used_memory":
                    mc.setUsed(Long.parseLong(getValue(detail)));
                    getNum++;
                    continue;
                case "maxmemory":
                    mc.setMax(Long.parseLong(getValue(detail)));
                    getNum++;
                    continue;

                case "total_system_memory":
                    mc.setTotal(Long.parseLong(getValue(detail)));
                    getNum++;
                    continue;

                    // case "tcp_port":
                    // host.setPort(Integer.parseInt(value));
                    // continue;

                case "role":
                    host.setType("master".equals(getValue(detail)) ? EHostType.MASTER : EHostType.SLAVE);
                    getNum++;
                    continue;
            }

            if (4 <= getNum)
                break;
        }
        return new HostMemInfo(host, mc);
    }

    private static String getValue(String[] detail) {
        return detail[1].substring(0, detail[1].length() - 1);
    }

    public static List<SvrNodeInfo> getClusterNodes() {

        List<SvrNodeInfo> infos = new ArrayList<SvrNodeInfo>(RedisClusterUtil.NodeInfoMaps.size());
        // int ipPortLen = infos.get(0).getIpAddr().length();
        String[] strs = RedisClusterUtil.getClusterNodes().split("\n");
        for (int i = 0; i < strs.length; i++) {
            String s = strs[i];
            String[] detail = s.split(" ");
            SvrNodeInfo info = RedisClusterUtil.NodeInfoMaps.get(detail[1]);
            if (info == null)
                info = new SvrNodeInfo(detail[1], StateType.Normal);
            if (s.contains("fail")) {
                info.setState(StateType.Trouble);
            } else
                info.setState(StateType.Normal);
            if ("slave".equals(detail[2]))
                info.setRole(EHostType.SLAVE);
            infos.add(info);
        }

        return infos;
    }

    public static boolean clusterNormal() {
        boolean normal = false;
        String[] strs = RedisClusterUtil.getClusterInfo().split("\n");
        for (int i = 0; i < strs.length; i++) {
            String s = strs[i];
            String[] detail = s.split(":");
            if (detail[0].equals("cluster_state")) {
                normal = "ok".equals(getValue(detail));
                break;
            }
        }
        return normal;
    }

    @SuppressWarnings("unused")
    @Deprecated
    private static String getIpPort(String node, int ipPortLen) {
        int len = "[8a5927b5df98f592984f9c991c48a3bbd9c058cb ".length();
        return node.substring(len, ipPortLen);

    }

    /**
     * @param jedis
     * @return
     * @throws CusException
     */
    @Deprecated
    public static boolean singleNodeNormal(Jedis jedis) throws CusException {
        boolean normal = false;
        // String strs =
        // RedisSingle_SentineUtil.getSingNodeInfo(jedis);
        // normal = true;
        return normal;
    }

    private static String getDateStr(long timeStmp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date(timeStmp));
    }
}
