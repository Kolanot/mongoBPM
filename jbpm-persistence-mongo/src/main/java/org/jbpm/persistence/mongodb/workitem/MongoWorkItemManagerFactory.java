package org.jbpm.persistence.mongodb.workitem;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.process.instance.WorkItemManager;
import org.drools.core.process.instance.WorkItemManagerFactory;

public class MongoWorkItemManagerFactory implements WorkItemManagerFactory {

    public WorkItemManager createWorkItemManager(InternalKnowledgeRuntime kruntime) {
        return new MongoWorkItemManager(kruntime);
    }

}
