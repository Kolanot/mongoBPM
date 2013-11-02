package org.jbpm.persistence.mongodb.session.timer;

import org.drools.core.time.Trigger;
import org.drools.core.time.impl.CronTrigger;
import org.drools.core.time.impl.IntervalTrigger;
import org.drools.core.time.impl.PointInTimeTrigger;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Polymorphic;

@Embedded 
@Polymorphic
public abstract class EmbeddedTimer {
	public abstract EmbeddedTrigger getTrigger();
	protected EmbeddedTrigger initTrigger(Trigger trigger) {
		EmbeddedTrigger et = null;
		if (trigger == null) return et;
        if ( trigger instanceof CronTrigger ) {
            et = new EmbeddedCronTrigger((CronTrigger) trigger);
        } else if ( trigger instanceof IntervalTrigger ) {
            et = new EmbeddedIntervalTrigger((IntervalTrigger) trigger);
        } else if ( trigger instanceof PointInTimeTrigger ) {
            et = new EmbeddedPointInTimeTrigger((PointInTimeTrigger) trigger);
        } else {
        	throw new RuntimeException( "Unable to serialize Trigger for type: " + trigger.getClass() );
        }
        return et;
	}
}
