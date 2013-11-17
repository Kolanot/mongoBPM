package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.ArrayList;
import java.util.List;

public class EvalConditionNode extends EmbeddedTuple {
	private static final long serialVersionUID = 1L;
	private List<EmbeddedTuple> leftTuples = new ArrayList<EmbeddedTuple>();

	public EvalConditionNode(int sinkId) {
		super(sinkId);
	}

	public List<EmbeddedTuple> getLeftTuples() {
		return leftTuples;
	}
}
