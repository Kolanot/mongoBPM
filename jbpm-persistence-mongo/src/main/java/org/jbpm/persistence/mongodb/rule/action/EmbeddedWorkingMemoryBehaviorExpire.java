package org.jbpm.persistence.mongodb.rule.action;

public class EmbeddedWorkingMemoryBehaviorExpire  extends EmbeddedWorkingMemoryAction {
	private static final long serialVersionUID = 1L;
    private final int nodeId;

	public EmbeddedWorkingMemoryBehaviorExpire(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
}