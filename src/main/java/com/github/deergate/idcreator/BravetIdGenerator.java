package com.github.deerdate.idcreator;

/**
 * 基于Snowflake的ID生成器
 * ID长度64位，结构如下:
 * 
 * +--------+-----------------+---------------+-------------+------------+
 * |  1bit  |      41bit      |     5bit      |   7bit      |   10bit    |
 * +--------+-----------------+---------------+-------------+------------+
 * |  sign  | delta timestamp |  data center  | machine id  |  sequence  |
 * +--------+-----------------+---------------+-------------+------------+
 *
 * <p>
 * 各段含义如下：
 * ----------------------------------------------------------------------------
 * |      sign      |  1位  |  符号位  |  取值为0，保证id值不为负                  |
 * ----------------------------------------------------------------------------
 * |    timestamp   |  41位 |  时间戳  |  当前时间与开始时间的时间戳差              |
 * ----------------------------------------------------------------------------
 * |   data center  |  5位  | 数据中心 |  0~31,最多32个数据中心                   |
 * ----------------------------------------------------------------------------
 * |    machine id  |  7位  | 机器标识 |  0~127，每个数据中心最多可以部署128台机器   |
 * ----------------------------------------------------------------------------
 * |    sequence    |  10位 | 序列计数 |  每个机器同一毫秒内最多可以产生1024个id号   |
 * ----------------------------------------------------------------------------
 * 
 * 注意：
 * timestamp 取值不是当前的时间戳，而是当前时间与开始时间的时间戳差，开始时间一般设置为系统开始使用时间。
 * 从开始时间，此id生成器可以保证69年内不产生重复id。((1L<<41)/(365*24*60*60*1000)=69)
 * 
 * 理论上，每台机器1秒最多产生1024*1000(100w)个id
 * 
 * 实测结果如下：
 * 		机器: 15款 MBP
 * 		CPU: 2.9 GHz 双核Intel Core i5
 * 		内存: 8 GB 1867 MHz DDR3
 * 		线程数: 100
 * 		MAX: 99w
 * 		MIN: 72w
 * 		AVG: 91w
 * </p>
 * 
 * @author Shang Yehua <niceshang@outlook.com>
 * 
 * @version 2.0
 *
 */
public class BravetIdGenerator {

	/**
	 * 计时起始点 1989-10-11 08:00:00.000
	 * 
	 * After 69 years, where are u and where am I?
	 * Would u still remember me over that long time ...
	 * 
	 */
	private static final long START = 624067200000L;

	/**
	 * Data Center 位数
	 */
	private static final long DATACENTER_BIT_COUNT = 5;

	/**
	 * Machine id 位数
	 */
	private static final long MACHINE_ID_BIT_COUNT = 7;

	/**
	 * 最大机器ID
	 * 
	 * 机器ID不能超过此值
	 */
	private static final long MAX_MACHINE_ID = -1L ^ (-1L << MACHINE_ID_BIT_COUNT);

	/**
	 * 最大数据中心ID
	 * 
	 * 数据中心ID不能超过此值
	 */
	private static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_BIT_COUNT);

	/**
	 * 序列位数
	 */
	private static final long SEQUENCE_BIT_COUNT = 10;

	private static final long TIMESTAMP_LEFT_SHIFT =
			DATACENTER_BIT_COUNT + MACHINE_ID_BIT_COUNT + SEQUENCE_BIT_COUNT;

	private static final long DATA_CENTER_SHIFT = MACHINE_ID_BIT_COUNT + SEQUENCE_BIT_COUNT;

	private static final long MACHINE_ID_SHIFT = SEQUENCE_BIT_COUNT;

	/**
	 * 序列的位板
	 */
	private static final int SEQUENCE_MASK = -1 ^ (-1 << SEQUENCE_BIT_COUNT);

	/**
	 * 当前序列值
	 */
	private int cursor = 0;

	/**
	 * 最后一次请求时间戳
	 */
	private volatile long lastTimestamp = -1L;

	public final long dataCenterID;

	public final long machineId;

	private final long dataCenterTag;

	private final long machineIdTag;

	public BravetIdGenerator(long dataCenterId, long machineId) {
		if (dataCenterId > MAX_DATACENTER_ID || dataCenterId < 0) {
			throw new IllegalArgumentException(String.format(
					"data center Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
		}
		if (machineId > MAX_MACHINE_ID || machineId < 0) {
			throw new IllegalArgumentException(String
					.format("machine Id can't be greater than %d or less than 0", MAX_MACHINE_ID));
		}
		this.dataCenterID = dataCenterId;
		this.machineId = machineId;
		dataCenterTag = dataCenterId << DATA_CENTER_SHIFT;
		machineIdTag = machineId << MACHINE_ID_SHIFT;
	}

	public synchronized long nextId() {
		long timestamp = System.currentTimeMillis() - START;
		//校验时间，如果时间被回拨，1秒内可以等待
		//超过1秒，生成失败
		if (timestamp < lastTimestamp) {
			if (lastTimestamp - timestamp < 3000) {
				//1秒以内，暂停进行矫正
				timestamp = tilNextMillis(lastTimestamp);
			} else {
				throw new RuntimeException(String.format(
						"Clock moved backwards.  Refusing to generate id for %d milliseconds",
						lastTimestamp - timestamp));
			}
		}
		if (lastTimestamp == timestamp) {
			cursor = (cursor + 1) & SEQUENCE_MASK;
			if (cursor == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			cursor = 0;
		}
		lastTimestamp = timestamp;
		return (timestamp << TIMESTAMP_LEFT_SHIFT) | dataCenterTag | machineIdTag | cursor;
	}

	static long tilNextMillis(long currentTimeMillis) {
		long timestamp = System.currentTimeMillis() - START;
		while (timestamp <= currentTimeMillis) {
			timestamp = System.currentTimeMillis() - START;
		}
		return timestamp;
	}

}
