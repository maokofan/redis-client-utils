package maoko.redis.utils.entity;

/**
 * 内存情况实体
 *
 * @author fanpei
 *
 */
public class MemInfo {

	long total;//

	long max;// 最大
	long used;// 已分配[使用]
	long free;// 已分配

	public MemInfo() {
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	/**
	 * jvm最大内存
	 *
	 * @return
	 */
	public long getMax() {
		return max;
	}

	/**
	 * 获取已分配的内存
	 *
	 * @return
	 */
	public long getUsed() {
		return used;
	}

	/**
	 * 已分配中可用内存
	 *
	 * @return
	 */
	public long getFree() {
		return free;
	}

	/**
	 * 当前最大可用内存
	 *
	 * @return
	 */
	public long getUsable() {
		return max - used + free;
	}

	public void setMax(long max) {
		this.max = max;
	}

	public void setUsed(long total) {
		this.used = total;
	}

	public void setFree(long free) {
		this.free = free;
	}

	public float getUsedPercent() {
		return used * 1f / max;
	}

	public float getUsedPercent_total() {
		return used * 1f / total;
	}

	public MemInfo(long max, long total, long free) {
		this.max = max;
		this.used = total;
		this.free = free;
	}

	public MemInfo(long max, long used) {
		this.max = max;
		this.used = used;
	}

	@Override
	public String toString() {
		return String.format("max:%d total:%d free:%d usePercent %f %%", getMax(), getUsed(), getUsable(),
				getUsedPercent() * 100);
	}

}
