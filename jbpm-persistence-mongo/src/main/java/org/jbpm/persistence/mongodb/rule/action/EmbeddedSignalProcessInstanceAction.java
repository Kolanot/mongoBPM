package org.jbpm.persistence.mongodb.rule.action;

import org.mongodb.morphia.annotations.Serialized;

public class EmbeddedSignalProcessInstanceAction extends
		EmbeddedWorkingMemoryAction {
	private static final long serialVersionUID = 1L;
	private long processInstanceId;
	private String signalType;
	@Serialized
	private Object event;
	
	public EmbeddedSignalProcessInstanceAction(long processInstanceId,
			String signalType, Object event) {
		super();
		this.processInstanceId = processInstanceId;
		this.signalType = signalType;
		this.event = event;
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public String getSignalType() {
		return signalType;
	}

	public Object getEvent() {
		return event;
	}
}
