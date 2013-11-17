package org.jbpm.persistence.mongodb.rule;

import org.drools.core.common.EqualityKey;
import org.drools.core.common.InternalFactHandle;
import org.jbpm.persistence.mongodb.object.MongoSerializable;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class EmbeddedFactHandle  implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private long recency;
	private boolean isJustified;
	private String persistentStrategyClass;
	@Embedded
	private MongoSerializable objectRef;

	public EmbeddedFactHandle() {}
	
	public EmbeddedFactHandle(InternalFactHandle ifh) {
		super();
		this.id = ifh.getId();
		this.recency = ifh.getRecency();
		if (ifh.getEqualityKey() != null
				&& ifh.getEqualityKey().getStatus() == EqualityKey.JUSTIFIED) {
			this.isJustified = true;
		} else {
			this.isJustified = false;
		}
	}

	public int getId() {
		return id;
	}

	public long getRecency() {
		return recency;
	}
	
	public boolean isJustified() {
		return isJustified;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public void setRecency(long recency) {
		this.recency = recency;
	}

	public void setJustified(boolean isJustified) {
		this.isJustified = isJustified;
	}

	public MongoSerializable getObjectRef() {
		return objectRef;
	}

	public void setObjectRef(MongoSerializable objectRef) {
		this.objectRef = objectRef;
	}

	public String getPersistentStrategyClass() {
		return persistentStrategyClass;
	}

	public void setPersistentStrategyClass(String persistentStrategyClass) {
		this.persistentStrategyClass = persistentStrategyClass;
	}
}