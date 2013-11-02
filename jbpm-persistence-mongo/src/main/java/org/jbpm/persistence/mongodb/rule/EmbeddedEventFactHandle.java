package org.jbpm.persistence.mongodb.rule;

import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;

public class EmbeddedEventFactHandle extends EmbeddedFactHandle {
	private long startTimestamp;
    private long duration;
    private boolean expired;
    private long activationsCount;

	public EmbeddedEventFactHandle(InternalFactHandle ifh) {
		super(ifh);
		if ( ifh instanceof EventFactHandle ) {
            EventFactHandle efh = (EventFactHandle) ifh;
			this.startTimestamp = efh.getStartTimestamp();
			this.duration = efh.getDuration();
			this.expired = efh.isExpired();
			this.activationsCount = efh.getActivationsCount();
		}
	}

    public long getStartTimestamp() {
		return startTimestamp;
	}

	public long getDuration() {
		return duration;
	}

	public boolean isExpired() {
		return expired;
	}

	public long getActivationsCount() {
		return activationsCount;
	}
}
