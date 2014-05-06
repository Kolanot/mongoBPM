package org.jbpm.persistence.mongodb.timer;

import java.util.Date;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.time.JobHandle;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.CronTrigger;
import org.drools.core.time.impl.IntervalTrigger;
import org.drools.core.time.impl.PointInTimeTrigger;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.process.instance.timer.TimerManager.OverdueTrigger;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoTimerMarshaller {
    static Logger logger = LoggerFactory.getLogger( MongoTimerMarshaller.class );
	public static EmbeddedProcessTimer serialize(TimerInstance timerInstance, Trigger trigger) {
			EmbeddedProcessTimer ept = new EmbeddedProcessTimer(trigger, timerInstance);
			return ept;
	}

	public static void deserialize(EmbeddedProcessTimer et, TimerManager tm, InternalKnowledgeRuntime kruntime) 
			throws ClassNotFoundException {
		if (et instanceof EmbeddedProcessTimer) {
			deserializeProcessTimer((EmbeddedProcessTimer) et, tm, kruntime);
		}
	}

	private static void deserializeProcessTimer(EmbeddedProcessTimer et,TimerManager tm,
			 InternalKnowledgeRuntime kruntime) {
		long processInstanceId = et.getTimerInstance().getProcessInstanceId();

		Trigger trigger = readTrigger(et.getTrigger());

		TimerInstance timerInstance = et.getTimerInstance();

		// check if the timer instance is not already registered to avoid
		// duplicated timers
		if (!tm.getTimerMap().containsKey(timerInstance.getId())) {
			ProcessJobContext pctx = new ProcessJobContext(timerInstance,
					trigger, processInstanceId, kruntime);
			Date date = trigger.hasNextFireTime();

			if (date != null) {
				long then = date.getTime();
				long now = pctx.getKnowledgeRuntime().getSessionClock()
						.getCurrentTime();
				// overdue timer
				if (then < now) {
					trigger = new OverdueTrigger(trigger,
							pctx.getKnowledgeRuntime());
				}
			}
			JobHandle jobHandle = tm.getTimerService().scheduleJob(TimerManager.processJob, pctx,
					trigger);
			timerInstance.setJobHandle(jobHandle);
			pctx.setJobHandle(jobHandle);

			tm.getTimerMap().put(timerInstance.getId(), timerInstance);
		}
	}

	private static Trigger readTrigger(EmbeddedTrigger et) {
		if (et == null) return null;
		if (et instanceof EmbeddedCronTrigger) {
			EmbeddedCronTrigger ect = (EmbeddedCronTrigger) et;
			CronTrigger trigger = new CronTrigger();
			trigger.setStartTime(ect.getStartTime());
			if (((EmbeddedCronTrigger) et).getEndTime() != null) {
				trigger.setEndTime(((EmbeddedCronTrigger) et).getEndTime());
			}
			trigger.setRepeatLimit(ect.getRepeatLimit());
			trigger.setRepeatCount(ect.getRepeatCount());
			trigger.setCronExpression(ect.getCronExpression());
			if (ect.getNextFireTime() != null) {
				trigger.setNextFireTime(ect.getNextFireTime());
			}
			String[] calendarNames = new String[ect.getCalendarNames().size()];
			for (int i = 0; i < calendarNames.length; i++) {
				calendarNames[i] = ect.getCalendarNames().get(i);
			}
			trigger.setCalendarNames(calendarNames);
			return trigger;
		} else if (et instanceof EmbeddedIntervalTrigger) {
			EmbeddedIntervalTrigger eit = (EmbeddedIntervalTrigger) et;
			IntervalTrigger trigger = new IntervalTrigger();
			trigger.setStartTime(eit.getStartTime());
			if (eit.getEndTime() != null) {
				trigger.setEndTime(eit.getEndTime());
			}
			trigger.setRepeatLimit(eit.getRepeatLimit());
			trigger.setRepeatCount(eit.getRepeatCount());
			if (eit.getNextFireTime() != null) {
				trigger.setNextFireTime(eit.getNextFireTime());
			}
			trigger.setPeriod(eit.getPeriod());
			String[] calendarNames = new String[eit.getCalendarNames().size()];
			for (int i = 0; i < calendarNames.length; i++) {
				calendarNames[i] = eit.getCalendarNames().get(i);
			}
			trigger.setCalendarNames(calendarNames);
			return trigger;
		} else if (et instanceof EmbeddedPointInTimeTrigger) {
			PointInTimeTrigger trigger = new PointInTimeTrigger(et
					.getNextFireTime().getTime(), null, null);
			return trigger;
		}
		throw new RuntimeException("Unable to deserialize Trigger for type: "
				+ et.getClass());
	}
}
