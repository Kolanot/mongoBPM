package org.jbpm.persistence.mongodb.session.timer;

import java.util.Date;

import org.drools.core.time.impl.PointInTimeTrigger;

public class EmbeddedPointInTimeTrigger implements EmbeddedTrigger {
	private Date nextFireTime;
	public EmbeddedPointInTimeTrigger(PointInTimeTrigger trigger) {
		this.nextFireTime = trigger.hasNextFireTime();
	}
	
	public Date getNextFireTime () {
		return nextFireTime;
	}
}