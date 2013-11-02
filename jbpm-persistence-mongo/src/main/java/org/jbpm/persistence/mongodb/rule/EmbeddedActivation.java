package org.jbpm.persistence.mongodb.rule;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.reteoo.LeftTuple;

import org.mongodb.morphia.annotations.Embedded;

@Embedded 
public class EmbeddedActivation {
	private long activationNumber;
	private int leftTuple;
	private int salience;
	private String rulePackage;
	private String ruleName;
	private long propagationNumber;
	private String activationGroupName;
	private boolean queued;
	private boolean evaluated;
	private int factHandleId;
	private final List<Integer> logicalDependencyList = new ArrayList<Integer>();
	private final List<Integer> tupleFactHandleList = new ArrayList<Integer>();
	public long getActivationNumber() {
		return activationNumber;
	}
	public int getLeftTuple() {
		return leftTuple;
	}
	public int getSalience() {
		return salience;
	}
	public String getRulePackage() {
		return rulePackage;
	}
	public String getRuleName() {
		return ruleName;
	}
	public long getPropagationNumber() {
		return propagationNumber;
	}
	public String getActivationGroupName() {
		return activationGroupName;
	}
	public boolean isQueued() {
		return queued;
	}
	public int getFactHandleId() {
		return factHandleId;
	}
	public List<Integer> getLogicalDependencyList() {
		return logicalDependencyList;
	}
	public List<Integer> getTupleFactHandleList() {
		return tupleFactHandleList;
	}
	public int[] getTupleFactHandleArray() {
		int i = 0;
		int[] ret = new int[tupleFactHandleList.size()];
		for (int handleId: tupleFactHandleList) {
			ret[i++] = handleId;
		}
		return ret;
	}
	public void setActivationNumber(long activationNumber) {
		this.activationNumber = activationNumber;
	}
	public void setLeftTuple(int leftTuple) {
		this.leftTuple = leftTuple;
	}
	public void setSalience(int salience) {
		this.salience = salience;
	}
	public void setRulePackage(String rulePackage) {
		this.rulePackage = rulePackage;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public void setPropagationNumber(long propagationNumber) {
		this.propagationNumber = propagationNumber;
	}
	public void setActivationGroupName(String activationGroupName) {
		this.activationGroupName = activationGroupName;
	}
	public void setQueued(boolean queued) {
		this.queued = queued;
	}
	public void setFactHandleId(int factHandleId) {
		this.factHandleId = factHandleId;
	}
	public boolean isEvaluated() {
		return evaluated;
	}
	public void setEvaluated(boolean evaluated) {
		this.evaluated = evaluated;
	}
	public void addLogicalDependency(int factHandleId) {
		this.logicalDependencyList.add(factHandleId);
	}
	public void addTupleFactHandleId(int handleId) {
		this.tupleFactHandleList.add(handleId);
	}
	public void addLeftTuple(LeftTuple leftTuple) {
		populateFactTupleHandleList(leftTuple, this.tupleFactHandleList);
	}

	// this is also used by MongoActivationKey
	public static void populateFactTupleHandleList(final LeftTuple leftTuple, List<Integer> tupleFactHandleList) {
        if( leftTuple != null ) {
            for( LeftTuple entry = leftTuple; entry != null; entry = entry.getParent() ) {
                tupleFactHandleList.add(entry.getLastHandle().getId());
            }
        }
    }
}
