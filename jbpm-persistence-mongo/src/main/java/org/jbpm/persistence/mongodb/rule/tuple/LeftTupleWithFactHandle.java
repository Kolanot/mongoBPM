package org.jbpm.persistence.mongodb.rule.tuple;

import org.jbpm.persistence.mongodb.rule.EmbeddedInternalFactHandle;

public class LeftTupleWithFactHandle extends EmbeddedTuple {
	private EmbeddedInternalFactHandle lastHandle;
	
	public LeftTupleWithFactHandle(int sinkId, EmbeddedInternalFactHandle lastHandle) {
		super(sinkId);
		this.lastHandle = lastHandle;
	}

	public EmbeddedInternalFactHandle getLastHandle() {
		return lastHandle;
	}

}
