package org.jbpm.persistence.mongodb.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.drools.core.beliefsystem.BeliefSet;
import org.drools.core.common.AbstractWorkingMemory;
import org.drools.core.common.ActivationIterator;
import org.drools.core.common.AgendaGroupQueueImpl;
import org.drools.core.common.AgendaItem;
import org.drools.core.common.BaseNode;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.EqualityKey;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalRuleBase;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.InternalWorkingMemoryEntryPoint;
import org.drools.core.common.LeftTupleIterator;
import org.drools.core.common.LogicalDependency;
import org.drools.core.common.Memory;
import org.drools.core.common.NamedEntryPoint;
import org.drools.core.common.NodeMemories;
import org.drools.core.common.ObjectStore;
import org.drools.core.common.ObjectTypeConfigurationRegistry;
import org.drools.core.common.QueryElementFactHandle;
import org.drools.core.common.TruthMaintenanceSystem;
import org.drools.core.common.WorkingMemoryAction;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.phreak.RuleAgendaItem;
import org.drools.core.reteoo.BetaMemory;
import org.drools.core.reteoo.BetaNode;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.LeftTupleSink;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.NodeTypeEnums;
import org.drools.core.reteoo.ObjectSink;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.ObjectTypeConf;
import org.drools.core.reteoo.ObjectTypeNode;
import org.drools.core.reteoo.PropagationQueuingNode;
import org.drools.core.reteoo.QueryRiaFixerNode;
import org.drools.core.reteoo.QueryTerminalNode;
import org.drools.core.reteoo.RightInputAdapterNode;
import org.drools.core.reteoo.RightTuple;
import org.drools.core.reteoo.RuleTerminalNode;
import org.drools.core.reteoo.WindowNode;
import org.drools.core.reteoo.AccumulateNode.AccumulateContext;
import org.drools.core.reteoo.AccumulateNode.AccumulateMemory;
import org.drools.core.reteoo.FromNode.FromMemory;
import org.drools.core.reteoo.ObjectTypeNode.ObjectTypeNodeMemory;
import org.drools.core.reteoo.QueryElementNode.QueryElementNodeMemory;
import org.drools.core.rule.Rule;
import org.drools.core.spi.Activation;
import org.drools.core.spi.AgendaGroup;
import org.drools.core.util.FastIterator;
import org.drools.core.util.LinkedListEntry;
import org.drools.core.util.ObjectHashMap;
import org.jbpm.persistence.mongodb.MongoSessionStore;
import org.jbpm.persistence.mongodb.object.MongoSerializable;
import org.jbpm.persistence.mongodb.object.PersistenceStrategyHelper;
import org.jbpm.persistence.mongodb.object.SessionObjectPersistenceStrategy;
import org.jbpm.persistence.mongodb.rule.action.MongoActionMarshaller;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedAccumulateNodeMemory;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedNodeMemory;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedQueryElementNodeMemory;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedFromNodeMemory;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedRIANodeMemory;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedBeliefSet;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedEqualityKey;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedLogicalDependency;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedTruthMaintenanceSystem;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.rule.EntryPoint;

public class MongoRuleOutputMarshaller {
	public static void serialize(MongoRuleData ruleData, AbstractWorkingMemory wm,
			KieBase kbase, Environment env) throws ClassNotFoundException,
			IOException {
		wm.getAgenda().unstageActivations();
		evaluateRuleActivations(wm);
		writeRuleData(ruleData, wm, kbase, env);
	}

