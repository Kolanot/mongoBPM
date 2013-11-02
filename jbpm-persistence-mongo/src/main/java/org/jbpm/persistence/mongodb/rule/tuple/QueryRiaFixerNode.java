package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.ArrayList;
import java.util.List;

public class QueryRiaFixerNode extends EmbeddedTuple {
	private List<EmbeddedTuple> leftTuples = new ArrayList<EmbeddedTuple>();

	public QueryRiaFixerNode(int sinkId) {
		super(sinkId);
	}

	public List<EmbeddedTuple> getLeftTuples() {
		return leftTuples;
	}
}
