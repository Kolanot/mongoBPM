package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.mongodb.rule.EmbeddedInternalFactHandle;

public class UnificationNode extends EmbeddedTuple {
	private boolean openQuery; 
	private EmbeddedInternalFactHandle factHandle;
	private List<LeftTupleWithFactHandle> leftTuples;
	
	public UnificationNode(int sinkId, boolean openQuery, EmbeddedInternalFactHandle factHandle) {
		super(sinkId);
		this.openQuery = openQuery;
		this.factHandle = factHandle;
		if (!openQuery) 
			leftTuples = new ArrayList<LeftTupleWithFactHandle>();
	}

	public boolean isOpenQuery() {
		return openQuery;
	}

	public EmbeddedInternalFactHandle getFactHandle() {
		return factHandle;
	}
	
	public List<LeftTupleWithFactHandle> getLeftTuples() {
		return leftTuples;
	}
}
