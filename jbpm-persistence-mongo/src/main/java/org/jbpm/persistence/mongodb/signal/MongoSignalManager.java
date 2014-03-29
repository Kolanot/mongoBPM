package org.jbpm.persistence.mongodb.signal;

import java.io.NotSerializableException;
import java.util.List;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.persistence.mongodb.MongoProcessStore;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceMarshaller;
import org.jbpm.process.instance.event.DefaultSignalManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoSignalManager extends DefaultSignalManager {
    private static Logger logger = LoggerFactory.getLogger( MongoSignalManager.class );
    private MongoProcessStore procStore;

    public MongoSignalManager(InternalKnowledgeRuntime kruntime) {
        super(kruntime);
        procStore = (MongoProcessStore)kruntime.getEnvironment().get(MongoProcessStore.envKey);
    }
    
    public void signalEvent(String type,
                            Object event) {
    	logger.debug("MongoSignalManager, signalEvent(" + type + ", " + event + ")" );
        
        for ( MongoProcessInstanceInfo instance : getProcessInstancesForEvent( type ) ) {
            try {
                getKnowledgeRuntime().getProcessInstance( instance.getProcessInstanceId() );
            } catch (IllegalStateException e) {
                // IllegalStateException can be thrown when using RuntimeManager
                // and invalid ksession was used for given context
            }
        }
        
        super.signalEvent( type,
                           event );
        try {
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

	public void signalEvent(long processInstanceId, String type, Object event) {
		ProcessInstance instance = getKnowledgeRuntime().getProcessInstance( processInstanceId );
		super.signalEvent(processInstanceId, type, event);
		instance = getKnowledgeRuntime().getProcessInstance( processInstanceId );
		MongoProcessInstanceInfo procInstInfo = new MongoProcessInstanceInfo(instance);
		try {
			MongoProcessInstanceMarshaller.serialize(procInstInfo);
		} catch (NotSerializableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		procStore.saveOrUpdate(procInstInfo);
	}
    private List<MongoProcessInstanceInfo> getProcessInstancesForEvent(String type) {
    	return procStore.findProcessInstancesByProcessEvent(type);
    }

}
