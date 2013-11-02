package org.jbpm.persistence.mongodb.object;

import java.io.Serializable;

import org.mongodb.morphia.annotations.Serialized;

public class MongoEntityId implements Serializable {
	private static final long serialVersionUID = 1L;

	@Serialized
	private final Serializable id;
	private final String className;
	
	public MongoEntityId(Serializable id, String className) {
		super();
		this.id = id;
		this.className = className;
	}

	public Serializable getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}
}
