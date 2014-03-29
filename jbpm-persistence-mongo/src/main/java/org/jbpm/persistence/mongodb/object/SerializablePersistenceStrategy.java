package org.jbpm.persistence.mongodb.object;

import java.io.Serializable;

import org.mongodb.morphia.annotations.Polymorphic;

@Polymorphic
public class SerializablePersistenceStrategy implements
		ProcessObjectPersistenceStrategy, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean accept(Class<?> clazz) {
		return Serializable.class.isAssignableFrom(clazz);  
	}

	@Override
	public MongoSerializable serialize(Object object) {
		return (object instanceof Serializable)? new MongoJavaSerializable((Serializable)object):null;
	}

	@Override
	public Object deserialize(MongoSerializable value) throws ClassNotFoundException {
		return value.getSerializedObject();
	}
}
