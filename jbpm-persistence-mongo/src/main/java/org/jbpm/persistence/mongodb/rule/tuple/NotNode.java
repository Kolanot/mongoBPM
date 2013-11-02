package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.ArrayList;
import java.util.List;

public class NotNode extends EmbeddedTuple {
	private List<EmbeddedTuple> notBlockedTuples;

	public NotNode(int sinkId, boolean leftTupleNotBlocked) {
		super(sinkId);
		if (leftTupleNotBlocked) notBlockedTuples = new ArrayList<EmbeddedTuple>();
	}

	public List<EmbeddedTuple> getNotBlockedTuples() {
		return notBlockedTuples;
	}
	
	public boolean isLeftTupleNotBlocked() {
		return notBlockedTuples != null;
	}
}
