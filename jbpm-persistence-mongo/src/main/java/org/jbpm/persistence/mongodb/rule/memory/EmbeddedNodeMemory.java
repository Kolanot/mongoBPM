package org.jbpm.persistence.mongodb.rule.memory;

public class EmbeddedNodeMemory implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int nodeId;
	public EmbeddedNodeMemory(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}
}
