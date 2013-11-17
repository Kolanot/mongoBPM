package org.jbpm.persistence.mongodb.rule.tms;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.common.EqualityKey;

public class EmbeddedEqualityKey implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int status;
	private final int handleId;
	private final List<Integer> otherHandles = new ArrayList<Integer>();
	private EmbeddedBeliefSet ebs; 
	
	public EmbeddedEqualityKey(EqualityKey key) {
		this.status = key.getStatus();
		this.handleId = key.getFactHandle().getId();
	}

	public int getStatus() {
		return status;
	}

	public int getHandleId() {
		return handleId;
	}

	public List<Integer> getOtherHandles() {
		return otherHandles;
	}

	public void addOtherHandle(int handleId) {
		this.otherHandles.add(handleId);
	}

	public EmbeddedBeliefSet getBeliefSet() {
		return ebs;
	}

	public void setBeliefSet(EmbeddedBeliefSet ebs) {
		this.ebs = ebs;
	}
	
	public boolean hasBeliefSet() {
		return ebs != null;
	}
}