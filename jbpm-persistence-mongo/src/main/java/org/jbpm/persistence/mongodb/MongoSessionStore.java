package org.jbpm.persistence.mongodb;

import java.util.List;

import org.jboss.logging.Logger;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationProperty;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceId;
import org.jbpm.persistence.mongodb.object.ProcessObjectPersistenceStrategy;
import org.jbpm.persistence.mongodb.session.MongoSessionId;
import org.jbpm.persistence.mongodb.session.MongoSessionInfo;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemId;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationProperty;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.mongodb.MongoClient;

public class MongoSessionStore {
	private static final Logger log = Logger.getLogger(MongoSessionStore.class); 
	
	public static String envKey = "MongoSessionStore";
	static String dbName = "BPM"; 
	Datastore ds; 
	Morphia morphia;
	MongoClient mongo;
	
	public MongoSessionStore() {
		try {
			// Mongo singleton, remote connection to DB server
			mongo = MongoSingleton.INSTANCE.getMongo();
			morphia = new Morphia();
			morphia.map(MongoSessionInfo.class);
			ds= morphia.createDatastore(mongo, dbName);
			if (ds.get(MongoSessionId.class, MongoSessionId.sessionId) == null)
				ds.save(new MongoSessionId());
			if (ds.get(MongoWorkItemId.class, MongoWorkItemId.workItemId) == null)
				ds.save(new MongoWorkItemId());
			if (ds.get(MongoProcessInstanceId.class, MongoProcessInstanceId.processsInstanceId) == null)
				ds.save(new MongoProcessInstanceId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveNew(MongoSessionInfo sessionInfo) {
		sessionInfo.setId(getNextSessionId());
		ds.save(sessionInfo);
		sessionInfo.setModifiedSinceLastSave(false);
	}

	public void saveOrUpdate(MongoSessionInfo sessionInfo) {
		if (sessionInfo.getId() == 0) {
			sessionInfo.setId(getNextSessionId());
		}
		ds.save(sessionInfo);
		sessionInfo.setModifiedSinceLastSave(false);
	}

	public int getNextSessionId() {
		Query<MongoSessionId> query = ds.find(MongoSessionId.class);
		UpdateOperations<MongoSessionId> ops = ds.createUpdateOperations(MongoSessionId.class).inc("seq");
		MongoSessionId sessionId = ds.findAndModify(query, ops);
		return sessionId.getSeq();
	}

	public long getNextProcessInstanceId() {
		Query<MongoProcessInstanceId> query = ds.find(MongoProcessInstanceId.class);
		UpdateOperations<MongoProcessInstanceId> ops = ds.createUpdateOperations(MongoProcessInstanceId.class).inc("seq");
		MongoProcessInstanceId instanceId = ds.findAndModify(query, ops);
		return instanceId.getSeq();
	}

	public long getNextWorkItemId() {
		Query<MongoWorkItemId> query = ds.find(MongoWorkItemId.class);
		UpdateOperations<MongoWorkItemId> ops = ds.createUpdateOperations(MongoWorkItemId.class).inc("seq");
		MongoWorkItemId workItemId = ds.findAndModify(query, ops);
		return workItemId.getSeq();
	}

	public List<MongoSessionInfo> findAllSessions() {
		return ds.find(MongoSessionInfo.class).asList();
	}

	public MongoSessionInfo findSessionInfo(int sessionId) {
		return ds.get(MongoSessionInfo.class, sessionId);
	}

	public MongoSessionInfo findSessionInfoByProcessInstanceId(long processInstanceId) {
		try {
			return ds.find(MongoSessionInfo.class).disableValidation().filter("processData.processInstances.processInstanceId", processInstanceId).get();
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public MongoSessionInfo findSessionInfoByProcessCorrelationKey(CorrelationKey corKey) {
		String keyPrefix = "processData.processInstances.correlationKey";
		MongoCorrelationKey mongoKey = new MongoCorrelationKey(corKey);
		Query<MongoSessionInfo> q = ds.createQuery(MongoSessionInfo.class).disableValidation();
		if (mongoKey.getName() == null) {
			q = q.filter(keyPrefix+".name exists", false);
		} else {
			q = q.filter(keyPrefix+".name", mongoKey.getName());
		}
		for (CorrelationProperty<?> property:mongoKey.getProperties()) {
			MongoCorrelationProperty p = (MongoCorrelationProperty)property;
			if (p.getName() == null) {
				q = q.filter(keyPrefix+".properties.name exists", false);
			} else {
				q = q.filter(keyPrefix+".properties.name", p.getName());
			}
			if (p.getValue() == null) {
				q = q.filter(keyPrefix+".properties.value exists", false);
			} else {
				q = q.filter(keyPrefix+".properties.value", p.getValue());
			}
		}
		log.info("query = " + q.getQueryObject().toString());
		MongoSessionInfo sessionInfo = q.get();
		return sessionInfo;
	}
	
	public MongoSessionInfo findSessionInfoByProcessEvent(String eventType) {
		MongoSessionInfo sessionInfo = ds.find(MongoSessionInfo.class).filter("processData.processinInstances.eventTypes elem", eventType).get();
		return sessionInfo;
	}
	
	public void removeSessionInfo(Long id) {
		MongoSessionInfo session = ds.get(MongoSessionInfo.class, id);
		removeSessionInfo(session);
	}
	
	public void removeSessionInfo(MongoSessionInfo session) {
		if (session != null) {
			ds.delete(session);
		}
	}

	public void removeSessions() {
		ds.delete(ds.find(MongoSessionInfo.class));
	}
	
	public MongoSessionInfo findSessionByWorkItemId (long workItemId) {
		MongoSessionInfo sessionInfo = ds.find(MongoSessionInfo.class).disableValidation().filter("workItems." + workItemId+".id", workItemId).get();
		return sessionInfo;
	}
	
	public List<MongoSessionInfo> getAllSessions() {
		return ds.find(MongoSessionInfo.class).asList();
	}
	
	public Object getMongoEntity(Class<?> entityClass, Object id) {
		return ds.get(entityClass, id);
	}
	
	public List<ProcessObjectPersistenceStrategy> getAllPersistenceStrategies() {
		return ds.find(ProcessObjectPersistenceStrategy.class).asList();
	}
}
