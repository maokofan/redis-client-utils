package maoko.redis.utils.ifs;


/**
 * 消息发布通知
 * 
 * @author fanpei
 *
 */
public interface ISubPubMsg extends IPubData {
	String SPLIT = "|";// 分隔符
	String PARESE_SPLIT = "\\|";// 分隔符

	/**
	 * 组装消息
	 *
	 * @param sb
	 * @param str
	 */
	static void add(StringBuilder sb, String str) {
		sb.append(str).append(SPLIT);
	}

	/**
	 * 组装消息
	 * 
	 * @param sb
	 * @param str
	 */
	static void add(StringBuilder sb, int str) {
		sb.append(str).append(SPLIT);
	}

	/**
	 * 解析消息称对象
	 * 
	 * @param message
	 * @return
	 */
	void parseMessage(String message) throws Exception;

}
