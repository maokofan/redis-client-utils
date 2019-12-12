package maoko.redis.utils.core;

import maoko.common.model.net.CusHostAndPort;

import java.util.List;

/**
 * redis配置
 * 
 * @author fanpei
 *
 */
public class RedisPoolConfig {
	// 是否是哨兵模式
	boolean isSentinel = false;

	// 集群节点
	List<CusHostAndPort> hosts;

	public List<CusHostAndPort> getHosts() {
		return hosts;
	}

	public void setHosts(List<CusHostAndPort> hosts) {
		this.hosts = hosts;
	}

	public boolean isSentinel() {
		return isSentinel;
	}

	public void setSentinel(boolean isSentinel) {
		this.isSentinel = isSentinel;
	}

	/**
	 * 是否是集群
	 * 
	 * @return
	 */
	public boolean isCloud() {
		if (isSentinel)
			return false;

		if (hosts != null && !hosts.isEmpty() && hosts.size() > 1)
			return true;
		else
			return false;
	}
}
