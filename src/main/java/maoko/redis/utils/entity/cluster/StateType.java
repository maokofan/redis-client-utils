package maoko.redis.utils.entity.cluster;

/**
 * 节点状态信息
 * 
 * @author fanpei
 *
 */
public enum StateType {
	Normal((byte) 0x00), Trouble((byte) 0x01), OFF((byte) 0x02);

	private byte ncode;

	StateType(byte _ncode) {
		this.ncode = _ncode;
	}

	public byte getValue() {
		return ncode;
	}

	public boolean isNormal() {
		if (ncode == 0x00)
			return true;
		else
			return false;
	}
}
