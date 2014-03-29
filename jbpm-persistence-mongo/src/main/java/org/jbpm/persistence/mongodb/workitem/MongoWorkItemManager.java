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
import org.jbpm.persistence.mongodb.MongoProcessStore;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoWorkItemManager implements WorkItemManager {
    Logger logger = LoggerFactory.getLogger( getClass() );

    private InternalKnowledgeRuntime kruntime;
    private Map<String, WorkItemHandler> workItemHandlers = new HashMap<String, WorkItemHandler>();
    private MongoProcessStore procStore;
    
    public MongoWorkItemManager(InternalKnowledgeRuntime kruntime) {
        this.kruntime = kruntime;
        procStore = (MongoProcessStore)kruntime.getEnvironment().get(MongoProcessStore.envKey);
    }
    
    public void internalExecuteWorkItem(WorkItem workItem) {
    	long procInstId = workItem.getProcessInstanceId();
    	MongoProcessInstanceInfo procInstInfo = procStore.findProcessInstanceInfo(procInstId);
    	if (workItem.getId() == 0) {
    		long workItemId = getNextWorkItemId(procInstId);
    		((WorkItemImpl)workItem).setId(workItemId);
			procInstInfo.addWorkItem(workItem, workItemId);
    	} else if (!procInstInfo.hasWorkItem(workItem.getId())) {
    		procInstInfo.addWorkItem(workItem, workItem.getId());
    	}
        WorkItemHandler handler = (WorkItemHandler) this.workItemHandlers.get(workItem.getName());
        if (handler != null) {
            handler.executeWorkItem(workItem, this);
        } else {
            throwWorkItemNotFoundException( workItem );
        }
    }

	private long getNextWorkItemId(long procInstId) {
		MongoProcessStore store = (MongoProcessStore)this.kruntime.getEnvironment().get(MongoProcessStore.envKey);
		long workItemId = store.getNextWorkItemId();
		return MongoPersistUtil.pairingTwoIDs(procInstId, workItemId);
	}
	
	private MongoProcessInstanceInfo getMongoProcInstInfoByWorkItemId(long workItemId) {
		long procInstId = MongoPersistUtil.resolveFirstIdFromPairing(workItemId);
		return procStore.findProcessInstanceInfo(procInstId);
	}

    private void throwWorkItemNotFoundException(WorkItem workItem) {
        throw new WorkItemHandlerNotFoundException( "Could not find work item handler for " + workItem.getName(),
                                                    workItem.getName() );
    }
    
    public WorkItemHandler getWorkItemHandler(String name) {
    	return this.workItemHandlers.get(name);
    }
    
    public void retryWorkItem(long workItemId) {
        MongoProcessInstanceInfo procInstInfo = getMongoProcInstInfoByWorkItemId(workItemId);
        if (procInstInfo != null) {
  			MongoWorkItemInfo workItemInfo = procInstInfo.getWorkItem(workItemId);
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

    public void internalAbortWorkItem(long workItemId) {
        MongoProcessInstanceInfo procInstInfo = getMongoProcInstInfoByWorkItemId(workItemId);
        // work item may have been aborted
        if (procInstInfo != null) {
  			MongoWorkItemInfo workItemInfo = procInstInfo.getWorkItem(workItemId);
  			if (workItemInfo == null) {
  				logger.info("cannot found workItem by id " + workItemId);
  				return;
  			}
  			WorkItem workItem = workItemInfo.getWorkItem();
			WorkItemHandler handler = (WorkItemHandler) this.workItemHandlers.get(workItem.getName());
			if (handler != null) {
				handler.abortWorkItem(workItem, this);
			}
			procInstInfo.removeWorkItem(workItemId);
			procStore.saveOrUpdate(procInstInfo);
        }
    }

    public void internalAddWorkItem(WorkItem workItem) {
    }

    public void completeWorkItem(long id, Map<String, Object> results) {
        MongoProcessInstanceInfo procInstInfo = getMongoProcInstInfoByWorkItemId(id);
        // work item may have been aborted
        if (procInstInfo != null) {
        	MongoWorkItemInfo itemInfo = procInstInfo.getWorkItem(id);
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
            logger.info("work item completed, process instance:" + procInstInfo.getProcessInstanceId() + ",work item id: " + workItem.getId());
        }
    }

    public void abortWorkItem(long id) {
        MongoProcessInstanceInfo procInstInfo = getMongoProcInstInfoByWorkItemId(id);
        // work item may have been aborted
        if (procInstInfo != null) {
        	MongoWorkItemInfo itemInfo = procInstInfo.getWorkItem(id);
  			if (itemInfo == null) {
  				logger.info("cannot found workItem by id " + id);
  				return;
  			}
            WorkItem workItem = itemInfo.getWorkItem();
            workItem.setState(WorkItem.ABORTED);
            // process instance may have finished already
            ProcessInstance processInstance = kruntime.getProcessInstance(workItem.getProcessInstanceId());
            itemInfo.setWorkItem(workItem);
            if (processInstance != null) {
                processInstance.signalEvent("workItemAborted", workItem);
            }
        }
    }

    public WorkItem getWorkItem(long workItemId) {
        MongoProcessInstanceInfo procInstInfo = getMongoProcInstInfoByWorkItemId(workItemId);
        MongoWorkItemInfo itemInfo = procInstInfo.getWorkItem(workItemId);
       return itemInfo == null? null:itemInfo.getWorkItem();
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
