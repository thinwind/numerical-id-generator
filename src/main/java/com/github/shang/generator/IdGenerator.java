package com.github.shang.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(IdWorker.class);
	private static int datacenterId;
	private static int workerId;
	private static final Properties prop;

	private static final IdWorker idWorker;

	private static final InstaIdGenerator instaIdGenerator;

	static {
		prop = new Properties();
		InputStream in = null;
		try {
			in = IdGenerator.class.getClassLoader().getResourceAsStream("id-gen.properties");
			prop.load(in);
			datacenterId = Integer.parseInt(prop.getProperty("datacenter_id"));
			workerId = Integer.parseInt(prop.getProperty("worder_id"));
		} catch (Exception e) {
			// datacenterId 默认为0
			datacenterId = 0;
			// workerId 默认为1
			workerId = 1;
			LOG.warn("ID生成器配置读取失败，使用默认值: datacenterId=0，workerId=1，建议检查后重新启动应用");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.error("id生成器配置文件读取流关闭失败{}", e);
					e.printStackTrace();
				}
			}
		}

		idWorker = new IdWorker(workerId, datacenterId);

		instaIdGenerator = new InstaIdGenerator();
	}

	/**
	 * 生成新ID
	 * 
	 * 根据部署机器及生成器标识进行id生成 区别于用户标识分片，此方法用生成的 机器信息进行区分
	 * 
	 * @return 新ID
	 */
	public static long nextId() {
		return idWorker.nextId();
	}

	/**
	 * 生成新ID
	 * 
	 * 根据用户标识，将id归为某个逻辑分片
	 * 
	 * @param tag
	 *            用户标识
	 * @return 新ID
	 */
	public static long nextId(long tag) {
		return instaIdGenerator.nextId(tag);
	}

	/**
	 * 获取uuid
	 *
	 * @return uuid
	 */
	public static String getUUID(boolean ignoreHyphen) {
		String uuid = UUID.randomUUID().toString();
		if (ignoreHyphen) {
			return uuid.replaceAll("-", "");
		} else
			return uuid;
	}

	/**
	 * 获取某个月的可能生成的最小与最大id对，这两个都是开放的，不包括在内
	 * 
	 * <p>
	 * <ul>
	 * <li>最小值计算方式： 上个月可能达到的最大值
	 * <li>最大值计算方式：下个月可能出现的最小值
	 * </ul>
	 * 
	 * @param year
	 *            年
	 * @param month
	 *            月份 与Calendar#MONTH一致，从0开始，到11结束
	 * 
	 * @see java.util.Calendar#MONTH
	 * 
	 * @return 此时间段可能生成的最小与最大id对
	 */
	public static IdPair getIdPairOfMonth(int year, int month) {
		Calendar cal = new GregorianCalendar(year, month, 1);

		return getIdPair(cal, Calendar.MONTH);
	}

	/**
	 * 获取某个月的可能生成的最小与最大id对，这两个都是开放的，不包括在内
	 * 
	 * <p>
	 * <ul>
	 * <li>最小值计算方式： 上个月可能达到的最大值
	 * <li>最大值计算方式：下个月可能出现的最小值
	 * </ul>
	 * 
	 * @return 此时间段可能生成的最小与最大id对
	 */
	public static IdPair getIdPairOfMonth(Date d) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);

		return getIdPairOfMonth(year, month);
	}

	/**
	 * 获取某一天的可能生成的最小与最大id对，这两个都是开放的，不包括在内 最小值计算方式： 上一天可能达到的最大值 最大值计算方式：
	 * 下一天可能出现的最小值
	 * 
	 * @return 此时间段可能生成的最小与最大id对
	 */
	public static IdPair getIdPairOfDay(int year, int month, int day) {
		Calendar cal = new GregorianCalendar(year, month, day);

		return getIdPair(cal, Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获取某一天的可能生成的最小与最大id对 这两个都是开放的，不包括在内 最小值计算方式： 上一天可能达到的最大值 最大值计算方式：
	 * 下一天可能出现的最小值
	 * 
	 * @return 此时间段可能生成的最小与最大id对
	 */
	public static IdPair getIdPairOfDay(Date day) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(day);

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int dayOM = cal.get(Calendar.DAY_OF_MONTH);

		return getIdPairOfDay(year, month, dayOM);
	}

	private static IdPair getIdPair(Calendar cal, int field) {
		// 先计算最大值
		cal.add(field, 1);
		long nextTime = TimeUtil.timeGen(cal.getTimeInMillis());
		long maxId = nextTime << (InstaIdGenerator.totalBitCount - InstaIdGenerator.timestampBitCount);

		cal.add(field, -1);
		long lastTime = TimeUtil.timeGen(cal.getTimeInMillis()) - 1;

		long minId = lastTime << (InstaIdGenerator.totalBitCount - InstaIdGenerator.timestampBitCount) | 0xfff;
		IdPair result = new IdPair(minId, maxId);
		return result;
	}

	/**
	 * 获取一个某个日期开始，一段秒数内的可能的最小与最大的id对
	 * 
	 * @param date
	 *            开始时间
	 * @param secondsInMillis
	 *            毫秒数
	 * 
	 * @return 此时间段可能生成的最小与最大id对
	 */
	public static IdPair getIdPairOfDateWithSecInMillis(Date date, long secondsInMillis) {
		long nextTime = TimeUtil.timeGen(date.getTime() + secondsInMillis + 1);
		long maxId = nextTime << (InstaIdGenerator.totalBitCount - InstaIdGenerator.timestampBitCount);

		long lastTime = TimeUtil.timeGen(date) - 1;

		long minId = lastTime << (InstaIdGenerator.totalBitCount - InstaIdGenerator.timestampBitCount) | 0xfff;
		IdPair result = new IdPair(minId, maxId);
		return result;
	}

	/**
	 * 获取一个某个日期开始，一段秒数内的可能的最小与最大的id对
	 * 
	 * @param date
	 *            开始时间
	 * @param seconds
	 * 
	 * @param date
	 *            开始时间
	 * @param secondsInMillis
	 *            毫秒数
	 * 
	 * @return 此时间段可能生成的最小与最大id对
	 */
	public static IdPair getIdPairOfDateWithSec(Date date, long seconds) {
		long secondsInMillis = seconds * 1000;
		return getIdPairOfDateWithSecInMillis(date, secondsInMillis);
	}

	/**
	 * 获取uuid
	 *
	 * @return uuid
	 */
	public static String getUUID() {
		return getUUID(true);
	}
}
