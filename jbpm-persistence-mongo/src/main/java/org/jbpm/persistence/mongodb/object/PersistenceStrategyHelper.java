package org.jbpm.persistence.mongodb.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.jbpm.persistence.mongodb.MongoSessionStore;

import org.mongodb.morphia.annotations.Entity;

public class PersistenceStrategyHelper {
	public static SessionObjectPersistenceStrategy getStrategy(MongoSessionStore store, Class<?> clazz) 
				throws ClassNotFoundException {
		if (clazz.isAnnotationPresent(Entity.class)) 
			return new MongoEntityPersistenceStrategy(store);
		List<SessionObjectPersistenceStrategy> strategies = store.getAllPersistenceStrategies();
		for (Iterator<SessionObjectPersistenceStrategy> itr = strategies.iterator(); itr.hasNext();) {
			SessionObjectPersistenceStrategy strategy = itr.next();
			if (strategy.accept(clazz)) return strategy;
		}
		return new SerializablePersistenceStrategy();
	}
	
	public static SessionObjectPersistenceStrategy getStrategy(MongoSessionStore store, 
			String strategyClassName) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Class.forName(strategyClassName);
		if (SessionObjectPersistenceStrategy.class.isAssignableFrom(clazz)) { 
			@SuppressWarnings("rawtypes")
			Constructor[] constructors = clazz.getConstructors();
			for (int i=0;i<constructors.length;i++) {
				Class<?>[] parameterTypes = constructors[i].getParameterTypes();
				if (parameterTypes.length == 0) // no parameters
					return (SessionObjectPersistenceStrategy)clazz.newInstance();
				if (parameterTypes.length == 1 && MongoSessionStore.class.equals(parameterTypes[0]))
					return (SessionObjectPersistenceStrategy)constructors[i].newInstance(store);
			}
		}
		return null;
	}
}
