package org.jbpm.persistence.mongodb.session.timer;

import org.drools.core.time.Trigger;
import org.jbpm.persistence.mongodb.rule.EmbeddedActivation;
import org.mongodb.morphia.annotations.Embedded;

public class EmbeddedActivationTimer extends EmbeddedTimer {
	private static final long serialVersionUID = 1L;
	@Embedded
	private final EmbeddedActivation activation;
	@Embedded
	protected EmbeddedTrigger et; 

	public EmbeddedActivationTimer(EmbeddedActivation activation, Trigger trigger) {
		this.activation = activation;
		this.et = initTrigger(trigger);
	}

	public EmbeddedActivation geActivation() {
		return activation;
	}

	public EmbeddedTrigger getTrigger() {
		return et;
	}
	
	public void setTrigger(EmbeddedTrigger trigger) {
		this.et = trigger;
	}
}
