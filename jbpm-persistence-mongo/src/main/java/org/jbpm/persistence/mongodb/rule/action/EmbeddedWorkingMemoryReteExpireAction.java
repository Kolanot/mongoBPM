package org.jbpm.persistence.mongodb.rule.action;

public class EmbeddedWorkingMemoryReteExpireAction extends
		EmbeddedWorkingMemoryAction {
	private static final long serialVersionUID = 1L;
	private int objectTypeNodeId;
	private int factHandelId;
	
	public EmbeddedWorkingMemoryReteExpireAction(int objectTypeNodeId,
			int factHandelId) {
		super();
		this.objectTypeNodeId = objectTypeNodeId;
		this.factHandelId = factHandelId;
	}

	public int getObjectTypeNodeId() {
		return objectTypeNodeId;
	}

	public int getFactHandelId() {
		return factHandelId;
	}
}
