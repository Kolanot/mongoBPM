package org.jbpm.persistence.mongodb.object;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.jbpm.persistence.mongodb.MongoSessionStore;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Polymorphic;
import org.mongodb.morphia.annotations.Transient;

@Polymorphic
public class MongoEntityPersistenceStrategy implements
		SessionObjectPersistenceStrategy, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Transient
	private final MongoSessionStore store;
	
	public MongoEntityPersistenceStrategy(MongoSessionStore store) {
		this.store = store;
	}
	
	@Override
	public boolean accept(Class<?> clazz) {
		return clazz.isAnnotationPresent(Entity.class);
	}

	@Override
	public MongoSerializable serialize(Object object) {
		Serializable idValue = null;
		if (!object.getClass().isAnnotationPresent(Entity.class)) return null;
		Class<?> clazz = object.getClass();
		String className = clazz.getName();
        do {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length && idValue == null; i++) {
                Field field = fields[i];
                Id id = field.getAnnotation(Id.class);
                if (id != null) {
                    try {
                        idValue = callIdMethod(object, "get"
                                + Character.toUpperCase(field.getName().charAt(0))
                                + field.getName().substring(1));
                    } catch (Exception e) {
                        try {
							idValue = (Serializable) field.get(object);
						} catch (IllegalArgumentException e1) {
							e1.printStackTrace();
						} catch (IllegalAccessException e1) {
							e1.printStackTrace();
						}
					}
                }
            }
        } while ((clazz = clazz.getSuperclass()) != null && idValue == null);
        return new MongoEntitySerializable(new MongoEntityId(idValue, className));
	}

	@Override
	public Object deserialize(MongoSerializable value) throws ClassNotFoundException {
		if (!(value instanceof MongoEntitySerializable)) return null;
		MongoEntityId entityId = (MongoEntityId)((MongoEntitySerializable)value).getSerializedObject();
        Object entity = store.getMongoEntity(Class.forName(entityId.getClassName()), entityId.getId());
		return entity;
	}

    private static Serializable callIdMethod(Object target, String methodName) throws IllegalArgumentException,
    SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	return (Serializable) target.getClass().getMethod(methodName, (Class[]) null).invoke(target, new Object[]{});
    }
}
