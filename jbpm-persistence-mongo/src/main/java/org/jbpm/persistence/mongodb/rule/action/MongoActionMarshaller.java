package org.jbpm.persistence.mongodb.rule.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.drools.core.beliefsystem.simple.SimpleBeliefSystem;
import org.drools.core.beliefsystem.simple.BeliefSystemLogicalCallback;
import org.drools.core.common.AbstractWorkingMemory;
import org.drools.core.common.AgendaGroupQueueImpl;
import org.drools.core.common.BaseNode;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalRuleBase;
import org.drools.core.common.InternalRuleFlowGroup;
import org.drools.core.common.RuleFlowGroupImpl;
import org.drools.core.common.WorkingMemoryAction;
import org.drools.core.common.AbstractWorkingMemory.WorkingMemoryReteAssertAction;
import org.drools.core.common.AbstractWorkingMemory.WorkingMemoryReteExpireAction;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.ProtobufMessages;
import org.drools.core.marshalling.impl.RuleBaseNodes;
import org.drools.core.marshalling.impl.ProtobufMessages.ActionQueue;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.PropagationQueuingNode;
import org.drools.core.reteoo.WindowNode;
import org.drools.core.reteoo.PropagationQueuingNode.PropagateAction;
import org.drools.core.reteoo.WindowNode.WindowMemory;
import org.drools.core.rule.Behavior;
import org.drools.core.rule.Rule;
import org.drools.core.rule.SlidingTimeWindow;
import org.drools.core.rule.SlidingTimeWindow.BehaviorExpireWMAction;
import org.drools.core.rule.SlidingTimeWindow.SlidingTimeWindowContext;
import org.drools.core.spi.Activation;
import org.drools.core.spi.PropagationContext;
import org.jbpm.persistence.mongodb.instance.EmbeddedProcessInstance;
import org.jbpm.persistence.mongodb.rule.EmbeddedActivation;
import org.jbpm.persistence.mongodb.rule.MongoActivationKey;
import org.jbpm.persistence.mongodb.rule.MongoActivationsFilter;
import org.jbpm.persistence.mongodb.rule.MongoRuleData;
import org.jbpm.process.instance.event.DefaultSignalManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.drools.core.spi.RuleFlowGroup;

public class MongoActionMarshaller {
	private final static Logger log = Logger.getLogger(MongoActionMarshaller.class.getName()); 
	
	public static EmbeddedWorkingMemoryAction serialize(WorkingMemoryAction action) 
			throws IOException, ClassNotFoundException {
		ProtobufMessages.ActionQueue.Action actionMsg = action.serialize(null);
		if (action instanceof PropagationQueuingNode.PropagateAction) {
			int propagationQueuingNodeId = actionMsg.getPropagate().getNodeId(); 
			EmbeddedPropagateAction epa = new EmbeddedPropagateAction(propagationQueuingNodeId);
			return epa;
		} else if (action instanceof SlidingTimeWindow.BehaviorExpireWMAction) {
			EmbeddedWorkingMemoryBehaviorExpire expire = new EmbeddedWorkingMemoryBehaviorExpire(actionMsg.getBehaviorExpire().getNodeId());
			return expire;
		} else if (action instanceof RuleFlowGroupImpl.DeactivateCallback) {
			EmbeddedRuleFlowGroupDeactivateCallback edc = new EmbeddedRuleFlowGroupDeactivateCallback(actionMsg.getDeactivateCallback().getRuleflowGroup());
			return edc;
		} else if (action instanceof AgendaGroupQueueImpl.DeactivateCallback) {
			EmbeddedAgendaGroupDeactivateCallback edc = new EmbeddedAgendaGroupDeactivateCallback(actionMsg.getDeactivateCallback().getRuleflowGroup());
			return edc;
		} else if (action instanceof DefaultSignalManager.SignalAction) {
			String signalType;
			Object event = null;
			ObjectInputStream in = toInputStream(action);
			signalType = in.readUTF();
			if (in.readBoolean()) {
				event = in.readObject();
			}
			if (event instanceof ProcessInstance) {
				log.info("This is a process instance:" + event);
				EmbeddedProcessInstance epi = new EmbeddedProcessInstance();
				epi.setProcessInstanceId(((ProcessInstance)event).getId());
				event = epi;
			}
			EmbeddedSignalAction esa = new EmbeddedSignalAction(signalType, event);
			return esa;
		} else if (action instanceof DefaultSignalManager.SignalProcessInstanceAction) {
			long processInstanceId;
			String signalType;
			Object event = null;
			ObjectInputStream in = toInputStream(action);
			processInstanceId = in.readLong();
			signalType = in.readUTF();
			if (in.readBoolean()) {
				event = in.readObject();
			}
			EmbeddedSignalProcessInstanceAction espi = new EmbeddedSignalProcessInstanceAction(processInstanceId, signalType, event);
			return espi;
		} else if (action instanceof BeliefSystemLogicalCallback) {
			ActionQueue.LogicalRetract lr = actionMsg.getLogicalRetract();
			int handleId = lr.getHandleId();
			boolean fullyRetract = lr.getFullyRetract();
			boolean update = lr.getUpdate();
			EmbeddedLogicalRetractCallback lrc = new EmbeddedLogicalRetractCallback(handleId, fullyRetract, update);
			EmbeddedActivation activation = new EmbeddedActivation();
			activation.setRulePackage(lr.getActivation().getPackageName());
			activation.setRuleName(lr.getActivation().getRuleName());
			activation.getTupleFactHandleList().addAll(lr.getActivation().getTuple().getHandleIdList());
			lrc.setActivation(activation);
			return lrc;
		} else if (action instanceof AbstractWorkingMemory.WorkingMemoryReteAssertAction) {
			EmbeddedWorkingMemoryReteAssertAction raa = new EmbeddedWorkingMemoryReteAssertAction();
			ActionQueue.Assert ast = actionMsg.getAssert();
			raa.setFactHandleId(ast.getHandleId());
			raa.setRemoveLogical(ast.getRemoveLogical());
			raa.setUpdateEqualsMap(ast.getUpdateEqualsMap());
			raa.setRuleOriginPackage(ast.getOriginPkgName());
			raa.setRuleOriginName(ast.getOriginRuleName());
			raa.getLeftTupleHandleIdList().addAll(ast.getTuple().getHandleIdList());
			return raa;
		} else if (action instanceof AbstractWorkingMemory.WorkingMemoryReteExpireAction) {
			ActionQueue.Expire exp = actionMsg.getExpire();
			EmbeddedWorkingMemoryReteExpireAction rea = new EmbeddedWorkingMemoryReteExpireAction(exp.getNodeId(), exp.getHandleId());
			return rea;
		} else  {
			return null;
		}
	}

