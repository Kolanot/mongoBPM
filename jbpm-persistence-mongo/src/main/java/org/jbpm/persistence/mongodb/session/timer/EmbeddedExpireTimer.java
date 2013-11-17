package org.jbpm.persistence.mongodb.session.timer;

import java.util.Date;

import org.drools.core.time.Trigger;
import org.mongodb.morphia.annotations.Embedded;

public class EmbeddedExpireTimer extends EmbeddedTimer {
	private static final long serialVersionUID = 1L;
	@Embedded
	protected EmbeddedTrigger et; 
	private int factHandleId;
	private String entryPointId;
    private String classTypeName;
	
    public EmbeddedExpireTimer() {}
	public EmbeddedExpireTimer(int factHandleId, String entryPointId, 
			String classTypeName, Trigger trigger) {
		this.et= initTrigger(trigger);
		this.factHandleId = factHandleId;
		this.entryPointId = entryPointId;
		this.classTypeName = classTypeName;
	}

	public int getFactHandleId() {
		return factHandleId;
	}

	public String getEntryPointId() {
		return entryPointId;
	}

	public String getClassTypeName() {
		return classTypeName;
	}

	public Date getTriggerNextFireTime() {
		return getTrigger() == null? null:getTrigger().getNextFireTime();
	}

	public EmbeddedTrigger getTrigger() {
		return et;
	}
	
	public void setTrigger(EmbeddedTrigger trigger) {
		this.et = trigger;
	}
}
