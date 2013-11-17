package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.ArrayList;
import java.util.List;

public class RightInputAdaterNode extends EmbeddedTuple {
	private static final long serialVersionUID = 1L;
	private int internalFactHandleId;
	private long recency;
    private List<Integer> rightTupleIds = new ArrayList<Integer>();
    
	public RightInputAdaterNode(int sinkId, int id, long recency) {
		super(sinkId);
		this.internalFactHandleId = id; 
	}
	public int getInternalFactHandleId() {
		return internalFactHandleId;
	}
	public long getRecency() {
		return recency;
	}
	public List<Integer> getRightTupleIds() {
		return rightTupleIds;
	}

}
