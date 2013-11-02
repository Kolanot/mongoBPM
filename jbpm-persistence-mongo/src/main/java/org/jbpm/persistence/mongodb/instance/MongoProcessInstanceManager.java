package org.jbpm.persistence.mongodb.instance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.persistence.mongodb.MongoKnowledgeService;
import org.jbpm.persistence.mongodb.session.MongoSessionManager;
import org.jbpm.persistence.mongodb.session.MongoSessionMap;
import org.jbpm.persistence.mongodb.session.MongoSessionNotFoundException;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link ProcessInstanceManager} that uses MongoDB.
 */
public class MongoProcessInstanceManager implements ProcessInstanceManager {
    Logger logger = LoggerFactory.getLogger( getClass() );

    private InternalKnowledgeRuntime kruntime;
    private MongoSessionManager sessionManager;
    
    //private Map<Long, MongoProcessInstanceInfo> processInstances = new HashMap<Long, MongoProcessInstanceInfo>(); 
    
    public void setKnowledgeRuntime(InternalKnowledgeRuntime kruntime) {
        this.kruntime = kruntime;
        sessionManager = new MongoSessionManager(kruntime.getKieBase(), kruntime.getSessionConfiguration(), kruntime.getEnvironment());
    }

    public void addProcessInstance(ProcessInstance processInstance, CorrelationKey correlationKey) {
    	sessionManager.addProcessInstance((KieSession)kruntime, processInstance, correlationKey);
        internalAddProcessInstance(processInstance);
    }
    
    public void internalAddProcessInstance(ProcessInstance processInstance) {
    }

    public ProcessInstance getProcessInstance(long id) {
        return getProcessInstance(id, false);
    }

	public ProcessInstance getProcessInstance(long id, boolean readOnly) {
        InternalRuntimeManager manager = (InternalRuntimeManager) kruntime.getEnvironment().get("RuntimeManager");
        if (manager != null) {
            manager.validate((KieSession) kruntime, ProcessInstanceIdContext.get(id));
        }
        MongoProcessInstanceInfo procInstInfo = (MongoProcessInstanceInfo) sessionManager.findCachedProcessInstance((KieSession)kruntime, id, readOnly);
        if (procInstInfo == null)
        	return null;
        org.jbpm.process.instance.ProcessInstance processInstance = (org.jbpm.process.instance.ProcessInstance)	procInstInfo.getProcessInstance();

        if (processInstance != null) {
            if (((ProcessInstanceImpl) processInstance).getProcessXml() == null) {
    	        Process process = kruntime.getKieBase().getProcess( processInstance.getProcessId() );
    	        if ( process == null ) {
    	            throw new IllegalArgumentException( "Could not find process " + processInstance.getProcessId() );
    	        }
    	        processInstance.setProcess( process );
            }
            if ( processInstance.getKnowledgeRuntime() == null ) {
                Long parentProcessInstanceId = (Long) ((ProcessInstanceImpl) processInstance).getMetaData().get("ParentProcessInstanceId");
                if (parentProcessInstanceId != null) {
                    kruntime.getProcessInstance(parentProcessInstanceId);
                }
                processInstance.setKnowledgeRuntime( kruntime );
            }
            if (!procInstInfo.isReconnected()) {
            	((RuleFlowProcessInstance)processInstance).reconnect();
            	procInstInfo.reconnect();
            }
        }
        return processInstance;
    }

    public Collection<ProcessInstance> getProcessInstances() {
        Set<ProcessInstance> processInstances = sessionManager.getAllProcessInstances();
        return Collections.unmodifiableCollection(processInstances);
    }

    public void internalRemoveProcessInstance(ProcessInstance processInstance) {
    }
    
    public void clearProcessInstances() {
    	logger.debug("clearProcessInstances called");
        for (ProcessInstance processInstance: new ArrayList<ProcessInstance>(getProcessInstances())) {
    		((ProcessInstanceImpl) processInstance).disconnect();
        }
    }

    public void clearProcessInstancesState() {
        try {
        	Collection<ProcessInstance> processInstances = getProcessInstances();
            // at this point only timers are considered as state that needs to be cleared
            TimerManager timerManager = ((InternalProcessRuntime)kruntime.getProcessRuntime()).getTimerManager();
            
            for (ProcessInstance processInstance: processInstances) {
                WorkflowProcessInstance pi = ((WorkflowProcessInstance) processInstance);
                
                for (org.kie.api.runtime.process.NodeInstance nodeInstance : pi.getNodeInstances()) {
                    if (nodeInstance instanceof TimerNodeInstance){
                        if (((TimerNodeInstance)nodeInstance).getTimerInstance() != null) {
                            timerManager.cancelTimer(((TimerNodeInstance)nodeInstance).getTimerInstance().getId());
                        }
                    } else if (nodeInstance instanceof StateBasedNodeInstance) {
                        List<Long> timerIds = ((StateBasedNodeInstance) nodeInstance).getTimerInstances();
                        if (timerIds != null) {
                            for (Long id: timerIds) {
                                timerManager.cancelTimer(id);
                            }
                        }
                    }
                }
                
            }
        } catch (Exception e) {
            // catch everything here to make sure it will not break any following 
            // logic to allow complete clean up 
        }
    }

	public ProcessInstance findProcessInstanceByWorkItemId(long workItemId) {
		MongoWorkItemInfo workItem = MongoSessionMap.INSTANCE.getWorkItem(workItemId);
		if (workItem == null) {
			KieSession session = MongoKnowledgeService.loadStatefulKnowledgeSessionByWorkItemId
					(workItemId, 
					this.kruntime.getKieBase(), 
					this.kruntime.getSessionConfiguration(),
					this.kruntime.getEnvironment());
			if (session != null) {
				workItem = MongoSessionMap.INSTANCE.getWorkItem(workItemId);
			}
		}
		if (workItem == null) return null;
		MongoProcessInstanceInfo procInst = MongoSessionMap.INSTANCE.getProcessInstance(workItem.getProcessInstanceId(), false);
		if (procInst != null) 
			return procInst.getProcessInstance();
		else 
			return null;
	}

    @Override
    public ProcessInstance getProcessInstance(CorrelationKey correlationKey) {
		MongoProcessInstanceInfo procInst = sessionManager.findProcessInstanceByCorrelationKey(correlationKey);
		if (procInst != null) 
			return procInst.getProcessInstance();
		else 
			return null;
	}

    @Override
    public void removeProcessInstance(ProcessInstance processInstance) {
    	if (processInstance == null) return;
    	long procInstId = processInstance.getId();
    	if (!MongoSessionMap.INSTANCE.isProcessInstanceCached(procInstId)) {
    		sessionManager.reloadProcessInstance(procInstId);
    	} else {
    		
    	}
		MongoSessionMap.INSTANCE.removeProcessInstance(procInstId);
        internalRemoveProcessInstance(processInstance);
    }
}
