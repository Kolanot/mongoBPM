package org.jbpm.persistence.mongodb.session.timer;

import org.drools.core.time.Trigger;
import org.jbpm.process.instance.timer.TimerInstance;
import org.mongodb.morphia.annotations.Embedded;

public class EmbeddedProcessTimer extends EmbeddedTimer {
	private static final long serialVersionUID = 1L;
	@Embedded
	private EmbeddedTimerInstance timerInstance;
	@Embedded
	protected EmbeddedTrigger trigger; 
	
	public EmbeddedProcessTimer(Trigger trigger, 
			TimerInstance timerInstance) {
		this.timerInstance = new EmbeddedTimerInstance(timerInstance);
		this.trigger = initTrigger(trigger);
	}

	public EmbeddedProcessTimer() {
		super();
	}
	
	public TimerInstance getTimerInstance() {
		if (timerInstance == null) return null;
		return timerInstance.getTimerInstance();
	}
	
	public EmbeddedTrigger getTrigger() {
		return trigger;
	}
	
	public void setTrigger(EmbeddedTrigger trigger) {
		this.trigger = trigger;
	}
}
