package org.jbpm.persistence.mongodb.rule.action;

public class EmbeddedRuleFlowGroupDeactivateCallback extends EmbeddedWorkingMemoryAction {
	private static final long serialVersionUID = 1L;
	private String ruleFlowGroupName;

	public EmbeddedRuleFlowGroupDeactivateCallback(String ruleFlowGroupName) {
		this.ruleFlowGroupName = ruleFlowGroupName;
	}
	
	public String getRuleFlowGroupName() {
		return ruleFlowGroupName;
	}
}
