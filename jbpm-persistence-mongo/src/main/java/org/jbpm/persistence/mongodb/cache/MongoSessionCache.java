package org.jbpm.persistence.mongodb.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jbpm.persistence.mongodb.MongoPersistUtil;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.instance.MongoProcessData;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.session.MongoSessionInfo;
import org.jbpm.persistence.mongodb.session.MongoSessionNotFoundException;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.process.CorrelationKey;

public interface MongoSessionCache {
	Collection<MongoSessionInfo> getAllSessions();
	
	MongoSessionInfo getCachedSession (int sessionId);
	
	void removeCachedSession(int sessionId);
	
	void addSession(MongoSessionInfo sessionInfo);
	
	MongoProcessInstanceInfo getProcessInstance(long procInstId, boolean readOnly);

	Set<ProcessInstance> findProcessInstancesByEvent(int sessionId, String type);
	
	MongoProcessInstanceInfo findProcessInstanceByCorrelationKey(CorrelationKey key);
	
	Set<MongoProcessInstanceInfo> findProcessInstancesByProcessId(String processId);

	boolean isSessionCached(int sessionId);
	
	boolean isProcessInstanceCached(long procInstId);
	
	void removeProcessInstance(long procInstId);

	boolean isWorkItemCached(long workItemId);
	
	MongoSessionInfo getSessionByWorkItemId(long workItemId);
	
	MongoWorkItemInfo getWorkItem(long workItemId);	
}
