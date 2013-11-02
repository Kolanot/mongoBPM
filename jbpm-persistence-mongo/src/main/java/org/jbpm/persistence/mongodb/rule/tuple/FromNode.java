package org.jbpm.persistence.mongodb.rule.tuple;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.mongodb.rule.EmbeddedInternalFactHandle;

public class FromNode extends EmbeddedTuple {
	private List<EmbeddedInternalFactHandle> matchedFactHandles = new ArrayList<EmbeddedInternalFactHandle>();
	private List<EmbeddedTuple> rightTuples = new ArrayList<EmbeddedTuple>();

	public FromNode(int sinkId) {
		super(sinkId);
	}

	public List<EmbeddedInternalFactHandle> getMatchedFactHandles() {
		return matchedFactHandles;
	}
	public List<EmbeddedTuple> getRightTuples() {
		return rightTuples;
	}
}