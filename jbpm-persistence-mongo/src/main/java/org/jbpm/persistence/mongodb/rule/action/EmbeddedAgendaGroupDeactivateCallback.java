package org.jbpm.persistence.mongodb.rule.action;

public class EmbeddedAgendaGroupDeactivateCallback extends EmbeddedWorkingMemoryAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ruleFlowGroupName;

	public EmbeddedAgendaGroupDeactivateCallback(String ruleFlowGroupName) {
		this.ruleFlowGroupName = ruleFlowGroupName;
	}
	
	public String getRuleFlowGroupName() {
		return ruleFlowGroupName;
	}
}
