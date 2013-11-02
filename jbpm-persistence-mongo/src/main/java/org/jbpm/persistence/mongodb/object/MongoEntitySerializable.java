package org.jbpm.persistence.mongodb.object;

import java.io.Serializable;

public class MongoEntitySerializable implements MongoSerializable {

	private MongoEntityId entityId;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MongoEntitySerializable(MongoEntityId entityId) {
		this.entityId = entityId;
	}
	
	@Override
	public String getSerializationStrategyClass() {
		return MongoEntityPersistenceStrategy.class.getName();
	}

	@Override
	public Serializable getSerializedObject() {
		return entityId;
	}

	@Override
	public void setSerializedObject(Serializable object) {
		this.entityId = (MongoEntityId)object;
	}

}
