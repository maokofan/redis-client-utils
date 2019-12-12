package maoko.redis.utils.ifs;

/**
 * 发布消息接口
 *
 * @author fanpei
 *
 */
public interface IPubData {

	/**
	 * 获取频道
	 *
	 * @return
	 */
	String getChanal();

	/**
	 * 获取消息
	 *
	 * @return
	 */
	String getMessage();

}

