package maoko.redis.utils.entity;

public class MyTuple implements Comparable<MyTuple> {
	private String element;
	private Double score;

	public MyTuple(String element, Double score) {
		super();
		this.element = element;
		this.score = score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result + element.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyTuple other = (MyTuple) obj;
		if (element.equals(other.getElement()) && score == other.getScore()) {
			return true;
		} else
			return false;
	}

	@Override
	public int compareTo(MyTuple other) {
		if (this.score == other.getScore() || this.element == other.element)
			return 0;
		else
			return this.score < other.getScore() ? -1 : 1;
	}

	/**
	 * 获取值
	 *
	 * @return
	 */
	public String getElement() {
		return element;
	}

	/**
	 * 获取分数
	 *
	 * @return
	 */
	public double getScore() {
		return score;
	}

	@Override
	public String toString() {
		return '[' + element + ',' + score + ']';
	}
}
