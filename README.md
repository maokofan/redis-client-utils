# redis-client-utils
redis客户端助手
# 主要功能
1. 非阻塞订阅发布； 
2. 基本的数据结构操作； 
3. 自动回收连接。
# 1.使用方式
 ## 配置redis.xml文件，并放入程序启动同级目录config目录下
 ```
 <?xml version="1.0" encoding="utf-8"?>
<Redis>
    <Nodes sentinel="false"><!-- 是否是哨兵主从模式 -->
        <HostAndPort IP="127.0.0.1" Port="6379"/>
    </Nodes>
</Redis>
 ```
 ## 程序入口进行初始化
```
Class APP
{
  public static void main(String[] args)
  {
        SDKCommon.init();
        cache = CacheFactory.getRedisClientUtils();
        cache.start();
  }
}
```
# 2.Example
## 订阅和发布
```
 String CHANEL_NAME = "LIDONGREDISTEST";
 //异步订阅
 SubPuber subPuber = new SubPuber(CHANEL_NAME);
 cache.subScribeNio(subPuber, subPuber.getChanel());
```
```
//循环发布
 String CHANEL_NAME = "LIDONGREDISTEST";
 while (true) {
            try {
                cache.pubScribe(CHANEL_NAME, StringUtil.getMsgStr("hello redis,now time is {}", DateFormatUtil.dateformat(new Date())));
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
```

