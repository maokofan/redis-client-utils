package com.chart.redis;

import maoko.common.log.IWriteLog;
import maoko.common.log.Log4j2Writer;
import maoko.common.tdPool.TdCachePoolExctor;
import redis.clients.jedis.JedisPubSub;

/**
 * 消息订阅发布器
 *
 * @author fanpei
 */

public class SubPuber extends JedisPubSub {
    private static final IWriteLog log = new Log4j2Writer(SubPuber.class);
    private static TdCachePoolExctor tdPool=new TdCachePoolExctor();
    private String channel;

    public SubPuber(String channel) {
        this.channel = channel;
    }

    public String getChanel() {
        return channel;
    }

    @Override
    public void onMessage(String channel, String message) {
        final String channel1=channel;
        final String message1=message;
        tdPool.execute(new Runnable() {
            public void run() {
                log.debug("收到频道[{}]消息：{}", channel1, message1);
            }
        });
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
       // log.debug("开始订阅  channel:{} subscribedChannels:", channel,
              //  Integer.toString(subscribedChannels));
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
       // log.debug("取消订阅 channel:{} subscribedChannels:", channel,
                //Integer.toString(subscribedChannels));
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {

    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {

    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {


    }

}
