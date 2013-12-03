package org.jbpm.persistence.mongodb.instance;

import java.util.Date;
import java.util.Set;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.persistence.mongodb.session.MongoSessionManager;
import org.jbpm.process.instance.event.DefaultSignalManager;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoSignalManager extends DefaultSignalManager {
    Logger logger = LoggerFactory.getLogger( getClass() );
    private MongoSessionManager sessionManager;

    public MongoSignalManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
        sessionManager = new MongoSessionManager(kruntime.getKieBase(), kruntime.getSessionConfiguration(), kruntime.getEnvironment());
    }
    
    public void signalEvent(String type,
                            Object event) {
    	logger.debug("MongoSignalManager, signalEvent(" + type + ", " + event + ")" );
        int sessionId = ((KieSession)getKnowledgeRuntime()).getId();
        
        for ( ProcessInstance instance : getProcessInstancesForEvent( type ) ) {
            try {
                getKnowledgeRuntime().getProcessInstance( instance.getId() );
            } catch (IllegalStateException e) {
                // IllegalStateException can be thrown when using RuntimeManager
                // and invalid ksession was used for given context
            }
        }
        
        super.signalEvent( type,
                           event );
        try {
        	logger.debug("set session updated in MongoSignalManager class, signalEvent method, sessionId" + sessionId);
        	sessionManager.setSessionUpdated(sessionId, new Date( getKnowledgeRuntime().getLastIdleTimestamp() ));
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    private Set<ProcessInstance> getProcessInstancesForEvent(String type) {
    	KieSession session = (KieSession)getKnowledgeRuntime();
    	return sessionManager.findProcessInstancesByEvent(session.getId(), type);
    }

}
