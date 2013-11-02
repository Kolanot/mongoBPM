package org.jbpm.persistence.mongodb.session.timer;

import org.mongodb.morphia.annotations.Embedded;

public class EmbeddedBehaviorTimer extends EmbeddedTimer {
	private int eventFactHandleId;
	public EmbeddedBehaviorTimer() {}
	public EmbeddedBehaviorTimer(int eventFactHandleId) {
		this.eventFactHandleId = eventFactHandleId;
	}

	public int getEventFactHandleId() {
		return eventFactHandleId;
	}

	public EmbeddedTrigger getTrigger() {
		return null;
	}
}
