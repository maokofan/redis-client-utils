package maoko.redis.utils;

/**
 * 部署配置信息
 *
 * @author maoko
 * @date 2019/12/10 16:17
 */
public class RedisConf {
    public static final String OVERFLOW = "数据达到redis上限，丢包处理，写入数据失败";
    public static final String OOMSTR = "OOM command not allowed when used memory > 'maxmemory'.";
    // cluster 参数
    // 连接超时时间
    public static final int TIMEOUT = 10000;
    // 在将连接放回池中前，自动检验连接是否有效
    public static final boolean Test_ON_Return = false;
    public static final boolean Test_ON_Borrow = true;
    // 表示有一个idle object evitor线程对idle
    // object进行扫描，如果validate失败，此object会被从pool中drop掉；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义；
    public static final boolean testWhileIdle = true;
    // 表示idle object evitor两次扫描之间要sleep的毫秒数
    public static final int timeBetweenEvictionRunsMillis = 30000;
    // 表示一个对象至少停留在idle状态的最短时间，然后才能被idle object
    // evitor扫描并驱逐；这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
    public static final int MinEvictableIdleTimeMillis = 100000;
    // 表示idle object evitor每次扫描的最多的对象数 -1 scan all
    public static final int NumTestsPerEvictionRun = 10;
    // 在minEvictableIdleTimeMillis基础上，加入了至少minIdle个对象已经在pool里面了。如果为-1，evicted不会根据idle
    // time驱逐任何对象。
    // 如果minEvictableIdleTimeMillis>0，则此项设置无意义，且只有在timeBetweenEvictionRunsMillis大于0时才有意义；
    public static final int softMinEvictableIdleTimeMillis = 5000;

    // 控制一个pool最大的连接个数
    public static final int MAX_TOTAL = 100000;
    // 最大活跃数
    public static final int MAX_ACTIVE = 1000;
    // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值是8。
    public static final int MAX_IDLE = 50;
    // 控制一个pool最小空闲连接数
    public static final int MIN_IDLE = 10;
    // 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    public static final int MAX_WAIT_TIMEOUT = 20000;

    // single参数
    public static final String clusterName = "mymaster";
    public static final int connectionTimeout = 10000;
}
