package org.jbpm.persistence.mongodb.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.jbpm.persistence.mongodb.MongoSessionStore;

import org.mongodb.morphia.annotations.Entity;

public class PersistenceStrategyHelper {
	public static ProcessObjectPersistenceStrategy getStrategy(MongoSessionStore store, Class<?> clazz) 
				throws ClassNotFoundException {
		if (clazz.isAnnotationPresent(Entity.class)) 
			return new MongoEntityPersistenceStrategy(store);
		List<ProcessObjectPersistenceStrategy> strategies = store.getAllPersistenceStrategies();
		for (Iterator<ProcessObjectPersistenceStrategy> itr = strategies.iterator(); itr.hasNext();) {
			ProcessObjectPersistenceStrategy strategy = itr.next();
			if (strategy.accept(clazz)) return strategy;
		}
		return new SerializablePersistenceStrategy();
	}
	
	public static ProcessObjectPersistenceStrategy getStrategy(MongoSessionStore store, 
			String strategyClassName) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Class.forName(strategyClassName);
		if (ProcessObjectPersistenceStrategy.class.isAssignableFrom(clazz)) { 
			@SuppressWarnings("rawtypes")
			Constructor[] constructors = clazz.getConstructors();
			for (int i=0;i<constructors.length;i++) {
				Class<?>[] parameterTypes = constructors[i].getParameterTypes();
				if (parameterTypes.length == 0) // no parameters
					return (ProcessObjectPersistenceStrategy)clazz.newInstance();
				if (parameterTypes.length == 1 && MongoSessionStore.class.equals(parameterTypes[0]))
					return (ProcessObjectPersistenceStrategy)constructors[i].newInstance(store);
			}
		}
		return null;
	}
}
