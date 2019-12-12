package maoko.redis.utils.subpub;

import maoko.common.log.IWriteLog;
import maoko.common.log.Log4j2Writer;
import maoko.redis.utils.entity.cluster.SvrNodeState;
import maoko.redis.utils.ifs.ICache;
import redis.clients.jedis.JedisPubSub;

/**
 * 消息订阅器
 *
 * @author maoko
 * @date 2019/12/10 17:35
 */
public class MesageSub implements Runnable {
    private static IWriteLog log = new Log4j2Writer(MesageSub.class);
    private JedisPubSub jedisPubSub;
    private String channelName;
    private ICache iCache;
    private boolean isStoped = false;

    /**
     * @param jedisPubSub
     * @param channelName
     * @param iCache      缓存助手
     */
    public MesageSub(JedisPubSub jedisPubSub, String channelName, ICache iCache) {
        this.jedisPubSub = jedisPubSub;
        this.channelName = channelName;
        this.iCache = iCache;
    }

    private Thread td;

    public void stopRun() throws InterruptedException {
        isStoped = true;
        if (td != null)
            td.interrupt();
    }

    @Override
    public void run() {
        long printTime = 0;
        long timeexpire = 2 * 60 * 60 * 1000;
        td = Thread.currentThread();
        td.setName(String.format("消息订阅-[%s]", channelName));

        while (!isStoped) {
            try {
                SvrNodeState state = iCache.getSvrStates();
                if (state != null && !state.isTrouble())
                    iCache.subScribe(jedisPubSub, channelName);
            } catch (Exception e) {
                e.printStackTrace();
                if (System.currentTimeMillis() - printTime > timeexpire) {
                    printTime = System.currentTimeMillis();
                    log.warn("订阅发生异常,2小时以后再次打印 type{} message:{}", channelName, e);
                }
            }
            try {
                Thread.sleep(2000);// 订阅异常2秒钟后重试
            } catch (InterruptedException e) {
                break;
            }
        }
        log.info("频道:{] 线程:{}已退出", channelName, td.getName());
    }
}
