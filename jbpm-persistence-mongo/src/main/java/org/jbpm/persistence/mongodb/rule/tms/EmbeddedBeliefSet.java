package org.jbpm.persistence.mongodb.rule.tms;

import java.util.ArrayList;
import java.util.List;

public class EmbeddedBeliefSet implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int handleId;
	private final List<EmbeddedLogicalDependency> logicalDependencies = new ArrayList<EmbeddedLogicalDependency>();
	
	public EmbeddedBeliefSet(int handleId) {
		this.handleId = handleId;
	}
	
	public int getHandleId() {
		return handleId;
	}
	
	public List<EmbeddedLogicalDependency> getLogicalDependencies() {
		return logicalDependencies;
	}
	
	public void addLogicalDependency(EmbeddedLogicalDependency eld) {
		logicalDependencies.add(eld);
	}
	
	public boolean hasLogicalDependency() {
		return !logicalDependencies.isEmpty();
	}
}
