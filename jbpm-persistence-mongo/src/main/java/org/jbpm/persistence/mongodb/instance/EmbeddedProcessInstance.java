package org.jbpm.persistence.mongodb.instance;

import java.io.Serializable;

public class EmbeddedProcessInstance implements Serializable{

	private long processInstanceId;

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
}
