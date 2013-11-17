package org.jbpm.persistence.mongodb.session.timer;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.time.Trigger;
import org.mongodb.morphia.annotations.Embedded;

public class EmbeddedPhreakTimerNodeTimer extends EmbeddedTimer {
	private static final long serialVersionUID = 1L;
	@Embedded
	protected EmbeddedTrigger et; 
	private final int timerNodeId;
	private List<Integer> tupleHandles = new ArrayList<Integer>();

	public EmbeddedPhreakTimerNodeTimer(int timerNodeId, Trigger trigger) {
		this.timerNodeId = timerNodeId;
		this.et = initTrigger(trigger);
	}
	public int getTimerNodeId() {
		return timerNodeId;
	}
	public List<Integer> getTupleHandles() {
		return tupleHandles;
	}
	public int[] getTupleFactHandleArray() {
		int i = 0;
		int[] ret = new int[tupleHandles.size()];
		for (int handleId: tupleHandles) {
			ret[i++] = handleId;
		}
		return ret;
	}
	public EmbeddedTrigger getTrigger() {
		return et;
	}
	public void setTrigger(EmbeddedTrigger trigger) {
		this.et = trigger;
	}
}
