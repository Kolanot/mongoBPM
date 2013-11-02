package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.ArrayList;
import java.util.List;

public class JoinNode extends EmbeddedTuple {
	private List<EmbeddedTuple> rightTuples = new ArrayList<EmbeddedTuple>();

	public JoinNode(int sinkId) {
		super(sinkId);
	}

	public List<EmbeddedTuple> getRightTuples() {
		return rightTuples;
	}
}
