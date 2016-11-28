package com.github.shang.generator;

public class IdPair {
	private long smallestId;
	private long largestId;
	public long getSmallestId() {
		return smallestId;
	}
	public void setSmallestId(long smallestId) {
		this.smallestId = smallestId;
	}
	public long getLargestId() {
		return largestId;
	}
	public void setLargestId(long largestId) {
		this.largestId = largestId;
	}
	public IdPair(long smallestId, long largestId) {
		super();
		this.smallestId = smallestId;
		this.largestId = largestId;
	}
	
}
