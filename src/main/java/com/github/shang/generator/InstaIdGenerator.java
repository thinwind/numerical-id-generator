package com.github.shang.generator;
import static com.github.shang.generator.TimeUtil.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author Mr_Shang
 * 
 * @version 1.0
 *
 */
public class InstaIdGenerator {

	protected static final Logger LOG = LoggerFactory.getLogger(IdWorker.class);

	/**
	 * 时间戳的位数，实际占41位，最高位保持为0，保证long值为正数
	 */
	public static final int timestampBitCount = 42;

	/**
	 * 逻辑分片位数
	 */
	public static final int regionBitCount = 10;

	/**
	 * 逻辑分片的最大数量
	 */
	public static final int regionModelVal = 1 << regionBitCount;

	/**
	 * 序列位数
	 */
	public static final int sequenceBitCount = 12;

	/**
	 * 总的位数
	 */
	public static final int totalBitCount = timestampBitCount + regionBitCount + sequenceBitCount;

	/**
	 * 当前序列值
	 */
	private long sequence = 0;

	/**
	 * 最后一次请求时间戳
	 */
	private long lastTimestamp = -1L;

	/**
	 * 序列的位板
	 */
	private long sequenceMask = -1L ^ (-1L << sequenceBitCount);
	
	/**
	 * 最后一次请求用户标识
	 */
	private long lastTag=1L;

	public InstaIdGenerator() {

	}

	public InstaIdGenerator(long seq) {
		if (seq < 0) {
			seq = 0;
		}
		this.sequence = seq;
	}

	public synchronized long nextId(long tag) {
		long timestamp = timeGen();

		if(tag<0){
			tag=-tag;
		}
		
		if (timestamp < lastTimestamp) {
			LOG.error(String.format("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp));
			throw new RuntimeException(String.format(
					"Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
		}

		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0L;
		}
		
		if(tag==lastTag){
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		}
		lastTag=tag;
		
		lastTimestamp = timestamp;

		return (timestamp << (totalBitCount - timestampBitCount))
				| ((tag % regionModelVal) << (totalBitCount - timestampBitCount - regionBitCount)) | sequence;
	}
}
