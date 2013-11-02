package org.jbpm.persistence.mongodb.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.mongodb.rule.tuple.EmbeddedTuple;

import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class EmbeddedInternalFactHandle {
	private int type;
	private int id;
	private long recency;
    private long startTimestamp;
    private long duration;
    private boolean expired;
    private long activationsCount;
    private String objectClassName;
    private Serializable objectClassIdValue;
    private List<Integer> rightTupleIds = new ArrayList<Integer>();  
    private List<EmbeddedTuple> leftTuples = new ArrayList<EmbeddedTuple>();
	
    public EmbeddedInternalFactHandle(int type, int id, long recency,
			long startTimestamp, long duration, boolean expired,
			long activationsCount, String objectClassName,
			Serializable objectClassIdValue) {
		super();
		this.type = type;
		this.id = id;
		this.recency = recency;
		this.startTimestamp = startTimestamp;
		this.duration = duration;
		this.expired = expired;
		this.activationsCount = activationsCount;
		this.objectClassName = objectClassName;
		this.objectClassIdValue = objectClassIdValue;
	}

	public int getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public long getRecency() {
		return recency;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public long getDuration() {
		return duration;
	}

	public boolean isExpired() {
		return expired;
	}

	public long getActivationsCount() {
		return activationsCount;
	}

	public String getObjectClassName() {
		return objectClassName;
	}

	public Serializable getObjectClassIdValue() {
		return objectClassIdValue;
	}

	public List<Integer> getRightTupleIds() {
		return rightTupleIds;
	}

	public List<EmbeddedTuple> getLeftTuples() {
		return leftTuples;
	}
}