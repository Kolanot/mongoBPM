package org.jbpm.persistence.mongodb.test;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceManager;
import org.jbpm.process.instance.ProcessInstanceManager;
import org.jbpm.process.instance.ProcessInstanceManagerFactory;

public class CustomExampleProcessInstanceManagerFactory implements ProcessInstanceManagerFactory {

	public ProcessInstanceManager createProcessInstanceManager(InternalKnowledgeRuntime kruntime) {
		MongoProcessInstanceManager result = new MongoProcessInstanceManager();
		result.setKnowledgeRuntime(kruntime);
		return result;
	}

}
