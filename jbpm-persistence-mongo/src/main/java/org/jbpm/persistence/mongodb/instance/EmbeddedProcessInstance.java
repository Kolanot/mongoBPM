package org.jbpm.persistence.mongodb.instance;

import java.io.Serializable;

public class EmbeddedProcessInstance implements Serializable {


	private static final long serialVersionUID = 1L;
	private long processInstanceId;

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
}