	private static void writeRuleData(MongoRuleData ruleData, AbstractWorkingMemory wm,
			KieBase kbase, Environment env) throws ClassNotFoundException,
			IOException {

		EmbeddedFactHandleFactory factory = new EmbeddedFactHandleFactory(
				wm.getFactHandleFactory());
		ruleData.setFactHandleFactory(factory);

		EmbeddedFactHandle ifh = new EmbeddedFactHandle(
				wm.getInitialFactHandle());
		ruleData.setInitialFacthandle(ifh);

		ruleData.setAgenda(writeAgenda(wm));

		writeNodeMemories(ruleData, wm, kbase);

		for (EntryPoint wmep : wm.getEntryPoints().values()) {
			EmbeddedEntryPoint eep = new EmbeddedEntryPoint(wmep.getEntryPointId());

			writeObjectTypeConfiguration(wm,
					((InternalWorkingMemoryEntryPoint) wmep)
							.getObjectTypeConfigurationRegistry(), eep);

			writeFactHandles(env, eep,
					((NamedEntryPoint) wmep).getObjectStore());

			writeTruthMaintenanceSystem(env, wmep, eep);
			ruleData.getEntrypoints().put(eep.getEntryPointId(), eep);
		}

		writeActionQueue(wm, ruleData);
	}

	private static void evaluateRuleActivations(AbstractWorkingMemory wm) {
		// ET: NOTE: initially we were only resolving partially evaluated rules
		// but some tests fail because of that. Have to resolve all rule agenda
		// items
		// in order to fix the tests

		// find all partially evaluated rule activations
		// ActivationIterator it = ActivationIterator.iterator( wm );
		// Set<String> evaluated = new HashSet<String>();
		// for ( org.drools.core.spi.Activation item =
		// (org.drools.core.spi.Activation) it.next(); item != null; item =
		// (org.drools.core.spi.Activation) it.next() ) {
		// if ( !item.isRuleAgendaItem() ) {
		// evaluated.add(
		// item.getRule().getPackageName()+"."+item.getRule().getName() );
		// }
		// }
		// need to evaluate all lazy partially evaluated activations before
		// serializing
		boolean dirty = true;
		while (dirty) {
			for (Activation activation : wm.getAgenda().getActivations()) {
				if (activation.isRuleAgendaItem() /*
												 * && evaluated.contains(
												 * activation
												 * .getRule().getPackageName
												 * ()+"."
												 * +activation.getRule().getName
												 * () )
												 */) {
					// evaluate it
					((RuleAgendaItem) activation).getRuleExecutor()
							.reEvaluateNetwork(wm, null, false);
					((RuleAgendaItem) activation).getRuleExecutor()
							.removeRuleAgendaItemWhenEmpty(wm);
				}
			}
			dirty = false;
			if (((InternalRuleBase) wm.getRuleBase()).getConfiguration()
					.isPhreakEnabled()) {
				// network evaluation with phreak and TMS may make previous
				// processed rules dirty again, so need to reprocess until all
				// is flushed.
				for (Activation activation : wm.getAgenda().getActivations()) {
					if (activation.isRuleAgendaItem()
							&& ((RuleAgendaItem) activation).getRuleExecutor()
									.isDirty()) {
						dirty = true;
						break;
					}
				}
			}
		}
	}

