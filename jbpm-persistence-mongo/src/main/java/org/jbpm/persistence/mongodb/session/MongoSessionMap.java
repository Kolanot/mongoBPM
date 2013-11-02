package org.jbpm.persistence.mongodb.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import org.jbpm.persistence.mongodb.MongoPersistUtil;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.instance.MongoProcessData;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.process.CorrelationKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoSessionMap {
	private static final Logger log = LoggerFactory.getLogger(MongoSessionMap.class.getName());
	public static final MongoSessionMap INSTANCE = new MongoSessionMap();
	
	private Map<Integer, MongoSessionInfo> sessionMap;
	private Map<Long, Integer> processInstanceIndex;
	private Map<Long, Integer> workItemIndex;
	private Map<MongoCorrelationKey, Integer> correlationIndex;
	
	private MongoSessionMap() {
		sessionMap = new HashMap<Integer, MongoSessionInfo>();
		processInstanceIndex = new HashMap<Long, Integer>();
		workItemIndex = new HashMap<Long, Integer>();
		correlationIndex = new HashMap<MongoCorrelationKey, Integer>();
	}
	
	public Map<Integer, MongoSessionInfo> getSessionMap() {
		return sessionMap;
	}
	
	public MongoSessionInfo getCachedSession (int sessionId) {
		return sessionMap.get(sessionId);
	}
	
	public void addSession(MongoSessionInfo sessionInfo) {
		sessionMap.put(sessionInfo.getId(), sessionInfo);
		int sessionId = sessionInfo.getId();
		MongoProcessData procData = sessionInfo.getProcessdata();
		for (MongoProcessInstanceInfo procInstInfo: procData.getProcessInstances()) {
			long procInstanceId = procInstInfo.getProcessInstanceId();
			processInstanceIndex.put(procInstanceId, sessionId);
			if (procInstInfo.getCorrelationKey() != null) {
				log.info("procInstInfo, class: " + procInstInfo.getCorrelationKey().getClass()+ ":" + procInstInfo.getCorrelationKey());
				correlationIndex.put(procInstInfo.getCorrelationKey(),sessionId);
			}
		}
		Collection<MongoWorkItemInfo> workItemSet = procData.getWorkItems();
		for (Iterator<MongoWorkItemInfo> itr = workItemSet.iterator(); itr.hasNext();) {
			long workItemId = itr.next().getId();
			workItemIndex.put(workItemId, sessionId);
		}
	}
	
	public MongoProcessInstanceInfo getProcessInstance(long procInstId, boolean readOnly) {
		int sessionId = MongoPersistUtil.resolveSessionIdFromPairing(procInstId);
		log.info("procInstId" + procInstId);
		log.info("sessionId" + sessionId);
		MongoSessionInfo sessionInfo = sessionMap.get(sessionId);
		if (sessionInfo == null) return null;
		MongoProcessData procData = sessionInfo.getProcessdata();
		if (procData != null) {
			if (!readOnly) sessionInfo.setModifiedSinceLastSave(true);
			return procData.getProcessInstance(procInstId);
		}
		return null;
	}

	public Set<ProcessInstance> findProcessInstancesByEvent(int sessionId, String type) {
		MongoSessionInfo sessionInfo = sessionMap.get(sessionId);
		if (sessionInfo == null) 
			throw new MongoSessionNotFoundException("not found session by id " + sessionId);
		Set<ProcessInstance> instances = new HashSet<ProcessInstance>(); 
		for (MongoProcessInstanceInfo procInstInfo: sessionInfo.getProcessdata().getProcessInstances()) {
			ProcessInstance instance = procInstInfo.getProcessInstance();
			if (instance.getEventTypes() != null) {
				for (String eventType: instance.getEventTypes()) {
					if (eventType.equals(type)) {
						instances.add(instance);
					}
				}
			}
		}
		return instances;
	}
	
	public MongoProcessInstanceInfo findProcessInstanceByCorrelationKey(CorrelationKey key) {
		MongoCorrelationKey mongoKey = new MongoCorrelationKey(key);
		log.info("mongoKey:" + mongoKey);
		if (correlationIndex.containsKey(mongoKey)) {
			MongoSessionInfo sessionInfo = sessionMap.get(correlationIndex.get(mongoKey));
			MongoProcessData procData = sessionInfo.getProcessdata();
			if (procData != null) { 
				for (MongoProcessInstanceInfo procInstance:procData.getProcessInstances()) {
					if (procInstance.getCorrelationKey().equals(mongoKey))
						return procInstance;
				}
			}
		}
		return null;
	}

	public boolean isSessionCached(int sessionId) {
		return sessionMap.get(sessionId) != null;
	}
	
	public boolean isProcessInstanceCached(long procInstId) {
		int sessionId = MongoPersistUtil.resolveSessionIdFromPairing(procInstId);
		return isSessionCached(sessionId);
	}
	
	public boolean isWorkItemCached(long workItemId) {
		int sessionId = MongoPersistUtil.resolveSessionIdFromPairing(workItemId);
		return isSessionCached(sessionId);
	}
	
	public void removeProcessInstance(long procInstId) {
		int sessionId = MongoPersistUtil.resolveSessionIdFromPairing(procInstId);
		MongoSessionInfo sessionInfo = sessionMap.get(sessionId);
		MongoProcessData procData = sessionInfo.getProcessdata();
		if (procData != null) {
			procData.removeProcessInstance(procInstId);
		}
		sessionInfo.setModifiedSinceLastSave(true);
	}
	
	public MongoSessionInfo getSessionByWorkItemId(long workItemId) {
		int sessionId = MongoPersistUtil.resolveSessionIdFromPairing(workItemId);
		return sessionMap.get(sessionId);
	}
	
	public MongoWorkItemInfo getWorkItem(long workItemId) {
		int sessionId = MongoPersistUtil.resolveSessionIdFromPairing(workItemId);
		MongoSessionInfo sessionInfo = sessionMap.get(sessionId);
		MongoProcessData procData = sessionInfo.getProcessdata();
		if (procData != null) {
			return procData.getWorkItem(workItemId);
		} else {
			return null;
		}
	}
}
