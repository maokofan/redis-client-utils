package maoko.redis.utils.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 随机数组
 * 
 * @author fanpei
 *
 */
public class RandomArry<T> {

	List<T> datas;
	Random rd;

	/**
	 * 初始化容量为3
	 */
	public RandomArry() {
		rd = new Random();
		datas = new ArrayList<T>(3);
	}

	/**
	 * 指定容量
	 * 
	 * @param capcity
	 */
	public RandomArry(int capcity) {
		rd = new Random();
		datas = new ArrayList<T>(capcity);
	}
	
	public RandomArry(List<T> src)
	{

		rd = new Random();
		this.datas = src;
	}

	public void add(T e) {
		datas.add(e);
	}

	public T getRadomObj()
	{
		int index = rd.nextInt(datas.size());
		return datas.get(index);
	}

}
