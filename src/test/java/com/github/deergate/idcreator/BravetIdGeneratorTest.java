/* 
 * Copyright 2022 Shang Yehua <niceshang@outlook.com>
 */
package com.github.deergate.idcreator;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 *
 * TODO BravetIdGeneratorTest说明
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2022-03-02  16:06
 *
 */
public class BravetIdGeneratorTest {
    
	public static void main(String[] args) throws InterruptedException {
		BravetIdGenerator creator = new BravetIdGenerator(0, 0);
		int totalThreads = 100;
		HashSet<Long>[] sets1 = new HashSet[totalThreads];
		HashSet<Long>[] sets2 = new HashSet[totalThreads];
		HashSet<Long>[] sets3 = new HashSet[totalThreads];

		CountDownLatch latch = new CountDownLatch(totalThreads);
		CyclicBarrier barrier = new CyclicBarrier(totalThreads + 1);

		boolean[] runs1 = new boolean[sets1.length];
		boolean[] runs2 = new boolean[sets2.length];
		boolean[] runs3 = new boolean[sets3.length];

		for (int i = 0; i < runs1.length; i++) {
			runs1[i] = true;
			runs2[i] = true;
			runs3[i] = true;
		}

		for (int i = 0; i < totalThreads; i++) {
			int cursor = i;
			new Thread(() -> {
				HashSet<Long> idSet = new HashSet<>();
				sets1[cursor] = idSet;
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
				while (runs1[cursor]) {
					idSet.add(creator.nextId());
				}

				idSet = new HashSet<>();
				sets2[cursor] = idSet;
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
				while (runs2[cursor]) {
					idSet.add(creator.nextId());
				}

				idSet = new HashSet<>();
				sets3[cursor] = idSet;
				try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
				while (runs3[cursor]) {
					idSet.add(creator.nextId());
				}

				latch.countDown();
			}).start();
		}
		new Thread(() -> {
			try {
				barrier.await();
				TimeUnit.SECONDS.sleep(1);
				for (int i = 0; i < runs1.length; i++) {
					runs1[i] = false;
				}

				barrier.await();
				TimeUnit.SECONDS.sleep(1);
				for (int i = 0; i < runs2.length; i++) {
					runs2[i] = false;
				}

				barrier.await();
				TimeUnit.SECONDS.sleep(1);
				for (int i = 0; i < runs3.length; i++) {
					runs3[i] = false;
				}
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
		}).start();
		latch.await();
		Set<Long> finalSet = new HashSet<>();
		for (HashSet<Long> hashSet : sets1) {
			finalSet.addAll(hashSet);
		}
		System.out.println(finalSet.size());

		finalSet = new HashSet<>();
		for (HashSet<Long> hashSet : sets2) {
			finalSet.addAll(hashSet);
		}
		System.out.println(finalSet.size());

		finalSet = new HashSet<>();
		for (HashSet<Long> hashSet : sets3) {
			finalSet.addAll(hashSet);
		}
		System.out.println(finalSet.size());
	}

}
