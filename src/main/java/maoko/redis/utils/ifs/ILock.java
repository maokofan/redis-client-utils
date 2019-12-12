package maoko.redis.utils.ifs;

/**
 * 锁接口
 */
public interface ILock {

    boolean lock() throws Exception;

    void unlock();
}
