package maoko.redis.utils.ifs;

public interface ICacheSecnKey {

	/**
	 * 获取key byte[]形式
	 *
	 * @return
	 */
	byte[] getFirstKeyBytes();

	/**
	 * 获取key byte[]形式
	 *
	 * @return
	 */
	byte[] getSecondKeyBytes();

}

