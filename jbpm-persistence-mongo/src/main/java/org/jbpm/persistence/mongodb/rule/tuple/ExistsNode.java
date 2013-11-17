package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.ArrayList;
import java.util.List;

public class ExistsNode extends EmbeddedTuple {
	private static final long serialVersionUID = 1L;
	private List<EmbeddedTuple> leftTuples;

	public ExistsNode(int sinkId, boolean leftTupleBlocked) {
		super(sinkId);
		if (leftTupleBlocked) leftTuples = new ArrayList<EmbeddedTuple>();
	}

	public List<EmbeddedTuple> getLeftTuples() {
		return leftTuples;
	}
	
	public boolean isLeftTupleBlocked() {
		return leftTuples != null;
	}
}
