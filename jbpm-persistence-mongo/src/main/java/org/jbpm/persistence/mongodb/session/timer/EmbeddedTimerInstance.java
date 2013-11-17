package org.jbpm.persistence.mongodb.session.timer;

import java.util.Date;

import org.jbpm.process.instance.timer.TimerInstance;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class EmbeddedTimerInstance implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
    private long id;
    private long timerId;
    private long delay;
    private long period;
    private Date activated;
    private Date lastTriggered;
    private long processInstanceId;
    private int repeatLimit = -1;
    private int sessionId;
    private String cronExpression;
    
	public EmbeddedTimerInstance(TimerInstance instance) {
		this.id = instance.getId();
		this.timerId = instance.getTimerId();
		this.delay = instance.getDelay();
		this.period = instance.getPeriod();
		this.activated = instance.getActivated();
		this.lastTriggered = instance.getLastTriggered();
		this.processInstanceId = instance.getProcessInstanceId();
		this.repeatLimit = instance.getRepeatLimit();
		this.sessionId = instance.getSessionId();
		this.cronExpression = instance.getCronExpression();
	}
	
	public EmbeddedTimerInstance() {}
	
	public TimerInstance getTimerInstance () {
		TimerInstance instance = new TimerInstance();
		instance.setId(id);
		instance.setTimerId(timerId);
		instance.setDelay(delay);
		instance.setPeriod(period);
		instance.setActivated(activated);
		instance.setLastTriggered(lastTriggered);
		instance.setProcessInstanceId(processInstanceId);
		instance.setRepeatLimit(repeatLimit);
		instance.setSessionId(sessionId);
		instance.setCronExpression(cronExpression);
		return instance;
	}
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getTimerId() {
		return timerId;
	}

	public void setTimerId(long timerId) {
		this.timerId = timerId;
	}

	public long getDelay() {
        return delay;
    }
    
    public void setDelay(long delay) {
        this.delay = delay;
    }
    
    public long getPeriod() {
        return period;
    }
    
    public void setPeriod(long period) {
        this.period = period;
    }

    public Date getActivated() {
		return activated;
	}

	public void setActivated(Date activated) {
		this.activated = activated;
	}

	public void setLastTriggered(Date lastTriggered) {
    	this.lastTriggered = lastTriggered;
    }
    
    public Date getLastTriggered() {
    	return lastTriggered;
    }

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

    @Override
    public String toString() {
        return "TimerInstance [id=" + id + ", timerId=" + timerId + ", delay=" + delay + ", period=" + period + ", activated=" + activated + ", lastTriggered=" + lastTriggered + ", processInstanceId=" + processInstanceId
               + "]";
    }

    public int getRepeatLimit() {
        return repeatLimit;
    }

    public void setRepeatLimit(int stopAfter) {
        this.repeatLimit = stopAfter;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
}
