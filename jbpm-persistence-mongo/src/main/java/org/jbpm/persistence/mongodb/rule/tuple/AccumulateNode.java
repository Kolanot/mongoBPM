package org.jbpm.persistence.mongodb.rule.tuple;
import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.mongodb.rule.EmbeddedInternalFactHandle;
public class AccumulateNode extends EmbeddedTuple {

	private EmbeddedInternalFactHandle accctxFactHandle;
	private Object accumulateContext;
	private Boolean propagated;
	private List<EmbeddedTuple> childTuples = new ArrayList<EmbeddedTuple>();
	
	public AccumulateNode(int sinkId, 
			EmbeddedInternalFactHandle accctxFactHandle,
			Object accumulateContext,
			Boolean propagated) {
		super(sinkId);
		this.accctxFactHandle = accctxFactHandle;
		this.accumulateContext = accumulateContext;
		this.propagated = propagated;
	}

	public EmbeddedInternalFactHandle getAccctxFactHandle() {
		return accctxFactHandle;
	}

	public Object getAccumulateContext() {
		return accumulateContext;
	}

	public Boolean getPropagated() {
		return propagated;
	}

	public List<EmbeddedTuple> getChildTuples() {
		return childTuples;
	}
	
}
