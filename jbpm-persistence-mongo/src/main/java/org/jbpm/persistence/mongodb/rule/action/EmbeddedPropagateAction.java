package org.jbpm.persistence.mongodb.rule.action;

public class EmbeddedPropagateAction extends EmbeddedWorkingMemoryAction {
	private static final long serialVersionUID = 1L;
	private int propagationQueuingNodeId;

	public EmbeddedPropagateAction(int propagationQueuingNodeId) {
		super();
		this.propagationQueuingNodeId = propagationQueuingNodeId;
	}

	public int getPropagationQueuingNodeId() {
		return propagationQueuingNodeId;
	}
}
