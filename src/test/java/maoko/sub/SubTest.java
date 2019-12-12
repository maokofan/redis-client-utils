package maoko.sub;

import maoko.common.DateFormatUtil;
import maoko.common.StaticClass;
import maoko.common.StringUtil;
import maoko.common.exception.DataIsNullException;
import maoko.common.exception.OstypeMissWatchException;
import maoko.common.log.LoginitException;
import maoko.redis.utils.CacheFactory;
import maoko.redis.utils.except.CusException;
import maoko.redis.utils.ifs.ICache;
import maoko.redis.utils.subpub.SubCenter;
import maoko.sdk.SDKCommon;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Date;

/**
 * @author maoko
 * @date 2019/12/10 17:08
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SubTest {

    private static final String CHANEL_NAME = "LIDONGREDISTEST";
    private static ICache cache;
    private static SubCenter subCenter;

    @BeforeClass
    public static void start() throws CusException, OstypeMissWatchException, DataIsNullException, LoginitException {
        SDKCommon.init();
        cache = CacheFactory.createCacheOpt();
        cache.start();
        subCenter = new SubCenter();
        System.out.println("Hello redis! inited success!");
    }

    @Test
    public void test01sub() throws Exception {
        SubPuber subPuber = new SubPuber(CHANEL_NAME);
        subCenter.subChanel(subPuber, subPuber.getChanel());
    }

    @Test
    public void test02pub() throws InterruptedException, CusException {

        while (true) {
            cache.pubScribe(CHANEL_NAME, StringUtil.getMsgStr("hello redis,now time is {}", DateFormatUtil.dateformat(new Date())));
            Thread.sleep(1000);
        }
    }
}
