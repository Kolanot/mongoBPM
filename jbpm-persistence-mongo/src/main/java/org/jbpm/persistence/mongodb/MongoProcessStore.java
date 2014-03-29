package org.jbpm.persistence.mongodb;

import java.io.NotSerializableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationProperty;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceId;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceMarshaller;
import org.jbpm.persistence.mongodb.object.ProcessObjectPersistenceStrategy;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemId;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationProperty;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import com.mongodb.MongoClient;

public class MongoProcessStore {
	private static final Logger log = Logger.getLogger(MongoProcessStore.class); 
	
	public static String envKey = "MongoProcessStore";
	static String dbName = "BPM"; 
	Datastore ds; 
	Morphia morphia;
	MongoClient mongo;
	enum StoreAction {SAVE, DELETE};
	
	Map<StoreAction, MongoProcessInstanceInfo> tobeCommitted = new HashMap<StoreAction, MongoProcessInstanceInfo>();
	
	public MongoProcessStore() {
		try {
			// Mongo singleton, remote connection to DB server
			mongo = MongoSingleton.INSTANCE.getMongo();
			morphia = new Morphia();
			morphia.map(MongoProcessInstanceInfo.class);
			ds= morphia.createDatastore(mongo, dbName);
			if (ds.get(MongoWorkItemId.class, MongoWorkItemId.workItemId) == null)
				ds.save(new MongoWorkItemId());
			if (ds.get(MongoProcessInstanceId.class, MongoProcessInstanceId.processsInstanceId) == null)
				ds.save(new MongoProcessInstanceId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveNew(MongoProcessInstanceInfo instanceInfo) {
		saveOrUpdate(instanceInfo);
	}

	public void saveOrUpdate(MongoProcessInstanceInfo instanceInfo) {
		if (instanceInfo.getProcessInstanceId() == 0) {
			instanceInfo.setProcessInstanceId(getNextProcessInstanceId());
		}
		tobeCommitted.put(StoreAction.SAVE, instanceInfo);
	}

	public void commit() {
		for (Map.Entry<StoreAction, MongoProcessInstanceInfo> entry: tobeCommitted.entrySet()) {
			MongoProcessInstanceInfo procInstInfo = entry.getValue();
			if (entry.getKey() == StoreAction.SAVE) {
				try {
					MongoProcessInstanceMarshaller.serialize(procInstInfo);
				} catch (NotSerializableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ds.save(procInstInfo);
			} else if (entry.getKey() == StoreAction.DELETE) 
				ds.delete(entry.getValue());
		}
		tobeCommitted.clear();
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

	public List<MongoProcessInstanceInfo> findAllProcessInstances() {
		return ds.find(MongoProcessInstanceInfo.class).asList();
	}

	public MongoProcessInstanceInfo findProcessInstanceInfo(long id) {
		return ds.get(MongoProcessInstanceInfo.class, id);
	}

	public MongoProcessInstanceInfo findProcessInstanceInfoByProcessCorrelationKey(CorrelationKey corKey) {
		String keyPrefix = "correlationKey";
		MongoCorrelationKey mongoKey = new MongoCorrelationKey(corKey);
		Query<MongoProcessInstanceInfo> q = ds.createQuery(MongoProcessInstanceInfo.class).disableValidation();
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
		MongoProcessInstanceInfo instanceInfo = q.get();
		return instanceInfo;
	}
	
	public List<MongoProcessInstanceInfo> findProcessInstancesByProcessEvent(String eventType) {
		//Map<String, String> eventTypeObj = new HashMap<String, String>();
		//eventTypeObj.put("eventType", eventType);
		return ds.find(MongoProcessInstanceInfo.class).filter("eventTypes =", eventType).asList();
	}
	
	public void removeProcessInstanceInfo(Long id) {
		MongoProcessInstanceInfo instance = ds.get(MongoProcessInstanceInfo.class, id);
		removeProcessInstanceInfo(instance);
	}
	
	public void removeProcessInstanceInfo(MongoProcessInstanceInfo instance) {
		if (instance != null) {
			tobeCommitted.put(StoreAction.DELETE, instance);
		}
	}

	public void removeAllProcessInstances() {
		ds.delete(ds.find(MongoProcessInstanceInfo.class));
	}
	
	public MongoProcessInstanceInfo findProcessInstanceByWorkItemId (long workItemId) {
		MongoProcessInstanceInfo instanceInfo = ds.find(MongoProcessInstanceInfo.class).disableValidation().filter("workItems." + workItemId+".id", workItemId).get();
		return instanceInfo;
	}
	
	public List<MongoProcessInstanceInfo> getAllProcessInstances() {
		return ds.find(MongoProcessInstanceInfo.class).asList();
	}
	
	public Object getMongoEntity(Class<?> entityClass, Object id) {
		return ds.get(entityClass, id);
	}
	
	public List<ProcessObjectPersistenceStrategy> getAllPersistenceStrategies() {
		return ds.find(ProcessObjectPersistenceStrategy.class).asList();
	}
}
