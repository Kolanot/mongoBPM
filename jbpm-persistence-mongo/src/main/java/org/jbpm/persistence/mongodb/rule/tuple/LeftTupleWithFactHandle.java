package org.jbpm.persistence.mongodb.rule.tuple;

import org.jbpm.persistence.mongodb.rule.EmbeddedInternalFactHandle;

public class LeftTupleWithFactHandle extends EmbeddedTuple {
	private static final long serialVersionUID = 1L;
	private EmbeddedInternalFactHandle lastHandle;
	
	public LeftTupleWithFactHandle(int sinkId, EmbeddedInternalFactHandle lastHandle) {
		super(sinkId);
		this.lastHandle = lastHandle;
	}

	public EmbeddedInternalFactHandle getLastHandle() {
		return lastHandle;
	}

}
