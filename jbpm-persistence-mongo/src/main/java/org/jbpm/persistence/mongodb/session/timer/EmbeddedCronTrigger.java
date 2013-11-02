package org.jbpm.persistence.mongodb.session.timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.drools.core.time.impl.CronTrigger;

public class EmbeddedCronTrigger implements EmbeddedTrigger {
	private final Date startTime;
	private final Date endTime;
	private final int repeatLimit;
	private final int repeatCount;
	private final String cronExpression;
	private final Date nextFireTime;
	private final List<String> calendarNames = new ArrayList<String>();
	public EmbeddedCronTrigger(CronTrigger cronTrigger) {
		super();
		this.startTime = cronTrigger.getStartTime();
		this.endTime = cronTrigger.getEndTime();
		this.repeatLimit = cronTrigger.getRepeatLimit();
		this.repeatCount = cronTrigger.getRepeatCount();
		this.cronExpression = cronTrigger.getCronEx().getCronExpression();
		this.nextFireTime = cronTrigger.getNextFireTime();
        if ( cronTrigger.getCalendarNames() != null ) {
            for ( String calendarName : cronTrigger.getCalendarNames() ) {
                calendarNames.add( calendarName );
            }
        }

	}
	
	public Date getStartTime() {
		return startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public int getRepeatLimit() {
		return repeatLimit;
	}
	public int getRepeatCount() {
		return repeatCount;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public Date getNextFireTime() {
		return nextFireTime;
	}
	public List<String> getCalendarNames() {
		return calendarNames;
	}
}