	private static ObjectInputStream toInputStream(WorkingMemoryAction action)
			throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(os);
		action.writeExternal(out);
		out.close();
		byte[] objectByteArray = os.toByteArray();
		os.close();
		ByteArrayInputStream is = new ByteArrayInputStream(objectByteArray);
		ObjectInputStream in = new ObjectInputStream(is);
		return in;
	}

	public static WorkingMemoryAction deserialize(EmbeddedWorkingMemoryAction ewma, AbstractWorkingMemory wm, MongoRuleData ruleData) {
		WorkingMemoryAction action = null;
		InternalRuleBase ruleBase = (InternalRuleBase) ((KnowledgeBaseImpl) wm.getRuleBase()).ruleBase;
		Map<Integer, BaseNode> sinks = RuleBaseNodes.getNodeMap( ruleBase );
		
		if (ewma instanceof EmbeddedPropagateAction) {
			
			EmbeddedPropagateAction epa = (EmbeddedPropagateAction)ewma;
			int propagationQueuingNodeId = epa.getPropagationQueuingNodeId(); 
			PropagationQueuingNode node = (PropagationQueuingNode)sinks.get(propagationQueuingNodeId);
			action = new PropagationQueuingNode.PropagateAction(node);

		} else if (ewma instanceof EmbeddedWorkingMemoryBehaviorExpire) {

			EmbeddedWorkingMemoryBehaviorExpire ebe = (EmbeddedWorkingMemoryBehaviorExpire)ewma;
			int nodeId = ebe.getNodeId();
			WindowNode windowNode = (WindowNode) sinks.get( nodeId );
            WindowMemory memory = (WindowMemory) wm.getNodeMemory( windowNode );
            Object[] behaviorContext = ( Object[]  ) memory.behaviorContext;
            int i = 0;
            Behavior behavior = (SlidingTimeWindow) windowNode.getBehaviors()[i];
            Object context =  ( SlidingTimeWindowContext ) behaviorContext[i];
            PropagationContext pctx = null;
            action = new BehaviorExpireWMAction(nodeId, behavior, memory, context, pctx);

		} else if (ewma instanceof EmbeddedRuleFlowGroupDeactivateCallback) {
			
			EmbeddedRuleFlowGroupDeactivateCallback edc = (EmbeddedRuleFlowGroupDeactivateCallback)ewma;
			String ruleGroupName = edc.getRuleFlowGroupName();
			RuleFlowGroup ruleFlowGroup = wm.getAgenda().getRuleFlowGroup(ruleGroupName);
			action = new RuleFlowGroupImpl.DeactivateCallback((InternalRuleFlowGroup) ruleFlowGroup);
			
		} else if (ewma instanceof EmbeddedAgendaGroupDeactivateCallback) {

			EmbeddedAgendaGroupDeactivateCallback edc = (EmbeddedAgendaGroupDeactivateCallback)ewma;
			String ruleGroupName = edc.getRuleFlowGroupName();
			RuleFlowGroup ruleFlowGroup = wm.getAgenda().getRuleFlowGroup(ruleGroupName);
			action = new AgendaGroupQueueImpl.DeactivateCallback((InternalRuleFlowGroup) ruleFlowGroup);
			
		} else if (ewma instanceof EmbeddedSignalAction) {
			
			EmbeddedSignalAction esa = (EmbeddedSignalAction)ewma;
			Object signal = esa.getSignal();
			/*
			if (signal instanceof EmbeddedProcessInstance) {
				long procInstId = ((EmbeddedProcessInstance)signal).getProcessInstanceId();
				log.info("This is a process instance:" + procInstId);
				signal = MongoSessionMap.INSTANCE.getProcessInstance(procInstId, false);
			}
			*/
			action = new DefaultSignalManager.SignalAction(esa.getSignalType(),signal);
			
		} else if (ewma instanceof EmbeddedSignalProcessInstanceAction) {
			
			EmbeddedSignalProcessInstanceAction espi = (EmbeddedSignalProcessInstanceAction)ewma;
			action = new DefaultSignalManager.SignalProcessInstanceAction(espi.getProcessInstanceId(), 
					espi.getSignalType(), espi.getEvent());
			
		} else if (ewma instanceof EmbeddedLogicalRetractCallback) {
			
			EmbeddedLogicalRetractCallback elrc = (EmbeddedLogicalRetractCallback)ewma;
			InternalFactHandle handle = getFactHandle(elrc.getHandleId(), ruleData);
			Activation activation = getActivation(elrc.getActivation(), wm);
			PropagationContext context = activation.getPropagationContext();
			action = new  BeliefSystemLogicalCallback(handle, context, activation, elrc.hasUpdate(), elrc.isFullyRetract());
			
		} else if (ewma instanceof EmbeddedWorkingMemoryReteAssertAction) {
			
			EmbeddedWorkingMemoryReteAssertAction ea = (EmbeddedWorkingMemoryReteAssertAction)ewma;
			InternalFactHandle factHandle = getFactHandle(ea.getFactHandleId(), ruleData);
            boolean removeLogical = ea.isRemoveLogical();
            boolean updateEqualsMap = ea.isUpdateEqualsMap();
            org.drools.core.rule.Package pkg = ruleBase.getPackage( ea.getRuleOriginPackage());
            Rule ruleOrigin = pkg.getRule( ea.getRuleOriginName());
            LeftTuple leftTuple = getLeftTuple(ea, wm);
			action = new WorkingMemoryReteAssertAction(factHandle, removeLogical, updateEqualsMap, ruleOrigin, leftTuple);
			
		} else if (ewma instanceof EmbeddedWorkingMemoryReteExpireAction) {
			
			EmbeddedWorkingMemoryReteExpireAction ee = (EmbeddedWorkingMemoryReteExpireAction)ewma;
			InternalFactHandle factHandle = getFactHandle(ee.getFactHandelId(), ruleData);
			ObjectTypeNode node = (ObjectTypeNode)sinks.get(ee.getObjectTypeNodeId());
			action = new WorkingMemoryReteExpireAction(factHandle, node);
		}
		return action;
	}

	private static InternalFactHandle getFactHandle(int factHandleId, MongoRuleData ruleData) {
		return ruleData.getCachedHandle(factHandleId);
	}
	
	private static Activation getActivation(EmbeddedActivation ea, AbstractWorkingMemory wm) {
		InternalAgenda agenda  = (InternalAgenda) wm.getAgenda();
		MongoActivationsFilter filter = (MongoActivationsFilter) agenda.getActivationsFilter();
		Activation activation = (Activation)filter.getTuplesCache().get(new MongoActivationKey(ea));
		return activation;
	}
	
	private static LeftTuple getLeftTuple(EmbeddedWorkingMemoryReteAssertAction ea, AbstractWorkingMemory wm) {
		InternalAgenda agenda  = (InternalAgenda) wm.getAgenda();
		MongoActivationsFilter filter = (MongoActivationsFilter) agenda.getActivationsFilter();
		LeftTuple tuple = filter.getTuplesCache().get(new MongoActivationKey(ea.getRuleOriginPackage(), ea.getRuleOriginName(), ea.getLeftTupleHandleIdList()));
		return tuple;
	}
}
