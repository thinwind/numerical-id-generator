package com.github.shang.generator;
public class TimeUtil {
	
	 private static long twepoch = 1288834974657L;
	
	public static long timeGen() {
		return System.currentTimeMillis()-twepoch;
	}
	
	public static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
}
