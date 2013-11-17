package org.jbpm.persistence.mongodb.session.timer;

import java.util.Date;

import org.drools.core.time.impl.PointInTimeTrigger;

public class EmbeddedPointInTimeTrigger implements EmbeddedTrigger, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private Date nextFireTime;
	public EmbeddedPointInTimeTrigger(PointInTimeTrigger trigger) {
		this.nextFireTime = trigger.hasNextFireTime();
	}
	
	public Date getNextFireTime () {
		return nextFireTime;
	}
}