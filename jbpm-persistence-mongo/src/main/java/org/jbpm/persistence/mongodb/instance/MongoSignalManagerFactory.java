package org.jbpm.persistence.mongodb.instance;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.event.SignalManager;
import org.jbpm.process.instance.event.SignalManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoSignalManagerFactory implements SignalManagerFactory {
    Logger logger = LoggerFactory.getLogger( getClass() );

	public SignalManager createSignalManager(InternalKnowledgeRuntime kruntime) {
		logger.debug("create signal manager for kruntime" + kruntime);
		return new MongoSignalManager(kruntime);
	}

}