	private static EmbeddedAgenda writeAgenda(AbstractWorkingMemory wm) {
		EmbeddedAgenda ea = new EmbeddedAgenda();

		InternalAgenda agenda = (InternalAgenda) wm.getAgenda();
		AgendaGroup[] agendaGroups = (AgendaGroup[]) agenda
				.getAgendaGroupsMap().values()
				.toArray(new AgendaGroup[agenda.getAgendaGroupsMap().size()]);
		for (AgendaGroup ag : agendaGroups) {
			AgendaGroupQueueImpl group = (AgendaGroupQueueImpl) ag;
			EmbeddedAgenda.EmbeddedAgendaGroup eg = new EmbeddedAgenda.EmbeddedAgendaGroup(
					group);

			Map<Long, String> nodeInstances = group.getNodeInstances();
			for (Map.Entry<Long, String> entry : nodeInstances.entrySet()) {
				EmbeddedAgenda.EmbeddedAgendaGroup.NodeInstance ni = new EmbeddedAgenda.EmbeddedAgendaGroup.NodeInstance(
						entry.getKey(), entry.getValue());
				eg.getNodeInstances().add(ni);
			}

			ea.getAgendaGroups().add(eg);
		}

		LinkedList<AgendaGroup> focusStack = agenda.getStackList();
		for (Iterator<AgendaGroup> it = focusStack.iterator(); it.hasNext();) {
			AgendaGroup group = it.next();
			ea.getGroupStack().add(group.getName());
		}

		// serialize all dormant activations
		@SuppressWarnings("rawtypes")
		org.drools.core.util.Iterator it = ActivationIterator.iterator(wm);
		List<org.drools.core.spi.Activation> dormant = new ArrayList<org.drools.core.spi.Activation>();
		for (org.drools.core.spi.Activation item = (org.drools.core.spi.Activation) it
				.next(); item != null; item = (org.drools.core.spi.Activation) it
				.next()) {
			if (!item.isQueued()) {
				dormant.add(item);
			}
		}

		for (org.drools.core.spi.Activation activation : dormant) {
			ea.addMatch(writeActivation((AgendaItem) activation));
		}

		// serialize all network evaluator activations
		for (Activation activation : agenda.getActivations()) {
			if (activation.isRuleAgendaItem()) {
				// serialize it
				ea.addRuleActivation(writeActivation((AgendaItem) activation));
			}
		}

		return ea;
	}

	private static EmbeddedActivation writeActivation(AgendaItem agendaItem) {
		EmbeddedActivation _activation = new EmbeddedActivation();

		Rule rule = agendaItem.getRule();
		_activation.setRulePackage(rule.getPackage());
		_activation.setRuleName(rule.getName());
		_activation.setSalience(agendaItem.getSalience());
		_activation.setQueued(agendaItem.isQueued());
		_activation.setEvaluated(agendaItem.isRuleAgendaItem());
		addLeftTuple(_activation.getTupleFactHandleList(),
				agendaItem.getTuple());

		if (agendaItem.getActivationGroupNode() != null) {
			_activation.setActivationGroupName(agendaItem
					.getActivationGroupNode().getActivationGroup().getName());
		}

		if (agendaItem.getFactHandle() != null) {
			_activation.setFactHandleId(agendaItem.getFactHandle().getId());
		}

		org.drools.core.util.LinkedList<LogicalDependency> list = agendaItem
				.getLogicalDependencies();
		if (list != null && !list.isEmpty()) {
			for (LogicalDependency node = list.getFirst(); node != null; node = node
					.getNext()) {
				_activation.addLogicalDependency(((BeliefSet) node
						.getJustified()).getFactHandle().getId());
			}
		}
		return _activation;
	}

	private static void writeNodeMemories(MongoRuleData ruleData,
			InternalWorkingMemory wm, KieBase kbase) {
		NodeMemories memories = wm.getNodeMemories();
		// only some of the node memories require special serialization handling
		// so we iterate over all of them and process only those that require it
		for (int i = 0; i < memories.length(); i++) {
			Memory memory = memories.peekNodeMemory(i);
			// some nodes have no memory, so we need to check for nulls
			if (memory != null) {
				EmbeddedNodeMemory node = null;
				switch (memory.getNodeType()) {
				case NodeTypeEnums.AccumulateNode: {
					node = writeAccumulateNodeMemory(i, memory);
					break;
				}
				case NodeTypeEnums.RightInputAdaterNode: {
					InternalRuleBase ruleBase = (InternalRuleBase) ((InternalKnowledgeBase) kbase)
							.getRuleBase();
					node = writeRIANodeMemory(i, ruleBase, memories, memory);
					break;
				}
				case NodeTypeEnums.FromNode: {
					node = writeFromNodeMemory(i, memory);
					break;
				}
				case NodeTypeEnums.QueryElementNode: {
					node = writeQueryElementNodeMemory(i, memory, wm);
					break;
				}
				}
				if (node != null) {
					// not all node memories require serialization
					ruleData.getNodeMemories().add(node);
				}
			}
		}
	}

