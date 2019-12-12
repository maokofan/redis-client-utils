package maoko.redis.utils.entity.cluster;
import maoko.common.model.enm.EHostType;

/**
 * 服务节点信息
 * 
 * @author fanpei
 *
 */
public class SvrNodeInfo implements Comparable<SvrNodeInfo> {
	private EHostType role;// 主机类型
	private String name;// 节点名,可为空
	private String ipAddr;// ip地址和端口
	private StateType state;// 节点状态

	public boolean isMaster() {
		return EHostType.MASTER == role;
	}

	public EHostType getRole() {
		return role;
	}

	public void setRole(EHostType role) {
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * ip地址和端口
	 * 
	 * @return
	 */
	public String getIpAddr() {
		return ipAddr;
	}

	public String getIp() {
		return ipAddr.split(":")[0];
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public StateType getState() {
		return state;
	}

	public void setState(StateType state) {
		this.state = state;
	}

	public SvrNodeInfo(String name, String ipAddr, StateType state) {
		this.role = EHostType.MASTER;
		this.name = name;
		this.ipAddr = ipAddr;
		this.state = state;
	}

	public SvrNodeInfo(String ipAddr, StateType state) {
		this.role = EHostType.MASTER;
		this.ipAddr = ipAddr;
		this.state = state;
	}

	public SvrNodeInfo(String ipAddr) {
		this.role = EHostType.MASTER;
		this.ipAddr = ipAddr;
		this.state = StateType.Trouble;
	}

	public SvrNodeInfo() {
		this.role = EHostType.MASTER;
		this.state = StateType.Trouble;
		this.ipAddr = "";
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null)
			return false;
		if (!arg0.getClass().equals(SvrNodeInfo.class))
			return false;

		SvrNodeInfo other = (SvrNodeInfo) arg0;
		if (this.ipAddr != other.ipAddr)
			return false;
		if (this.name != other.name)
			return false;

		boolean nameEqual = true;
		if (this.name != null)
			nameEqual = name.equals(other.name);

		boolean ipequal = true;
		if (this.ipAddr != null)
			ipequal = this.ipAddr.equals(other.ipAddr);

		if (nameEqual && ipequal && this.role == other.role && this.state == other.state)
			return true;
		else
			return false;

	}

	@Override
	public int compareTo(SvrNodeInfo o) {
		return name.compareTo(o.getName());
	}

}
