package org.jbpm.persistence.mongodb.object;

public interface MongoSerializable extends java.io.Serializable {
	public String getSerializationStrategyClass();
	public java.io.Serializable getSerializedObject();
	public void setSerializedObject(java.io.Serializable object);
}
