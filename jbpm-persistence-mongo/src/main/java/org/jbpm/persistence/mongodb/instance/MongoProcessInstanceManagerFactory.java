package org.jbpm.persistence.mongodb.instance;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.ProcessInstanceManagerFactory;

public class MongoProcessInstanceManagerFactory implements ProcessInstanceManagerFactory {

	public ProcessInstanceManager createProcessInstanceManager(InternalKnowledgeRuntime kruntime) {
		MongoProcessInstanceManager result = new MongoProcessInstanceManager();
		result.setKnowledgeRuntime(kruntime);
		return result;
	}
}
