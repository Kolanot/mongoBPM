package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.List;

import org.jbpm.persistence.mongodb.rule.EmbeddedInternalFactHandle;

public class QueryTerminalNode extends EmbeddedTuple {
	private final EmbeddedInternalFactHandle factHandle;
	private List<LeftTupleWithFactHandle> leftTuples;

	protected QueryTerminalNode(int sinkId, EmbeddedInternalFactHandle factHandle) {
		super(sinkId);
		this.factHandle = factHandle;
	}

	public EmbeddedInternalFactHandle getFactHandle() {
		return factHandle;
	}
	
	public List<LeftTupleWithFactHandle> getLeftTuples() {
		return leftTuples;
	}
}
