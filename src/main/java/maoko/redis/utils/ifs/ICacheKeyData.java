package maoko.redis.utils.ifs;

/**
 * 缓存批量数据类型接口
 * 
 * @author fanpei
 *
 */
public interface ICacheKeyData {

	/**
	 * 获取一级缓存key字符串形式
	 * 
	 * @return
	 */
	String getFirstKeyStr();

	/**
	 * 一级key bytes
	 * 
	 * @return
	 */
	byte[] getFirstKeys();

	/**
	 * 二级key bytes
	 * 
	 * @return
	 */
	byte[] getSencondKeys();

	/**
	 * 获得权值
	 * 
	 * @return
	 */
	long getScore();

	/**
	 * 获取下一个index byte[]
	 * 
	 * @return
	 */
	byte[] getNextIndexBytes();

	/**
	 * 数据 bytes
	 * 
	 * @return
	 */
	byte[] getValueDatas();

}
