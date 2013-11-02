package org.jbpm.persistence.mongodb.rule.action;

import org.jbpm.persistence.mongodb.object.MongoJavaSerializable;
import org.kie.api.runtime.process.ProcessInstance;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedSignalAction extends EmbeddedWorkingMemoryAction {
	@Transient
    Logger logger = LoggerFactory.getLogger( getClass() );
	private String signalType;
	@Embedded
	private MongoJavaSerializable signal;
	
	public EmbeddedSignalAction() {}
	public EmbeddedSignalAction(String signalType, Object event) {
		super();
		logger.info("signalType=" + signalType + " event.class=" + event.getClass() + " event=" + event );
		this.signalType = signalType;
		if (event instanceof java.io.Serializable) {
			this.signal = new MongoJavaSerializable((java.io.Serializable)event);
		} else {
			logger.error("this is not a serializable event" + event);
		}
	}
	public String getSignalType() {
		return signalType;
	}
	public Object getSignal() {
		return signal != null? signal.getSerializedObject():null;
	}
	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}
	public void setSignal(Object event) {
		if (event instanceof java.io.Serializable) {
			this.signal = new MongoJavaSerializable((java.io.Serializable)event);
		} else {
			logger.error("this is not a serializable event" + event);
		}
	}
}
