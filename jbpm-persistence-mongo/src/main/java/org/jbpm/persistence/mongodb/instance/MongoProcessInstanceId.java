package org.jbpm.persistence.mongodb.instance;
import java.io.Serializable;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

public class MongoProcessInstanceId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String processsInstanceId = "InstanceId"; 
	@Id String id; 
	@Property Long seq;
	
	public MongoProcessInstanceId() {
		id =  processsInstanceId;
		seq = new Long(0);
	}
	
	public void setSeq (Long seq) {
		this.seq = seq;
	}
	
	public Long getSeq () {
		return seq;
	}
}
