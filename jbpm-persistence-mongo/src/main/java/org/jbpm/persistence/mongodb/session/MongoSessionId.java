package org.jbpm.persistence.mongodb.session;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

public class MongoSessionId {
	public static String sessionId = "SessionId"; 
	@Id String id; 
	@Property Integer seq;
	
	public MongoSessionId() {
		id = sessionId;
		seq = new Integer(0);
	}
	
	public void setSeq (Integer seq) {
		this.seq = seq;
	}
	
	public Integer getSeq () {
		return seq;
	}
}
