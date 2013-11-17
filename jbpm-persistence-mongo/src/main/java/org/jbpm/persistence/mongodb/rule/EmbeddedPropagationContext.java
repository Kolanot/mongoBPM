package org.jbpm.persistence.mongodb.rule;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class EmbeddedPropagationContext implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int type;
	private String ruleOriginPackage;
	private String ruleOriginName;
	private int leftTupleOrigin;
	private long propagationNumber;
	private int factHandleOriginId;
	private String entryPointId;
	
	public int getType() {
		return type;
	}
	public String getRuleOriginPackage() {
		return ruleOriginPackage;
	}
	public String getRuleOriginName() {
		return ruleOriginName;
	}
	public int getLeftTupleOrigin() {
		return leftTupleOrigin;
	}
	public long getPropagationNumber() {
		return propagationNumber;
	}
	public int getFactHandleOriginId() {
		return factHandleOriginId;
	}
	public String getEntryPointId() {
		return entryPointId;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	public void setRuleOriginPackage(String ruleOriginPackage) {
		this.ruleOriginPackage = ruleOriginPackage;
	}
	public void setRuleOriginName(String ruleOriginName) {
		this.ruleOriginName = ruleOriginName;
	}
	public void setLeftTupleOrigin(int leftTupleOrigin) {
		this.leftTupleOrigin = leftTupleOrigin;
	}
	public void setPropagationNumber(long propagationNumber) {
		this.propagationNumber = propagationNumber;
	}
	public void setFactHandleOriginId(int factHandleOriginId) {
		this.factHandleOriginId = factHandleOriginId;
	}
	public void setEntryPointId(String entryPointId) {
		this.entryPointId = entryPointId;
	}
}
