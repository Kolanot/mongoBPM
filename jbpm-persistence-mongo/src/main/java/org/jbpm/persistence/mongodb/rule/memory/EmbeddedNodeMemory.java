package org.jbpm.persistence.mongodb.rule.memory;

public class EmbeddedNodeMemory {
	private final int nodeId;
	public EmbeddedNodeMemory(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
}
