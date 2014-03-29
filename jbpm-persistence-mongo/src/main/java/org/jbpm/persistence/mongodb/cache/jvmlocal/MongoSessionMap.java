package org.jbpm.persistence.mongodb.cache.jvmlocal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import org.jbpm.persistence.mongodb.MongoPersistUtil;
import org.jbpm.persistence.mongodb.cache.MongoSessionCache;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.instance.MongoProcessData;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.session.MongoSessionInfo;
import org.jbpm.persistence.mongodb.session.MongoSessionNotFoundException;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.process.CorrelationKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoSessionMap implements MongoSessionCache {
	private static final Logger log = LoggerFactory.getLogger(MongoSessionMap.class.getName());
	private static MongoSessionMap INSTANCE;
	
	private Map<Integer, MongoSessionInfo> sessionMap;
	private Map<Long, Integer> processInstanceIndex;
	private Map<Long, Integer> workItemIndex;
	private Map<MongoCorrelationKey, Integer> correlationIndex;
	private Map<String, Set<Long>> processDefIndex;
	public static MongoSessionMap getSessionMap() {
		if (INSTANCE == null) {
			synchronized(MongoSessionMap.class) {
		         //double checked locking - because second check of Singleton instance with lock
					if (INSTANCE == null) {
					INSTANCE = new MongoSessionMap();
				}
			}
		}
		return INSTANCE;
		
	}
	private MongoSessionMap() {
		sessionMap = new HashMap<Integer, MongoSessionInfo>();
		processInstanceIndex = new HashMap<Long, Integer>();
		workItemIndex = new HashMap<Long, Integer>();
		correlationIndex = new HashMap<MongoCorrelationKey, Integer>();
		processDefIndex = new HashMap<String, Set<Long>>();
	}
	
	public Collection<MongoSessionInfo> getAllSessions() {
		return Collections.unmodifiableCollection(sessionMap.values());
	}
	
	public MongoSessionInfo getCachedSession (int sessionId) {
		return sessionMap.get(sessionId);
	}
	
	public void removeCachedSession(int sessionId) {
		MongoSessionInfo sessionInfo = sessionMap.get(sessionId);
		if (sessionInfo != null) {
			sessionMap.remove(sessionId);
			if (sessionInfo.getProcessdata() == null) return;
			for (MongoProcessInstanceInfo mpi: sessionInfo.getProcessdata().getProcessInstances()) {
				removeProcessInstanceFromCache(mpi);
			}
		}
	}
	
	public void addSession(MongoSessionInfo sessionInfo) {
		sessionMap.put(sessionInfo.getId(), sessionInfo);
		int sessionId = sessionInfo.getId();
		MongoProcessData procData = sessionInfo.getProcessdata();
		for (MongoProcessInstanceInfo procInstInfo: procData.getProcessInstances()) {
			long procInstanceId = procInstInfo.getProcessInstanceId();
			processInstanceIndex.put(procInstanceId, sessionId);
			Set<Long> procInstSet = processDefIndex.get(procInstInfo.getProcessId());
			if (procInstSet == null) {
				procInstSet = new HashSet<Long>();
				processDefIndex.put(procInstInfo.getProcessId(), procInstSet);
			}
			if (!procInstSet.contains(procInstanceId)) procInstSet.add(procInstanceId);
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
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(procInstId);
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

	public Set<MongoProcessInstanceInfo> findProcessInstancesByProcessId(String processId) {
		Set<MongoProcessInstanceInfo> result = new HashSet<MongoProcessInstanceInfo>();
		Set<Long> procInstSet = processDefIndex.get(processId);
		if (procInstSet == null) return result;
		for (long procInstId: procInstSet) {
			MongoProcessInstanceInfo procInstInfo = getProcessInstance(procInstId, true);
			result.add(procInstInfo);
		}
		return result;
	}

	public boolean isSessionCached(int sessionId) {
		return sessionMap.get(sessionId) != null;
	}
	
	public boolean isProcessInstanceCached(long procInstId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(procInstId);
		return isSessionCached(sessionId);
	}
	
	public boolean isWorkItemCached(long workItemId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(workItemId);
		return isSessionCached(sessionId);
	}
	
	public void removeProcessInstance(long procInstId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(procInstId);
		MongoSessionInfo sessionInfo = sessionMap.get(sessionId);
		MongoProcessData procData = sessionInfo.getProcessdata();
		if (procData != null) {
			MongoProcessInstanceInfo procInstInfo = procData.getProcessInstance(procInstId);
			procData.removeProcessInstance(procInstId);
			removeProcessInstanceFromCache(procInstInfo);
		}
		sessionInfo.setModifiedSinceLastSave(true);
	}

	private void removeProcessInstanceFromCache(MongoProcessInstanceInfo procInstInfo) {
		processInstanceIndex.remove(procInstInfo.getProcessInstanceId());
		if (procInstInfo.getCorrelationKey() != null) { 
			correlationIndex.remove(procInstInfo.getCorrelationKey());
		}
		Set<Long> procInstSet = processDefIndex.get(procInstInfo.getProcessId());
		if (procInstSet != null) {
			procInstSet.remove(procInstInfo.getProcessInstanceId());
		}
	}
	
	public MongoSessionInfo getSessionByWorkItemId(long workItemId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(workItemId);
		return sessionMap.get(sessionId);
	}
	
	public MongoWorkItemInfo getWorkItem(long workItemId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(workItemId);
		MongoSessionInfo sessionInfo = sessionMap.get(sessionId);
		MongoProcessData procData = sessionInfo.getProcessdata();
		if (procData != null) {
			return procData.getWorkItem(workItemId);
		} else {
			return null;
		}
	}
}
