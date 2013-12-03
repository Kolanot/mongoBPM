package org.jbpm.persistence.mongodb.session;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.persistence.mongodb.MongoKnowledgeService;
import org.jbpm.persistence.mongodb.MongoPersistUtil;
import org.jbpm.persistence.mongodb.MongoSessionStore;
import org.jbpm.persistence.mongodb.cache.MongoSessionCache;
import org.jbpm.persistence.mongodb.cache.MongoSessionCacheLocator;
import org.jbpm.persistence.mongodb.instance.MongoProcessData;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class MongoSessionManager {
    static Logger logger = LoggerFactory.getLogger( MongoSessionManager.class );
    private MongoSessionMarshaller marshaller;
	private MongoSessionStore store;
	private KieBase kbase;
    private final Environment env;
    private final KieSessionConfiguration conf;
    private MongoSessionCache sessionCache;
    
    public MongoSessionManager(InternalKnowledgeRuntime kruntime) {
    	this(kruntime.getKieBase(), kruntime.getSessionConfiguration(), kruntime.getEnvironment());
    }
    
    public MongoSessionManager(KieBase kbase, KieSessionConfiguration conf, Environment env) {
		marshaller = new MongoSessionMarshaller(kbase, env);
		this.env = env;
		this.conf = conf;
		this.kbase = kbase;
		store = ((MongoSessionStore) env.get( MongoSessionStore.envKey ));
		sessionCache = MongoSessionCacheLocator.valueOf((String) env.get(MongoSessionCache.cacheKey)).getCache();
	}
	
	private MongoSessionInfo getCachedSession(int sessionId) {
		return sessionCache.getCachedSession(sessionId);
	}
	
	public void removeCachedSession(int sessionId) {
		sessionCache.removeCachedSession(sessionId);
	}
	
	public KieSession getSession(int sessionId) 
			throws IOException, 
			ClassNotFoundException, 
			IllegalArgumentException, 
			InstantiationException, 
			IllegalAccessException, 
			InvocationTargetException {
		MongoSessionInfo sessionInfo = getMongoSessionInfo(sessionId);
		return sessionInfo.getSession();
 	}
	
	public boolean isSessionSaved(int sessionId) {
		MongoSessionInfo sessionInfo = getCachedSession(sessionId);
		return sessionInfo!= null && !sessionInfo.modifiedSinceLastSave;
	}
	public MongoSessionInfo getMongoSessionInfo(int sessionId) 
			throws IOException, 
			ClassNotFoundException, 
			IllegalArgumentException, 
			InstantiationException, 
			IllegalAccessException, 
			InvocationTargetException {
		MongoSessionInfo sessionInfo = getCachedSession(sessionId);
		if (sessionInfo == null) {
			sessionInfo = store.findSessionInfo(sessionId);
			if (sessionInfo != null) {
				sessionCache.addSession(sessionInfo);
			} else {
	            throw new MongoSessionNotFoundException( "Could not find session data for id " + sessionId);
			}
	        this.marshaller.loadSnapshot(sessionInfo, kbase, conf, env);
		}
        return sessionInfo;
	}

	public void setSessionUpdated(int sessionId, Date date) throws ClassNotFoundException, IOException {
		MongoSessionInfo sessionInfo = getCachedSession(sessionId);
		//this.marshaller.syncSnapshot(sessionInfo);
		sessionInfo.setLastModificationDate(date);
		sessionInfo.setModifiedSinceLastSave(true);
		
		logger.debug("session updated" + sessionId + " date:" + date);
	}

	public Set<ProcessInstance> getAllProcessInstances() {
		Set<ProcessInstance> instances = new HashSet<ProcessInstance>();
		Collection<MongoSessionInfo> sessions = sessionCache.getAllSessions();
		for (MongoSessionInfo sessionInfo:sessions){
			MongoProcessData procData = sessionInfo.getProcessdata();
			if (procData != null) {
				for (MongoProcessInstanceInfo procInfo : procData.getProcessInstances()) {
					instances.add(procInfo.getProcessInstance());
				}
			}
		}
		return instances;
	}
	
	public void addProcessInstance(KieSession session, ProcessInstance processInstance, CorrelationKey correlationKey) {
		MongoSessionInfo sessionInfo = sessionCache.getCachedSession(session.getId());
        MongoProcessInstanceInfo processInstanceInfo = new MongoProcessInstanceInfo(processInstance);
        if (correlationKey != null) 
        	processInstanceInfo.assignCorrelationKey(correlationKey);
        
        long procInstId = store.getNextProcessInstanceId();
        procInstId = MongoPersistUtil.pairingSessionId(sessionInfo.getId(), procInstId);
        processInstanceInfo.setProcessInstanceId(procInstId);
        ((org.jbpm.process.instance.ProcessInstance) processInstance).setId( processInstanceInfo.getProcessInstanceId() );
        processInstanceInfo.setProcessId(processInstance.getProcessId());

        sessionInfo.addProcessInstance(processInstanceInfo);
        sessionInfo.setModifiedSinceLastSave(true);
        sessionCache.addSession(sessionInfo);
	}
	
	public Set<ProcessInstance> findProcessInstancesByEvent(int sessionId, String type) {
		return sessionCache.findProcessInstancesByEvent(sessionId, type);
	}
    public void saveCachedSession(int sessionId) throws ClassNotFoundException, IOException {
        MongoSessionInfo sessionInfo = getCachedSession(sessionId);
        if (sessionInfo == null) return;
		marshaller.syncSnapshot(sessionInfo);
		sessionInfo.setLastModificationDate(new java.util.Date());
		logger.debug("MongoSessionManager.saveSession, session saved, sessionId " + sessionId );
		store.saveOrUpdate(sessionInfo);
		removeCachedSession(sessionId);
    }
    
    public void saveModifiedSessions() throws ClassNotFoundException, IOException {
        for (MongoSessionInfo session : sessionCache.getAllSessions()) {
        	if (session.isModifiedSinceLastSave()) {
        		marshaller.syncSnapshot(session);
        		session.setLastModificationDate(new java.util.Date());
        		logger.info("MongoSessionManager.saveModifiedSessions, session saved, sessionId " + session.getId() );
        		store.saveOrUpdate(session);	
        	}
        } 
    }
    
    public void addSession(KieSession session) throws ClassNotFoundException, IOException {
    	MongoSessionInfo sessionInfo = marshaller.getSnapshot(session);
    	store.saveNew(sessionInfo);
    	if (session instanceof InternalKnowledgeRuntime)
    		((InternalKnowledgeRuntime) session).setId(sessionInfo.getId());
    	sessionCache.addSession(sessionInfo);
    }
    
    public MongoProcessInstanceInfo findProcessInstanceByCorrelationKey(CorrelationKey corKey) {
		MongoProcessInstanceInfo procInst = sessionCache.findProcessInstanceByCorrelationKey(corKey);
		if (procInst == null) {
			try {
				int sessionId = findSessionIdbyProcessCorelationKey(corKey);
				reloadKnowledgeSession(sessionId);
				procInst = sessionCache.findProcessInstanceByCorrelationKey(corKey);
			} catch (MongoSessionNotFoundException e) {
				return null;
			}
		}
		if (procInst != null) 
			return procInst;
		else 
			return null;
    }

	public MongoProcessInstanceInfo findCachedProcessInstance(KieSession session, long procInstId, boolean readOnly) {
		int sessionIdFromProcId = MongoPersistUtil.resolveSessionIdFromPairing(procInstId);
		
		if (session.getId() != sessionIdFromProcId) {
			logger.info("This should be in a different session, currrent session is " + session.getId());
			return null;
		}
		if (!sessionCache.isSessionCached(sessionIdFromProcId)) {
			try {
				addSession(session);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		MongoProcessInstanceInfo procInst = sessionCache.getProcessInstance(procInstId, readOnly);
		if (procInst != null)  {
			return procInst;
		} else 
			return null;
	}

    public int findSessionIdbyProcessCorelationKey(CorrelationKey corKey) {
    	MongoSessionInfo sessionInfo = store.findSessionInfoByProcessCorrelationKey(corKey);
    	if (sessionInfo != null)
    		return sessionInfo.getId();
    	else 
    		throw new MongoSessionNotFoundException("not found session by correlation key " + corKey);
    }

	public StatefulKnowledgeSession reloadKnowledgeSession(int sessionId) {
    	StatefulKnowledgeSession ksession =  MongoKnowledgeService.loadStatefulKnowledgeSession(sessionId,kbase, conf, env);
        return ksession;
    }

	public void reloadProcessInstance(long procInstId) {
    	MongoKnowledgeService.loadStatefulKnowledgeSessionByProcessInstanceId(procInstId, kbase, conf, env);
    }

	public MongoProcessInstanceInfo getProcessInstance(long procInstId, boolean readOnly) {
		if (!sessionCache.isProcessInstanceCached(procInstId)) {
			MongoKnowledgeService.loadStatefulKnowledgeSessionByProcessInstanceId(procInstId, kbase, conf, env);
		}
		return sessionCache.getProcessInstance(procInstId, readOnly);
    }

	public void removeProcessInstance(long procInstId) {
		if (!sessionCache.isProcessInstanceCached(procInstId)) {
			MongoKnowledgeService.loadStatefulKnowledgeSessionByProcessInstanceId(procInstId, kbase, conf, env);
		}
		sessionCache.removeProcessInstance(procInstId);
    }

	public void reloadWorkItem(long workItemId) {
    	MongoKnowledgeService.loadStatefulKnowledgeSessionByWorkItemId(workItemId, kbase, conf, env);
    }
	
	public MongoSessionInfo getSessionByWorkItemId(long workItemId) {
		if (!sessionCache.isWorkItemCached(workItemId)) {
			MongoKnowledgeService.loadStatefulKnowledgeSessionByWorkItemId(workItemId, kbase, conf, env);
		}
		return sessionCache.getSessionByWorkItemId(workItemId);
    }
	
	public MongoWorkItemInfo getWorkItem(long workItemId) {
		if (!sessionCache.isWorkItemCached(workItemId)) {
			MongoKnowledgeService.loadStatefulKnowledgeSessionByWorkItemId(workItemId, kbase, conf, env);
		}
    	return sessionCache.getWorkItem(workItemId);
    }
}
