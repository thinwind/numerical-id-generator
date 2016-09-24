package com.github.shang.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(IdWorker.class);
	private final static int datacenterId;
	private final static int workerId;
	private static final Properties prop;
	
	private static final IdWorker idWorker;
	
	private static final InstaIdGenerator instaIdGenerator;
	
	static{
		prop=new Properties();
		InputStream in=null;
		try {
			in=IdGenerator.class.getClassLoader().getResourceAsStream("id-gen.properties");
			prop.load(in);
			datacenterId=Integer.parseInt(prop.getProperty("datacenter_id"));
			workerId=Integer.parseInt(prop.getProperty("worder_id"));
		} catch (Exception e) {
			throw new RuntimeException("id生成器配置读取失败");
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					LOG.error("id生成器配置文件读取流关闭失败{}",e);
					e.printStackTrace();
				}
			}
		}
		
		idWorker =new IdWorker(workerId,datacenterId);
		
		instaIdGenerator=new InstaIdGenerator();
	}
	
	/**
	 * 生成新ID
	 * 
	 * 		根据部署机器及生成器标识进行id生成
	 * 		区别于用户标识分片，此方法用生成的
	 *		机器信息进行区分
	 * 
	 * @return 新ID
	 */
	public static long nextId(){
		return idWorker.nextId();
	}
	
	/**
	 * 生成新ID
	 *  	
	 *  	根据用户标识，将id归为某个逻辑分片
	 * @param tag 用户标识
	 * @return  新ID
	 */
	public static long nextId(long tag){
		return instaIdGenerator.nextId(tag);
	}
}
