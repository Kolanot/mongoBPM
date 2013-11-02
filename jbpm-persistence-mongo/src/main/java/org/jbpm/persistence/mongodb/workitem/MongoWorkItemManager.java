package org.jbpm.persistence.mongodb.workitem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.WorkItemManager;
import org.jbpm.persistence.mongodb.MongoPersistUtil;
import org.jbpm.persistence.mongodb.MongoSessionStore;
import org.jbpm.persistence.mongodb.instance.MongoProcessData;
import org.jbpm.persistence.mongodb.session.MongoSessionInfo;
import org.jbpm.persistence.mongodb.session.MongoSessionManager;
import org.jbpm.persistence.mongodb.session.MongoSessionMap;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoWorkItemManager implements WorkItemManager {
    Logger logger = LoggerFactory.getLogger( getClass() );

    private InternalKnowledgeRuntime kruntime;
    private Map<String, WorkItemHandler> workItemHandlers = new HashMap<String, WorkItemHandler>();
    private MongoSessionManager sessionManager;
    
    public MongoWorkItemManager(InternalKnowledgeRuntime kruntime) {
        this.kruntime = kruntime;
        sessionManager = new MongoSessionManager(kruntime);
    }
    
    public void internalExecuteWorkItem(WorkItem workItem) {
    	MongoSessionInfo sessionInfo = getMongoSessionInfoByProcessInstanceId(workItem.getProcessInstanceId());
    	MongoProcessData processData = sessionInfo.getProcessdata(); 
    	if (workItem.getId() == 0) {
    		long workItemId = getNextWorkItemId(sessionInfo.getId());
    		((WorkItemImpl)workItem).setId(workItemId);
			processData.addWorkItem(workItem, workItemId);
    	} else if (!processData.hasWorkItem(workItem.getId())) {
    		processData.addWorkItem(workItem);
    	}
    	sessionInfo.setModifiedSinceLastSave(true);
        WorkItemHandler handler = (WorkItemHandler) this.workItemHandlers.get(workItem.getName());
        if (handler != null) {
            handler.executeWorkItem(workItem, this);
        } else {
            throwWorkItemNotFoundException( workItem );
        }
    }

	private MongoSessionInfo getMongoSessionInfoByProcessInstanceId(long procInstId) { 
		int sessionId = MongoPersistUtil.resolveSessionIdFromPairing(procInstId);
		MongoSessionInfo sessionInfo = null;
		try {
			sessionInfo = sessionManager.getMongoSessionInfo(sessionId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sessionInfo;
	}

	private long getNextWorkItemId(int sessionId) {
		MongoSessionStore store = (MongoSessionStore)this.kruntime.getEnvironment().get(MongoSessionStore.envKey);
		long workItemId = store.getNextWorkItemId();
		return MongoPersistUtil.pairingSessionId(sessionId, workItemId);
	}
	
	private MongoSessionInfo getMongoSessionInfoByWorkItemId(long workItemId) {
		if (!MongoSessionMap.INSTANCE.isWorkItemCached(workItemId)) {
			sessionManager.reloadWorkItem(workItemId);
		}
		MongoSessionInfo sessionInfo = MongoSessionMap.INSTANCE.getSessionByWorkItemId(workItemId);
		return sessionInfo;
	}

    private void throwWorkItemNotFoundException(WorkItem workItem) {
        throw new WorkItemHandlerNotFoundException( "Could not find work item handler for " + workItem.getName(),
                                                    workItem.getName() );
    }
    
    public WorkItemHandler getWorkItemHandler(String name) {
    	return this.workItemHandlers.get(name);
    }
    
    public void retryWorkItem(long workItemId) {
        MongoSessionInfo sessionInfo = getMongoSessionInfoByWorkItemId(workItemId);
        if (sessionInfo != null) {
  			MongoWorkItemInfo workItemInfo = getWorkItem(sessionInfo, workItemId);
  			if (workItemInfo == null) {
  				logger.debug("cannot found workItem by id " + workItemId);
  				return;
  			}
  			WorkItem workItem = workItemInfo.getWorkItem();
			WorkItemHandler handler = (WorkItemHandler) this.workItemHandlers.get(workItem.getName());
			if (handler != null) {
				handler.executeWorkItem(workItem, this);
			} else {
				throwWorkItemNotFoundException( workItem );
			}
        } else {
			logger.debug("cannot found session assoicated to workItem by id " + workItemId);
        }
    }

    public void internalAbortWorkItem(long id) {
        MongoSessionInfo sessionInfo = getMongoSessionInfoByWorkItemId(id);
        // work item may have been aborted
        if (sessionInfo != null) {
  			MongoWorkItemInfo workItemInfo = getWorkItem(sessionInfo, id);
  			if (workItemInfo == null) {
  				logger.info("cannot found workItem by id " + id);
  				return;
  			}
  			WorkItem workItem = workItemInfo.getWorkItem();
			WorkItemHandler handler = (WorkItemHandler) this.workItemHandlers.get(workItem.getName());
			if (handler != null) {
				handler.abortWorkItem(workItem, this);
			}
			sessionInfo.getProcessdata().removeWorkItem(id);
			sessionInfo.setModifiedSinceLastSave(true);
        }
    }

    public void internalAddWorkItem(WorkItem workItem) {
    }

    public void completeWorkItem(long id, Map<String, Object> results) {
        MongoSessionInfo sessionInfo = getMongoSessionInfoByWorkItemId(id);
        // work item may have been aborted
        if (sessionInfo != null) {
        	MongoWorkItemInfo itemInfo = getWorkItem(sessionInfo, id);
  			if (itemInfo == null) {
  				logger.info("cannot found workItem by id " + id);
  				return;
  			}
            WorkItem workItem = itemInfo.getWorkItem();
            workItem.setResults(results);
            ProcessInstance processInstance = kruntime.getProcessInstance(workItem.getProcessInstanceId());
            workItem.setState(WorkItem.COMPLETED);
            itemInfo.setWorkItem(workItem);
            
            // process instance may have finished already
            if (processInstance != null) {
                processInstance.signalEvent("workItemCompleted", workItem);
            }
            sessionInfo.setModifiedSinceLastSave(true);
            logger.debug("work item completed, session:" + sessionInfo.getId() + ",work item id: " + workItem.getId());
        }
    }

    public void abortWorkItem(long id) {
        MongoSessionInfo sessionInfo = getMongoSessionInfoByWorkItemId(id);
        // work item may have been aborted
        if (sessionInfo != null) {
        	MongoWorkItemInfo itemInfo = getWorkItem(sessionInfo, id);
  			if (itemInfo == null) {
  				logger.info("cannot found workItem by id " + id);
  				return;
  			}
            WorkItem workItem = itemInfo.getWorkItem();
            workItem.setState(WorkItem.ABORTED);
            // process instance may have finished already
            ProcessInstance processInstance = kruntime.getProcessInstance(workItem.getProcessInstanceId());
            if (processInstance != null) {
                processInstance.signalEvent("workItemAborted", workItem);
            }
            itemInfo.setWorkItem(workItem);
            sessionInfo.setModifiedSinceLastSave(true);
        }
    }

    public WorkItem getWorkItem(long id) {
        MongoSessionInfo sessionInfo = getMongoSessionInfoByWorkItemId(id);
        MongoWorkItemInfo itemInfo = getWorkItem(sessionInfo, id);
       return itemInfo == null? null:itemInfo.getWorkItem();
    }

    private MongoWorkItemInfo getWorkItem(MongoSessionInfo sessionInfo, long id) {
        // work item may have been aborted
        if (sessionInfo != null) {
            for (MongoWorkItemInfo itemInfo : sessionInfo.getProcessdata().getWorkItems())  {
            	if (itemInfo.getId() == id)
            		return itemInfo;
            }
        }	
    	return null;
    }

    public void registerWorkItemHandler(String workItemName, WorkItemHandler handler) {
        this.workItemHandlers.put(workItemName, handler);
    }

    public void clearWorkItems() {
        //if (workItems != null) {
        //    workItems.clear();
        //}
    }

    public void clear() {
        clearWorkItems();
    }
    
    public void signalEvent(String type, Object event) { 
        this.kruntime.signalEvent(type, event);
    } 
    
    public void signalEvent(String type, Object event, long processInstanceId) { 
        this.kruntime.signalEvent(type, event, processInstanceId);
    }

	@Override
	public Set<WorkItem> getWorkItems() {
		return null;
	}
}
