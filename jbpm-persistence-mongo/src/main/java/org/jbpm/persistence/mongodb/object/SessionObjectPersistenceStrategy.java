package org.jbpm.persistence.mongodb.object;

import org.mongodb.morphia.annotations.Entity;

@Entity
public interface SessionObjectPersistenceStrategy {
    public boolean accept(Class<?> clazz);

    public MongoSerializable serialize(Object object);
    
    public Object deserialize(MongoSerializable value) throws ClassNotFoundException;
}
