package org.jbpm.persistence.mongodb.rule.tuple;

public class EmbeddedTuple {
	private final int sinkId;
	private int factHandleId;
	protected EmbeddedTuple(int sinkId) {
		this.sinkId = sinkId;
	}
	
	public int getSinkId() {
		return sinkId;
	}

	public int getFactHandleId() {
		return factHandleId;
	}

	public void setFactHandleId(int factHandleId) {
		this.factHandleId = factHandleId;
	}
}