	public static void addLeftTuple(List<Integer> tupleHandleIds,
			LeftTuple leftTuple) {
		for (LeftTuple entry = leftTuple; entry != null; entry = entry
				.getParent()) {
			tupleHandleIds.add(entry.getLastHandle().getId());
		}
	}

	private static EmbeddedAccumulateNodeMemory writeAccumulateNodeMemory(
			final int nodeId, final Memory memory) {
		// for accumulate nodes, we need to store the ID of created (result)
		// handles
		AccumulateMemory accmem = (AccumulateMemory) memory;
		if (accmem.betaMemory.getLeftTupleMemory().size() > 0) {
			EmbeddedAccumulateNodeMemory _accumulate = new EmbeddedAccumulateNodeMemory(nodeId);
			@SuppressWarnings("rawtypes")
			final org.drools.core.util.Iterator tupleIter = accmem.betaMemory
					.getLeftTupleMemory().iterator();
			for (LeftTuple leftTuple = (LeftTuple) tupleIter.next(); leftTuple != null; leftTuple = (LeftTuple) tupleIter
					.next()) {
				AccumulateContext accctx = (AccumulateContext) leftTuple
						.getObject();
				InternalFactHandle handle = accctx.getResultFactHandle();
				if (handle != null) {
					EmbeddedAccumulateNodeMemory.Context context = _accumulate
							.addContext(new EmbeddedFactHandle(handle));
					addLeftTuple(context.getTupleFactHandleList(), leftTuple);
				}
			}

			return _accumulate;
		}
		return null;
	}

