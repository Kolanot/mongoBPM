package org.jbpm.persistence.mongodb.rule.tuple;

public class NonePropagationRightTuple extends EmbeddedTuple {
	private final int handleId; 
	public NonePropagationRightTuple(int sinkId, int handleId) {
		super(sinkId);
		this.handleId = handleId;
	}
	
	public int getHandleId() {
		return handleId;
	}
}
