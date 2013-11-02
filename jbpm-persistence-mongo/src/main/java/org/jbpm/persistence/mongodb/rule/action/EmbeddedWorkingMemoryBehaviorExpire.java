package org.jbpm.persistence.mongodb.rule.action;

public class EmbeddedWorkingMemoryBehaviorExpire  extends EmbeddedWorkingMemoryAction {
    private final int nodeId;

	public EmbeddedWorkingMemoryBehaviorExpire(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
}