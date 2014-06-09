package org.jbpm.persistence.mongodb.task.service;

import org.drools.core.command.Interceptor;
import org.jbpm.persistence.mongodb.MongoCommandInterceptor;
import org.jbpm.services.task.HumanTaskConfigurator;
import org.kie.api.runtime.manager.RuntimeEnvironment;

public class MongoHumanTaskServiceConfigurator extends HumanTaskConfigurator {

    private RuntimeEnvironment runtimeEnvironment;
    
	public MongoHumanTaskServiceConfigurator(RuntimeEnvironment runtimeEnvironment) {
		super();
		this.runtimeEnvironment = runtimeEnvironment;
	}

	@Override
	protected void addDefaultInterceptor() {
	   	// add default interceptor if present
    	try {
    		Interceptor defaultInterceptor = new MongoCommandInterceptor(runtimeEnvironment.getEnvironment());
    		interceptor(5, defaultInterceptor);
    	} catch (Exception e) {
    		//logger.warn("No default interceptor found, exception ",	e.getMessage());
    	}
		
	}

}
