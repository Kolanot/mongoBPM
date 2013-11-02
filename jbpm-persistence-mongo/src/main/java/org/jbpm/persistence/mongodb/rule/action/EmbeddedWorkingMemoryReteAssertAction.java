package org.jbpm.persistence.mongodb.rule.action;

import java.util.ArrayList;
import java.util.List;

public class EmbeddedWorkingMemoryReteAssertAction extends
		EmbeddedWorkingMemoryAction {
	private int factHandleId;
	private boolean removeLogical;
	private boolean updateEqualsMap;
	private String ruleOriginPackage;
	private String ruleOriginName;
	private List<Integer> leftTupleHandleIdList = new ArrayList<Integer>();

	public EmbeddedWorkingMemoryReteAssertAction() {
		super();
	}

	public int getFactHandleId() {
		return factHandleId;
	}

	public boolean isRemoveLogical() {
		return removeLogical;
	}

	public boolean isUpdateEqualsMap() {
		return updateEqualsMap;
	}

	public String getRuleOriginPackage() {
		return ruleOriginPackage;
	}

	public String getRuleOriginName() {
		return ruleOriginName;
	}

	public List<Integer> getLeftTupleHandleIdList() {
		return leftTupleHandleIdList;
	}

	public void setFactHandleId(int factHandleId) {
		this.factHandleId = factHandleId;
	}

	public void setRemoveLogical(boolean removeLogical) {
		this.removeLogical = removeLogical;
	}

	public void setUpdateEqualsMap(boolean updateEqualsMap) {
		this.updateEqualsMap = updateEqualsMap;
	}

	public void setRuleOriginPackage(String ruleOriginPackage) {
		this.ruleOriginPackage = ruleOriginPackage;
	}

	public void setRuleOriginName(String ruleOriginName) {
		this.ruleOriginName = ruleOriginName;
	}
}
