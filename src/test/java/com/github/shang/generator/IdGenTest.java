package com.github.shang.generator;

import org.junit.Test;

public class IdGenTest {

	@Test
	public void testIdPair(){
		IdPair pair=IdGenerator.getIdPairOfDay(2016, 10, 24);
		System.out.println("min:"+pair.getSmallestId());
		System.out.println("max:"+pair.getLargestId());
	}
}
