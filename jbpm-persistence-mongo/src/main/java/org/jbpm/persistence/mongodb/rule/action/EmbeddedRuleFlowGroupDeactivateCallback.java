package org.jbpm.persistence.mongodb.rule.action;

public class EmbeddedRuleFlowGroupDeactivateCallback extends EmbeddedWorkingMemoryAction {
	private String ruleFlowGroupName;

	public EmbeddedRuleFlowGroupDeactivateCallback(String ruleFlowGroupName) {
		this.ruleFlowGroupName = ruleFlowGroupName;
	}
	
	public String getRuleFlowGroupName() {
		return ruleFlowGroupName;
	}
}
