package maoko;

import maoko.redis.utils.CacheFactory;
import maoko.redis.utils.ifs.ICache;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        try {
            ICache cache = CacheFactory.createCacheOpt();
            cache.start();
            System.out.println("Hello redis! inited success!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
