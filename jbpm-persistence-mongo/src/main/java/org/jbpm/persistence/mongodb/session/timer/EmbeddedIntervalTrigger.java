package org.jbpm.persistence.mongodb.session.timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.drools.core.time.impl.CronTrigger;
import org.drools.core.time.impl.IntervalTrigger;

public class EmbeddedIntervalTrigger implements EmbeddedTrigger, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private Date startTime;
	private Date endTime;
	private int repeatLimit;
	private int repeatCount;
	private long period;
	private Date nextFireTime;
	private List<String> calendarNames = new ArrayList<String>();
	public EmbeddedIntervalTrigger() {}
	public EmbeddedIntervalTrigger(IntervalTrigger trigger) {
		super();
		this.startTime = trigger.getStartTime();
		this.endTime = trigger.getEndTime();
		this.repeatLimit = trigger.getRepeatLimit();
		this.repeatCount = trigger.getRepeatCount();
		this.period = trigger.getPeriod();
		this.nextFireTime = trigger.getNextFireTime();
        if ( trigger.getCalendarNames() != null ) {
            for ( String calendarName : trigger.getCalendarNames() ) {
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
	public long getPeriod() {
		return period;
	}
	public Date getNextFireTime() {
		return nextFireTime;
	}
	public List<String> getCalendarNames() {
		return calendarNames;
	}
}