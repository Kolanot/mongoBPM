package org.jbpm.persistence.mongodb.session.timer;

import java.util.Date;

import org.mongodb.morphia.annotations.Embedded;
@Embedded
public interface EmbeddedTrigger {
	public Date getNextFireTime();
}