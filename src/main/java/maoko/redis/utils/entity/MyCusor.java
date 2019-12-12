package maoko.redis.utils.entity;

/**
 * 游标
 * 
 * @author fanpei
 *
 */
public class MyCusor {
	byte[] value;

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	public MyCusor() {
	}

	public MyCusor(byte[] value) {
		this.value = value;
	}

}
