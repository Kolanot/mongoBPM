package org.jbpm.persistence.mongodb.workitem;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity
public class MongoWorkItemId {
	public static String workItemId = "WorkItemId"; 
	@Id String id; 
	@Property Long seq;
	
	public MongoWorkItemId() {
		id =  workItemId;
		seq = new Long(0);
	}
	
	public void setSeq (Long seq) {
		this.seq = seq;
	}
	
	public Long getSeq () {
		return seq;
	}
}
