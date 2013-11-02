package org.jbpm.persistence.mongodb.rule.action;

import org.jbpm.persistence.mongodb.rule.EmbeddedActivation;

import org.mongodb.morphia.annotations.Embedded;
@Embedded
public class EmbeddedLogicalRetractCallback extends EmbeddedWorkingMemoryAction {
	private final int handleId;
	private final boolean fullyRetract;
	private final boolean update;
	@Embedded
	private EmbeddedActivation activation;
	
	public EmbeddedLogicalRetractCallback(int handleId, boolean fullyRetract,
			boolean update) {
		super();
		this.handleId = handleId;
		this.fullyRetract = fullyRetract;
		this.update = update;
	}

	public int getHandleId() {
		return handleId;
	}

	public boolean isFullyRetract() {
		return fullyRetract;
	}

	public boolean hasUpdate() {
		return update;
	}

	public EmbeddedActivation getActivation() {
		return activation;
	}

	public void setActivation(EmbeddedActivation activation) {
		this.activation = activation;
	}
}
