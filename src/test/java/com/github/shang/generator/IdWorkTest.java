package com.github.shang.generator;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class IdWorkTest {

	 
    static class IdWorkThread implements Runnable {
        private Set<Long> set;
 
        public IdWorkThread(Set<Long> set) {
			super();
			this.set = set;
		}

		@Override
        public void run() {
            while (true) {
                long id = IdGenerator.nextId();
                if (!set.add(id)) {
                    System.out.println("duplicate1:" + id);
                    break;
                }
            }
        }
    }
 
    @Test
    public void test30s() {
        Set<Long> set = new HashSet<Long>();
        Thread t1 = new Thread(new IdWorkThread(set));
        Thread t2 = new Thread(new IdWorkThread(set));
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