	private static EmbeddedRIANodeMemory writeRIANodeMemory(final int nodeId,
			final InternalRuleBase ruleBase, final NodeMemories memories,
			final Memory memory) {
		Map<Integer, BaseNode> nodeMap = getNodeMap(ruleBase);
		RightInputAdapterNode riaNode = (RightInputAdapterNode) nodeMap
				.get(nodeId);
		BetaNode betaNode = null;
		if (ruleBase.getConfiguration().isPhreakEnabled()) {
			ObjectSink[] sinks = riaNode.getSinkPropagator().getSinks();
			betaNode = (BetaNode) sinks[0];
		} else {
			betaNode = (BetaNode) riaNode.getNextLeftTupleSinkNode();
		}

		Memory betaMemory = memories.peekNodeMemory(betaNode.getId());
		BetaMemory bm;
		if (betaNode.getType() == NodeTypeEnums.AccumulateNode) {
			bm = ((AccumulateMemory) betaMemory).getBetaMemory();
		} else {
			bm = (BetaMemory) betaMemory;
		}

		// for RIA nodes, we need to store the ID of the created handles
		bm.getRightTupleMemory().iterator();
		if (bm.getRightTupleMemory().size() > 0) {
			EmbeddedRIANodeMemory ria = new EmbeddedRIANodeMemory(nodeId);
			@SuppressWarnings("rawtypes")
			final org.drools.core.util.Iterator it = bm.getRightTupleMemory()
					.iterator();

			// iterates over all propagated handles and assert them to the new
			// sink
			for (RightTuple entry = (RightTuple) it.next(); entry != null; entry = (RightTuple) it
					.next()) {
				LeftTuple leftTuple = (LeftTuple) entry.getFactHandle()
						.getObject();
				InternalFactHandle handle = (InternalFactHandle) leftTuple
						.getObject();
				if (handle != null) {
					EmbeddedRIANodeMemory.Context context = ria
							.addContext(new EmbeddedFactHandle(handle));
					addLeftTuple(context.getTupleFactHandleList(), leftTuple);
				}
			}

			return ria;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static EmbeddedFromNodeMemory writeFromNodeMemory(final int nodeId,
			final Memory memory) {
		FromMemory fromMemory = (FromMemory) memory;

		if (fromMemory.betaMemory.getLeftTupleMemory().size() > 0) {
			EmbeddedFromNodeMemory from = new EmbeddedFromNodeMemory(nodeId);

			@SuppressWarnings("rawtypes")
			final org.drools.core.util.Iterator tupleIter = fromMemory.betaMemory
					.getLeftTupleMemory().iterator();
			for (LeftTuple leftTuple = (LeftTuple) tupleIter.next(); leftTuple != null; leftTuple = (LeftTuple) tupleIter
					.next()) {
				Map<Object, RightTuple> matches = (Map<Object, RightTuple>) leftTuple
						.getObject();
				EmbeddedFromNodeMemory.Context context = from.addContext();
				addLeftTuple(context.getTupleFactHandleList(), leftTuple);
				for (RightTuple rightTuple : matches.values()) {
					context.getHandleList().add(
							new EmbeddedFactHandle(rightTuple.getFactHandle()));
				}
			}

			return from;
		}
		return null;
	}

	private static EmbeddedQueryElementNodeMemory writeQueryElementNodeMemory(
			final int nodeId, final Memory memory,
			final InternalWorkingMemory wm) {
		org.drools.core.util.Iterator<LeftTuple> it = LeftTupleIterator
				.iterator(wm, ((QueryElementNodeMemory) memory).getNode());

		EmbeddedQueryElementNodeMemory query = new EmbeddedQueryElementNodeMemory(
				nodeId);
		for (LeftTuple leftTuple = it.next(); leftTuple != null; leftTuple = it
				.next()) {
			InternalFactHandle handle = (InternalFactHandle) leftTuple
					.getObject();
			EmbeddedFactHandle efh = new EmbeddedFactHandle(handle);

			EmbeddedQueryElementNodeMemory.Context context = query
					.addContext(efh);
			addLeftTuple(context.getTupleFactHandleList(), leftTuple);

			LeftTuple childLeftTuple = leftTuple.getFirstChild();
			while (childLeftTuple != null) {
				RightTuple rightParent = childLeftTuple.getRightParent();
				EmbeddedFactHandle result = new EmbeddedFactHandle(
						rightParent.getFactHandle());
				context.addResult(result);

				while (childLeftTuple != null
						&& childLeftTuple.getRightParent() == rightParent) {
					// skip to the next child that has a different right parent
					childLeftTuple = childLeftTuple.getLeftParentNext();
				}
			}
		}

		return query.getContexts().isEmpty() ? null : query;
	}

	public static Map<Integer, BaseNode> getNodeMap(InternalRuleBase ruleBase) {
		Map<Integer, BaseNode> nodes = new HashMap<Integer, BaseNode>();
		buildNodeMap(ruleBase, nodes);
		return nodes;
	}

	private static void buildNodeMap(InternalRuleBase ruleBase,
			Map<Integer, BaseNode> nodes) {
		for (ObjectTypeNode sink : ruleBase.getRete().getObjectTypeNodes()) {
			nodes.put(sink.getId(), sink);
			addObjectSink(ruleBase, sink, nodes);
		}
	}

	private static void addObjectSink(InternalRuleBase ruleBase,
			ObjectSink sink, Map<Integer, BaseNode> nodes) {
		// we don't need to store alpha nodes, as they have no state to
		// serialise
		if (sink instanceof PropagationQueuingNode) {
			nodes.put(sink.getId(), ((BaseNode) sink));
		}
		if (sink instanceof LeftTupleSource) {
			LeftTupleSource node = (LeftTupleSource) sink;
			for (LeftTupleSink leftTupleSink : node.getSinkPropagator()
					.getSinks()) {
				addLeftTupleSink(ruleBase, leftTupleSink, nodes);
			}
		} else if (sink instanceof WindowNode) {
			WindowNode node = (WindowNode) sink;
			nodes.put(sink.getId(), ((BaseNode) sink));
			for (ObjectSink objectSink : node.getSinkPropagator().getSinks()) {
				addObjectSink(ruleBase, objectSink, nodes);
			}
		} else {
			ObjectSource node = (ObjectSource) sink;
			for (ObjectSink objectSink : node.getSinkPropagator().getSinks()) {
				addObjectSink(ruleBase, objectSink, nodes);
			}
		}
	}

	private static void addLeftTupleSink(InternalRuleBase ruleBase,
			LeftTupleSink sink, Map<Integer, BaseNode> nodes) {
		if (sink instanceof QueryRiaFixerNode) {
			nodes.put(sink.getId(), (LeftTupleSource) sink);
			addLeftTupleSink(ruleBase,
					((QueryRiaFixerNode) sink).getBetaNode(), nodes);
		} else if (sink instanceof LeftTupleSource) {
			nodes.put(sink.getId(), (LeftTupleSource) sink);
			for (LeftTupleSink leftTupleSink : ((LeftTupleSource) sink)
					.getSinkPropagator().getSinks()) {
				addLeftTupleSink(ruleBase, leftTupleSink, nodes);
			}
		} else if (sink instanceof ObjectSource) {
			// it may be a RIAN
			nodes.put(sink.getId(), (ObjectSource) sink);
			for (ObjectSink objectSink : ((ObjectSource) sink)
					.getSinkPropagator().getSinks()) {
				addObjectSink(ruleBase, objectSink, nodes);
			}
		} else if (sink instanceof RuleTerminalNode) {
			nodes.put(sink.getId(), (RuleTerminalNode) sink);
		} else if (sink instanceof QueryTerminalNode) {
			nodes.put(sink.getId(), (QueryTerminalNode) sink);
		}
	}

	private static void writeObjectTypeConfiguration(InternalWorkingMemory wm,
			ObjectTypeConfigurationRegistry otcr, EmbeddedEntryPoint eep) {
		for (ObjectTypeConf otc : otcr.values()) {
			final ObjectTypeNodeMemory memory = (ObjectTypeNodeMemory) wm
					.getNodeMemory(otc.getConcreteObjectTypeNode());
			if (memory != null && !memory.memory.isEmpty()) {
				EmbeddedEntryPoint.ObjectTypeConfiguration _otc = new EmbeddedEntryPoint.ObjectTypeConfiguration(
						otc);
				eep.addOtc(_otc);
			}
		}
	}

	private static void writeFactHandles(Environment env, EmbeddedEntryPoint eep, 
			ObjectStore objectStore) throws ClassNotFoundException {
		MongoSessionStore store = (MongoSessionStore) env
				.get(MongoSessionStore.envKey);
		// Write out FactHandles
		for (Iterator<?> it = objectStore.iterateFactHandles(); it.hasNext();) {
			InternalFactHandle handle = (InternalFactHandle) it.next();
			EmbeddedFactHandle eh = writeFactHandle(store, handle);
			eep.addHandle(eh);
		}
	}

	private static EmbeddedFactHandle writeFactHandle(MongoSessionStore store,
			InternalFactHandle handle) throws ClassNotFoundException {
		EmbeddedFactHandle efh = null;
		if (handle instanceof EventFactHandle) {
			efh = new EmbeddedEventFactHandle(handle);
		} else if (handle instanceof QueryElementFactHandle) {
			efh = new EmbeddedQueryFactHandle(handle);
		} else {
			efh = new EmbeddedFactHandle(handle);
		}

		Object object = handle.getObject();

		if (object != null) {
			SessionObjectPersistenceStrategy strategy = PersistenceStrategyHelper
					.getStrategy(store, object.getClass());
			MongoSerializable objectRef = strategy.serialize(object);
			efh.setPersistentStrategyClass(strategy.getClass().getName());
			efh.setObjectRef(objectRef);
		}

		return efh;
	}

	private static void writeTruthMaintenanceSystem(Environment env,
			EntryPoint wmep, EmbeddedEntryPoint eep)
			throws ClassNotFoundException {
		TruthMaintenanceSystem tms = ((NamedEntryPoint) wmep)
				.getTruthMaintenanceSystem();
		ObjectHashMap justifiedMap = tms.getEqualityKeyMap();

		if (!justifiedMap.isEmpty()) {
			EqualityKey[] keys = new EqualityKey[justifiedMap.size()];
			@SuppressWarnings("rawtypes")
			org.drools.core.util.Iterator it = justifiedMap.iterator();
			int i = 0;
			for (ObjectHashMap.ObjectEntry entry = (ObjectHashMap.ObjectEntry) it
					.next(); entry != null; entry = (ObjectHashMap.ObjectEntry) it
					.next()) {
				EqualityKey key = (EqualityKey) entry.getKey();
				keys[i++] = key;
			}

			EmbeddedTruthMaintenanceSystem etms = new EmbeddedTruthMaintenanceSystem();

			// write the assert map of Equality keys
			for (EqualityKey key : keys) {
				EmbeddedEqualityKey ekey = new EmbeddedEqualityKey(key);

				if (key.size() > 1) {
					// add all the other key's if they exist
					FastIterator keyIter = key.fastIterator();
					for (DefaultFactHandle handle = key.getFirst().getNext(); handle != null; handle = (DefaultFactHandle) keyIter
							.next(handle)) {
						ekey.addOtherHandle(handle.getId());
					}
				}

				if (key.getBeliefSet() != null) {
					MongoSessionStore store = (MongoSessionStore) env
							.get(MongoSessionStore.envKey);
					writeBeliefSet(store, key.getBeliefSet(), ekey);
				}

				etms.addKey(ekey);
			}

			eep.setTms(etms);
		}
	}

	@SuppressWarnings("rawtypes")
	private static void writeBeliefSet(MongoSessionStore store,
			BeliefSet beliefSet, EmbeddedEqualityKey ekey)
			throws ClassNotFoundException {

		EmbeddedBeliefSet ebs = new EmbeddedBeliefSet(beliefSet.getFactHandle()
				.getId());

		FastIterator it = beliefSet.iterator();
		for (LinkedListEntry node = (LinkedListEntry) beliefSet.getFirst(); node != null; node = (LinkedListEntry) it
				.next(node)) {
			LogicalDependency belief = (LogicalDependency) node.getObject();
			EmbeddedLogicalDependency eld = new EmbeddedLogicalDependency();

			LogicalDependency dependency = (LogicalDependency) node.getObject();
			org.drools.core.spi.Activation activation = dependency.getJustifier();
			EmbeddedActivation ea = new EmbeddedActivation();
			ea.setRulePackage(activation.getRule().getPackage());
			ea.setRuleName(activation.getRule().getName());
			addLeftTuple(ea.getTupleFactHandleList(), activation.getTuple());
			eld.setActivation(ea);

			if (belief.getObject() != null) {
				Object object = belief.getObject();
				SessionObjectPersistenceStrategy strategy = PersistenceStrategyHelper
						.getStrategy(store, object.getClass());
				MongoSerializable objectRef = strategy.serialize(object);
				eld.setObjectRef(objectRef);
			}

			if (belief.getValue() != null) {
				Object value = belief.getObject();
				SessionObjectPersistenceStrategy strategy = PersistenceStrategyHelper
						.getStrategy(store, value.getClass());
				MongoSerializable valueRef = strategy.serialize(value);
				eld.setValueRef(valueRef);
			}
			ebs.addLogicalDependency(eld);
		}
		ekey.setBeliefSet(ebs);
	}

	private static void writeActionQueue(AbstractWorkingMemory wm,
			MongoRuleData ruleData) throws IOException, ClassNotFoundException {
		if (!wm.getActionQueue().isEmpty()) {
			WorkingMemoryAction[] queue = wm.getActionQueue().toArray(
					new WorkingMemoryAction[wm.getActionQueue().size()]);
			for (int i = queue.length - 1; i >= 0; i--) {
				WorkingMemoryAction action = queue[i];
				ruleData.getActionQueue().add(
						MongoActionMarshaller.serialize(action));
			}
		}
	}

}
