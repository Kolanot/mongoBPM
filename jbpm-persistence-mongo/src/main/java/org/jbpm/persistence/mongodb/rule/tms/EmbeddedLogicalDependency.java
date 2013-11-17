package org.jbpm.persistence.mongodb.rule.tms;

import org.jbpm.persistence.mongodb.object.MongoSerializable;
import org.jbpm.persistence.mongodb.rule.EmbeddedActivation;

public class EmbeddedLogicalDependency implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private EmbeddedActivation ea;
	private MongoSerializable objectRef;
	private MongoSerializable valueRef;

	public EmbeddedActivation getActivation() {
		return ea;
	}

	public void setActivation(EmbeddedActivation ea) {
		this.ea = ea;
	}

	public MongoSerializable getObjectRef() {
		return objectRef;
	}

	public MongoSerializable getValueRef() {
		return valueRef;
	}

	public void setObjectRef(MongoSerializable objectRef) {
		this.objectRef = objectRef;
	}

	public void setValueRef(MongoSerializable valueRef) {
		this.valueRef = valueRef;
	}
}
