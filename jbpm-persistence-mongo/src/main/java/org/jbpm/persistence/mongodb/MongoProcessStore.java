package org.jbpm.persistence.mongodb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceId;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.process.CorrelationKey;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import com.mongodb.MongoClient;

public class MongoProcessStore {

	public static String envKey = "MongoProcessStore";
	static String dbName = "BPM"; 
	Datastore ds; 
	Morphia morphia;
	MongoClient mongo;
	
	public MongoProcessStore() {
		try {
			// Mongo singleton, remote connection to DB server
			mongo = MongoSingleton.INSTANCE.getMongo();
			morphia = new Morphia();
			morphia.map(MongoProcessInstanceInfo.class);
			ds= morphia.createDatastore(mongo, dbName);
			if (ds.get(MongoProcessInstanceId.class, MongoProcessInstanceId.processsInstanceId) == null)
				ds.save(new MongoProcessInstanceId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MongoProcessInstanceInfo findProcessInstanceInfo(Long processInstanceId) {
		return ds.get(MongoProcessInstanceInfo.class, processInstanceId);
	}

	public void saveOrUpdate(MongoProcessInstanceInfo processInstanceInfo) {
		if (processInstanceInfo.getProcessInstanceId() == 0) {
			processInstanceInfo.setProcessInstanceId(getNextProcessInstanceId());
		}
		ds.save(processInstanceInfo);
		processInstanceInfo.setModifiedSinceLastSave(false);
	}

	public long getNextProcessInstanceId() {
		Query<MongoProcessInstanceId> query = ds.find(MongoProcessInstanceId.class);
		UpdateOperations<MongoProcessInstanceId> ops = ds.createUpdateOperations(MongoProcessInstanceId.class).inc("seq");
		MongoProcessInstanceId instanceId = ds.findAndModify(query, ops);
		return instanceId.getSeq();
	}

	public void removeProcessInstanceInfo(Long id) {
		MongoProcessInstanceInfo instance = ds.get(MongoProcessInstanceInfo.class, id);
		removeProcessInstanceInfo(instance);
	}
	
	public void removeProcessInstanceInfo(MongoProcessInstanceInfo instance) {
		if (instance != null) {
			//MongoProcessInstanceHistory hist = new MongoProcessInstanceHistory(instance);
			//ds.save(hist);
			ds.delete(instance);
		}
		
	}

	public void removeProcessInstances() {
		ds.delete(ds.find(MongoProcessInstanceInfo.class));
	}
	
	public List<ProcessInstance> getProcessInstancesWaitingForEvent(String type) {
		List<MongoProcessInstanceInfo> instances = ds.find(MongoProcessInstanceInfo.class, "eventTypes", type).asList();
		if (instances == null) return null;
		List<ProcessInstance> result = new ArrayList<ProcessInstance>();
		for (Iterator<MongoProcessInstanceInfo> itr = instances.iterator(); itr.hasNext();) {
			result.add(itr.next().getProcessInstance());
		}
		return result;
	}
	
	public MongoProcessInstanceInfo findProcessInstanceByCorrelationKey (CorrelationKey corKey) {
		MongoCorrelationKey mongoKey = new MongoCorrelationKey(corKey);
		MongoProcessInstanceInfo instanceInfo = ds.find(MongoProcessInstanceInfo.class, "correlationKey", mongoKey).get();
		return instanceInfo;
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
}
