package org.jbpm.persistence.mongodb.session.timer;

import java.util.Date;

import org.drools.core.base.ClassObjectType;
import org.drools.core.common.AbstractWorkingMemory;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.ScheduledAgendaItem;
import org.drools.core.common.AbstractWorkingMemory.WorkingMemoryReteExpireAction;
import org.drools.core.common.Scheduler.ActivationTimerJob;
import org.drools.core.common.Scheduler.ActivationTimerJobContext;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.PersisterHelper;
import org.drools.core.marshalling.impl.ProtobufInputMarshaller;
import org.drools.core.marshalling.impl.ProtobufMessages;
import org.drools.core.marshalling.impl.ProtobufInputMarshaller.TupleKey;
import org.drools.core.marshalling.impl.ProtobufMessages.Timers.ActivationTimer;
import org.drools.core.marshalling.impl.ProtobufMessages.Timers.ExpireTimer;
import org.drools.core.marshalling.impl.ProtobufMessages.Timers.Timer;
import org.drools.core.marshalling.impl.ProtobufMessages.Timers.TimerNodeTimer;
import org.drools.core.phreak.PhreakTimerNode.Scheduler;
import org.drools.core.phreak.PhreakTimerNode.TimerNodeJobContext;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.ReteooRuleBase;
import org.drools.core.reteoo.ObjectTypeNode.ExpireJobContext;
import org.drools.core.rule.EntryPointId;
import org.drools.core.rule.SlidingTimeWindow.BehaviorJobContext;
import org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.SelfRemovalJobContext;
import org.drools.core.time.TimerService;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.CronTrigger;
import org.drools.core.time.impl.DefaultJobHandle;
import org.drools.core.time.impl.IntervalTrigger;
import org.drools.core.time.impl.PointInTimeTrigger;
import org.drools.core.time.impl.TimerJobInstance;
import org.jbpm.marshalling.impl.JBPMMessages;
import org.jbpm.marshalling.impl.ProtobufProcessMarshaller;
import org.jbpm.persistence.mongodb.rule.EmbeddedActivation;
import org.jbpm.persistence.mongodb.rule.MongoActivationKey;
import org.jbpm.persistence.mongodb.rule.MongoRuleData;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.process.instance.timer.TimerManager.OverdueTrigger;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;
import org.jbpm.process.instance.timer.TimerManager.StartProcessJobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoTimerMarshaller {
    static Logger logger = LoggerFactory.getLogger( MongoTimerMarshaller.class );
	public static EmbeddedTimer serialize(TimerJobInstance timer) {
		JobContext jobCtx = ((SelfRemovalJobContext) timer.getJobContext())
				.getJobContext();
		if (jobCtx instanceof BehaviorJobContext) {
			// BehaviorJob, no state
			BehaviorJobContext bJobCtx = (BehaviorJobContext) jobCtx;
			// write out SlidingTimeWindowContext
			SlidingTimeWindowContext slCtx = (SlidingTimeWindowContext) bJobCtx.behaviorContext;
			EventFactHandle handle = slCtx.getQueue().peek();

			EmbeddedBehaviorTimer ebt = new EmbeddedBehaviorTimer(
					handle.getId());
			return ebt;
		}
		if (jobCtx instanceof ActivationTimerJobContext) {
			ActivationTimerJobContext aJobCtx = (ActivationTimerJobContext) jobCtx;
			EmbeddedActivation activation = new EmbeddedActivation();
			activation.setRulePackage(aJobCtx.getScheduledAgendaItem()
					.getRule().getPackageName());
			activation.setRuleName(aJobCtx.getScheduledAgendaItem().getRule()
					.getName());
			activation
					.addLeftTuple(aJobCtx.getScheduledAgendaItem().getTuple());
			EmbeddedActivationTimer eat = new EmbeddedActivationTimer(
					activation, aJobCtx.getTrigger());
			return eat;
		}
		if (jobCtx instanceof ExpireJobContext) {
			ExpireJobContext ejobCtx = (ExpireJobContext) jobCtx;
			WorkingMemoryReteExpireAction expireAction = ejobCtx
					.getExpireAction();
			DefaultJobHandle jobHandle = (DefaultJobHandle) ejobCtx
					.getJobHandle();
			PointInTimeTrigger trigger = (PointInTimeTrigger) jobHandle
					.getTimerJobInstance().getTrigger();

			EmbeddedExpireTimer eet = new EmbeddedExpireTimer(expireAction
					.getFactHandle().getId(), expireAction.getNode()
					.getEntryPoint().getEntryPointId(),
					((ClassObjectType) expireAction.getNode().getObjectType())
							.getClassType().getName(), trigger);
			return eet;
		}
		if (jobCtx instanceof TimerNodeJobContext) {
			TimerNodeJobContext tnJobCtx = (TimerNodeJobContext) jobCtx;
			LeftTuple leftTuple = tnJobCtx.getLeftTuple();
			EmbeddedPhreakTimerNodeTimer eptnt = new EmbeddedPhreakTimerNodeTimer(
					tnJobCtx.getTimerNodeId(), tnJobCtx.getTrigger());
			for (LeftTuple entry = leftTuple; entry != null; entry = entry
					.getParent()) {
				eptnt.getTupleHandles().add(entry.getLastHandle().getId());
			}
			return eptnt;
		}
		if (jobCtx instanceof ProcessJobContext) {
			ProcessJobContext pJobCtx = (ProcessJobContext) jobCtx;
			TimerInstance timerInstance = pJobCtx.getTimer();
			Trigger trigger = pJobCtx.getTrigger();
			logger.info("trigger:" + trigger);
			EmbeddedProcessTimer ept = new EmbeddedProcessTimer(trigger,
					timerInstance);
			return ept;
		}
		if (jobCtx instanceof StartProcessJobContext) {
			// do not store StartProcess timers as they are registered whenever
			// session starts
			return null;
		}
		return null;
	}

	public static void deserialize(EmbeddedTimer et, AbstractWorkingMemory wm,
			MongoRuleData ruleData) throws ClassNotFoundException {
		if (et instanceof EmbeddedActivationTimer) {

		} else if (et instanceof EmbeddedBehaviorTimer) {

		} else if (et instanceof EmbeddedExpireTimer) {
			deserializeExpireTimer((EmbeddedExpireTimer) et, wm, ruleData);
		} else if (et instanceof EmbeddedPhreakTimerNodeTimer) {
			deserializePhreakTimerNodeTimer((EmbeddedPhreakTimerNodeTimer) et,
					ruleData);
		} else if (et instanceof EmbeddedProcessTimer) {
			deserializeProcessTimer((EmbeddedProcessTimer) et, wm);
		}
	}

	public void deserialize(EmbeddedActivationTimer eat, AbstractWorkingMemory wm, MongoRuleData ruleData) {
		EmbeddedActivation ea = eat.geActivation();

		MongoActivationKey key = new MongoActivationKey(ea);
		LeftTuple leftTuple = ruleData.getMarshallerReaderContext().filter.getTuplesCache().get(key);
		
		ScheduledAgendaItem item = (ScheduledAgendaItem) leftTuple.getObject();

		Trigger trigger = readTrigger(eat.getTrigger());

		InternalAgenda agenda = (InternalAgenda) wm.getAgenda();
		ActivationTimerJob job = new ActivationTimerJob();
		ActivationTimerJobContext ctx = new ActivationTimerJobContext(trigger, item, agenda);

		JobHandle jobHandle = ((InternalWorkingMemory) agenda
				.getWorkingMemory()).getTimerService().scheduleJob(job, ctx, trigger);
		item.setJobHandle(jobHandle);
	}

	private static void deserializeExpireTimer(EmbeddedExpireTimer eet,
			AbstractWorkingMemory wm, MongoRuleData ruleData)
			throws ClassNotFoundException {
		InternalFactHandle factHandle = ruleData.getCachedHandle(eet.getFactHandleId());
		
		EntryPointNode epn = ((ReteooRuleBase) wm.getRuleBase()).getRete()
				.getEntryPointNode(new EntryPointId(eet.getEntryPointId()));
		
		Class<?> cls = ((ReteooRuleBase) wm.getRuleBase()).getRootClassLoader()
				.loadClass(eet.getClassTypeName());
		
		ObjectTypeNode otn = epn.getObjectTypeNodes().get(new ClassObjectType(cls));

		TimerService clock = wm.getTimerService();

		JobContext jobctx = new ExpireJobContext(
				new WorkingMemoryReteExpireAction(factHandle, otn), wm);
		
		JobHandle handle = clock.scheduleJob(ObjectTypeNode.job, jobctx,
				new PointInTimeTrigger(eet.getTriggerNextFireTime().getTime(),
						null, null));
		
		jobctx.setJobHandle(handle);
	}

	private static void deserializePhreakTimerNodeTimer(EmbeddedPhreakTimerNodeTimer ept, 
			MongoRuleData ruleData) {
		int timerNodeId = ept.getTimerNodeId();
		TupleKey tuple = new TupleKey(ept.getTupleFactHandleArray());
		Trigger trigger = readTrigger(ept.getTrigger());

		Scheduler scheduler = ruleData.getMarshallerReaderContext()
				.removeTimerNodeScheduler(timerNodeId, tuple);
		if (scheduler != null) {
			scheduler.schedule(trigger);
		}
	}

	private static void deserializeProcessTimer(EmbeddedProcessTimer et,
			AbstractWorkingMemory wm) {
		TimerService ts = wm.getTimerService();

		long processInstanceId = et.getTimerInstance().getProcessInstanceId();

		Trigger trigger = readTrigger(et.getTrigger());

		TimerInstance timerInstance = et.getTimerInstance();

		TimerManager tm = ((InternalProcessRuntime) wm.getProcessRuntime())
				.getTimerManager();

		// check if the timer instance is not already registered to avoid
		// duplicated timers
		if (!tm.getTimerMap().containsKey(timerInstance.getId())) {
			ProcessJobContext pctx = new ProcessJobContext(timerInstance,
					trigger, processInstanceId, wm.getKnowledgeRuntime());
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
			JobHandle jobHandle = ts.scheduleJob(TimerManager.processJob, pctx,
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
