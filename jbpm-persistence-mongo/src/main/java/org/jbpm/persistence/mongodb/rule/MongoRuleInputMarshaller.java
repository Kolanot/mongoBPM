package org.jbpm.persistence.mongodb.rule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.drools.core.common.AbstractWorkingMemory;
import org.drools.core.common.AgendaGroupQueueImpl;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.EqualityKey;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalRuleBase;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.InternalWorkingMemoryEntryPoint;
import org.drools.core.common.NamedEntryPoint;
import org.drools.core.common.ObjectStore;
import org.drools.core.common.PropagationContextFactory;
import org.drools.core.common.QueryElementFactHandle;
import org.drools.core.common.TruthMaintenanceSystem;
import org.drools.core.common.WorkingMemoryAction;
import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.ProtobufInputMarshaller.QueryElementContext;
import org.drools.core.marshalling.impl.ProtobufInputMarshaller.TupleKey;
import org.drools.core.marshalling.impl.ProtobufMessages;
import org.drools.core.marshalling.impl.ProtobufMessages.FactHandle;
import org.drools.core.marshalling.impl.ProtobufMessages.NodeMemory.QueryElementNodeMemory.QueryContext;
import org.drools.core.reteoo.ObjectTypeConf;
import org.drools.core.spi.Activation;
import org.drools.core.spi.FactHandleFactory;
import org.drools.core.spi.PropagationContext;
import org.jbpm.persistence.mongodb.MongoSessionStore;
import org.jbpm.persistence.mongodb.object.MongoSerializable;
import org.jbpm.persistence.mongodb.object.PersistenceStrategyHelper;
import org.jbpm.persistence.mongodb.object.SessionObjectPersistenceStrategy;
import org.jbpm.persistence.mongodb.rule.EmbeddedAgenda.EmbeddedAgendaGroup;
import org.jbpm.persistence.mongodb.rule.action.EmbeddedWorkingMemoryAction;
import org.jbpm.persistence.mongodb.rule.action.MongoActionMarshaller;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedAccumulateNodeMemory;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedFromNodeMemory;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedNodeMemory;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedQueryElementNodeMemory;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedRIANodeMemory;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedBeliefSet;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedEqualityKey;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedLogicalDependency;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedTruthMaintenanceSystem;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.rule.EntryPoint;

public class MongoRuleInputMarshaller {
	public static void deserialize(MongoRuleData ruleData,	AbstractWorkingMemory wm) 
			throws IOException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		InternalRuleBase ruleBase = (InternalRuleBase) wm.getRuleBase();
		readNodeMemories(wm, ruleData);

		List<PropagationContext> pctxs = new ArrayList<PropagationContext>();

		if (ruleBase.getConfiguration().isPhreakEnabled()
				|| ruleData.getInitialFactHandle() != null) {
			wm.initInitialFact(ruleBase, ruleData.getMarshallerReaderContext());
			ruleData.addCachedHandle(wm.getInitialFactHandle());
		}

		for (EmbeddedEntryPoint eep : ruleData.getEntrypoints().values()) {
			readFactHandles(wm, eep, pctxs, ruleData);
			readTruthMaintenanceSystem(wm, ruleData, eep, pctxs);
		}

		cleanReaderContexts(pctxs);

		readActionQueue(wm, ruleData);

	}

	public static FactHandleFactory readFactHandleFactory(
			MongoRuleData ruleData, InternalRuleBase ruleBase)
			throws IOException {
		FactHandleFactory handleFactory = ruleBase.newFactHandleFactory(
				ruleData.getFactHandleFactory().getId(), ruleData
						.getFactHandleFactory().getRecency());
		return handleFactory;
	}

	public static InternalAgenda readAgenda(MongoRuleData ruleData,
			InternalRuleBase ruleBase) {
		InternalAgenda agenda = ruleBase.getConfiguration()
				.getComponentFactory().getAgendaFactory()
				.createAgenda(ruleBase, false);
		EmbeddedAgenda ea = ruleData.getAgenda();

		for (EmbeddedAgendaGroup ag : ea.getAgendaGroups()) {
			AgendaGroupQueueImpl group = (AgendaGroupQueueImpl) agenda
					.getAgendaGroup(ag.getName(), ruleBase);
			group.setActive(ag.isActive());
			group.setAutoDeactivate(ag.isAutoDeactivate());
			group.setClearedForRecency(ag.getClearedForRecency());
			group.hasRuleFlowListener(ag.hasRuleFlowListener());
			group.setActivatedForRecency(ag.getActivatedForRecency());

			for (EmbeddedAgendaGroup.NodeInstance nodeInstance : ag.getNodeInstances()) {
				group.addNodeInstance(nodeInstance.getProcessInstanceId(),
						nodeInstance.getNodeInstanceId());
			}
			agenda.getAgendaGroupsMap().put(group.getName(), group);
		}

		for (String groupName : ea.getGroupStack()) {
			agenda.addAgendaGroupOnStack(agenda.getAgendaGroup(groupName));
		}

		MongoActivationsFilter filter = new MongoActivationsFilter();
		readActivations(ea, agenda, filter);
		agenda.setActivationsFilter(filter);

		return agenda;
	}

	private static void readActivations(EmbeddedAgenda ea,
			InternalAgenda agenda, MongoActivationsFilter filter) {

		for (EmbeddedActivation activation : ea.getMatches()) {
			// this is a dormant activation
			filter.getDormantActivationsMap().put(
					new MongoActivationKey(activation), activation);
		}
		for (EmbeddedActivation activation : ea.getRuleActivations()) {
			// this is an active rule network evaluator
			filter.getRneActivations().put(new MongoActivationKey(activation),
					activation);
		}
	}

	private static MongoSessionStore getSessionStore(InternalWorkingMemory wm) {
		Environment env = wm.getEnvironment();
		MongoSessionStore store = (MongoSessionStore) env
				.get(MongoSessionStore.envKey);
		return store;
	}

	private static void readFactHandles(InternalWorkingMemory wm,
			EmbeddedEntryPoint eep, List<PropagationContext> pctxs,
			MongoRuleData ruleData) throws IOException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		MongoSessionStore store = getSessionStore(wm);

		EntryPoint entryPoint = wm.getEntryPoints().get(eep.getEntryPointId());
		ObjectStore objectStore = ((NamedEntryPoint) entryPoint)
				.getObjectStore();

		// load the handles
		for (EmbeddedFactHandle efh : eep.getObjectStore()) {
			InternalFactHandle handle = readFactHandle(store, entryPoint, efh);

			ruleData.addCachedHandle(handle);

			if (!efh.isJustified()) {
				// BeliefSystem handles the Object type
				if (handle.getObject() != null) {
					objectStore.addHandle(handle, handle.getObject());
				}

				// add handle to object type node
				assertHandleIntoOTN(wm, handle, pctxs, ruleData.getMarshallerReaderContext());
			}
		}
	}

	public static InternalFactHandle readFactHandle(MongoSessionStore store,
			EntryPoint entryPoint, EmbeddedFactHandle efh) throws IOException,
			ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		MongoSerializable objectRef = efh.getObjectRef();
		SessionObjectPersistenceStrategy strategy = PersistenceStrategyHelper
				.getStrategy(store, objectRef.getSerializationStrategyClass());
		Object object = strategy.deserialize(objectRef);

		InternalFactHandle handle = null;
		if (efh instanceof EmbeddedEventFactHandle) {
			EmbeddedEventFactHandle eefh = (EmbeddedEventFactHandle) efh;
			handle = new EventFactHandle(eefh.getId(), object,
					eefh.getRecency(), eefh.getStartTimestamp(),
					eefh.getDuration(), entryPoint);
			((EventFactHandle) handle).setExpired(eefh.isExpired());
		} else if (efh instanceof EmbeddedQueryFactHandle) {
			handle = new QueryElementFactHandle(object, efh.getId(),
					efh.getRecency());
		} else {
			handle = new DefaultFactHandle(efh.getId(), object,
					efh.getRecency(), entryPoint);
		}
		return handle;
	}

	private static void readTruthMaintenanceSystem(AbstractWorkingMemory wm,
			MongoRuleData ruleData, EmbeddedEntryPoint eep,
			List<PropagationContext> pctxs)
					throws IOException,
			ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		EntryPoint wmep = wm.getEntryPoints().get(eep.getEntryPointId());
		TruthMaintenanceSystem tms = ((NamedEntryPoint) wmep)
				.getTruthMaintenanceSystem();

		boolean wasOTCSerialized = !eep.getOtcList().isEmpty(); 
		// if isEmpty, then the OTC was not serialized (older
		// versions of drools)
		Set<String> tmsEnabled = new HashSet<String>();
		for (EmbeddedEntryPoint.ObjectTypeConfiguration otc : eep.getOtcList()) {
			if (otc.isTMSEnabled()) {
				tmsEnabled.add(otc.getTypeName());
			}
		}

		EmbeddedTruthMaintenanceSystem etms = eep.getTms();
		
		if (etms == null) return;
		
		for (EmbeddedEqualityKey eek : etms.getKeys()) {
			InternalFactHandle handle = (InternalFactHandle) ruleData
					.getCachedHandle(eek.getHandleId());

			// ObjectTypeConf state is not marshalled, so it needs to be
			// re-determined
			ObjectTypeConf typeConf = wm.getObjectTypeConfigurationRegistry()
					.getObjectTypeConf(((NamedEntryPoint) handle.getEntryPoint()).getEntryPoint(),
							handle.getObject());
			if (!typeConf.isTMSEnabled()
					&& (!wasOTCSerialized || tmsEnabled.contains(typeConf.getTypeName()))) {
				typeConf.enableTMS();
			}

			EqualityKey key = new EqualityKey(handle, eek.getStatus());
			handle.setEqualityKey(key);

			if (key.getStatus() == EqualityKey.JUSTIFIED) {
				// not yet added to the object stores
				((NamedEntryPoint) handle.getEntryPoint()).getObjectStore()
						.addHandle(handle, handle.getObject());
				// add handle to object type node
				assertHandleIntoOTN(wm, handle, pctxs, ruleData.getMarshallerReaderContext());
			}

			for (Integer factHandleId : eek.getOtherHandles()) {
				handle = (InternalFactHandle) ruleData
						.getCachedHandle(factHandleId);
				key.addFactHandle(handle);
				handle.setEqualityKey(key);
			}
			tms.put(key);

			readBeliefSet(ruleData, wm, tms, key, eek);
		}

	}

	private static void readBeliefSet(MongoRuleData ruleData,
			AbstractWorkingMemory wm, TruthMaintenanceSystem tms,
			EqualityKey key, EmbeddedEqualityKey eek) throws IOException,
			ClassNotFoundException, IllegalArgumentException, InstantiationException, 
			IllegalAccessException, InvocationTargetException {
		if (eek.hasBeliefSet()) {
			EmbeddedBeliefSet ebs = eek.getBeliefSet();
			InternalFactHandle handle = (InternalFactHandle) ruleData
					.getCachedHandle(eek.getHandleId());
			// phreak might serialize empty belief sets, so he have to handle it
			// during deserialization
			if (ebs.hasLogicalDependency()) {
				for (EmbeddedLogicalDependency eld : ebs.getLogicalDependencies()) {
					EmbeddedActivation ea = eld.getActivation();
					InternalAgenda agenda = (InternalAgenda) wm.getAgenda();
					MongoActivationsFilter filter = (MongoActivationsFilter) agenda.getActivationsFilter();
					MongoActivationKey ak = new MongoActivationKey(ea);

					Activation activation = (Activation) filter
							.getTuplesCache().get(ak).getObject();

					MongoSessionStore store = getSessionStore(wm);
					SessionObjectPersistenceStrategy objectStrategy = PersistenceStrategyHelper
							.getStrategy(store, eld.getObjectRef()
									.getSerializationStrategyClass());
					Object object = objectStrategy.deserialize(eld.getObjectRef());

					SessionObjectPersistenceStrategy valueStrategy = PersistenceStrategyHelper
							.getStrategy(store, eld.getValueRef()
									.getSerializationStrategyClass());
					Object value = valueStrategy.deserialize(eld.getValueRef());

					ObjectTypeConf typeConf = wm
							.getObjectTypeConfigurationRegistry()
							.getObjectTypeConf(
									((NamedEntryPoint) handle.getEntryPoint())
											.getEntryPoint(),
									handle.getObject());
					tms.readLogicalDependency(handle, object, value,
							activation, activation.getPropagationContext(),
							activation.getRule(), typeConf);
				}
			} else {
				handle.getEqualityKey().setBeliefSet(
						tms.getBeliefSystem().newBeliefSet(handle));
			}
		}
	}

	private static void cleanReaderContexts(List<PropagationContext> pctxs) {
		for (PropagationContext ctx : pctxs) {
			ctx.cleanReaderContext();
		}
	}

	private static void readActionQueue(AbstractWorkingMemory wm,
			MongoRuleData ruleData) throws IOException, ClassNotFoundException {
		Queue<WorkingMemoryAction> actionQueue = wm.getActionQueue();
		for (EmbeddedWorkingMemoryAction ea : ruleData.getActionQueue()) {
			actionQueue.offer(MongoActionMarshaller.deserialize(ea, wm, ruleData));
		}
	}

	private static void assertHandleIntoOTN(InternalWorkingMemory wm,
			InternalFactHandle handle, List<PropagationContext> pctxs,
			MarshallerReaderContext marshallerReaderContext) {
		Object object = handle.getObject();
		InternalWorkingMemoryEntryPoint ep = (InternalWorkingMemoryEntryPoint) handle
				.getEntryPoint();
		ObjectTypeConf typeConf = ep.getObjectTypeConfigurationRegistry()
				.getObjectTypeConf(ep.getEntryPoint(), object);

		PropagationContextFactory pctxFactory = ((InternalRuleBase) wm
				.getRuleBase()).getConfiguration().getComponentFactory()
				.getPropagationContextFactory();

		PropagationContext propagationContext = pctxFactory
				.createPropagationContext(wm.getNextPropagationIdCounter(),
						PropagationContext.INSERTION, null, null, handle,
						ep.getEntryPoint(), marshallerReaderContext);
		// keeping this list for a later cleanup is necessary because of the
		// lazy propagations that might occur
		pctxs.add(propagationContext);

		// need to think about the delay of scheduling of object expire
		ep.getEntryPointNode().assertObject(handle, propagationContext,
				typeConf, wm);

		propagationContext.evaluateActionQueue(wm);
		wm.executeQueuedActions();
	}

	/*
	 * have to load nodeMemeories into MarshallerReaderContext
	 * this context could be used in fact propagation later
	 */
	private static void readNodeMemories(AbstractWorkingMemory wm, MongoRuleData ruleData) 
			throws IOException {
		String nullString = "null";
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bs);
		os.writeChars(nullString);
		byte[] buf = bs.toByteArray();
		bs.close();
		os.close();
		Environment env = wm.getEnvironment();
        MarshallerReaderContext context = new MarshallerReaderContext( new ByteArrayInputStream(buf),
                                                                               null,
                                                                               null,
                                                                               null,
                                                                               null,
                                                                               env);
                                                                               
		for (EmbeddedNodeMemory enm : ruleData.getNodeMemories()) {
			Object memory = null;
			if (enm instanceof EmbeddedAccumulateNodeMemory) {
				EmbeddedAccumulateNodeMemory eanm = (EmbeddedAccumulateNodeMemory)enm;
				Map<TupleKey, FactHandle> map = new HashMap<TupleKey, FactHandle>();
				for (EmbeddedAccumulateNodeMemory.Context ctx : eanm.getContexts()) {
					TupleKey key = new TupleKey(ctx.getTupleFactHandleArray());
					ProtobufMessages.FactHandle _ifh = FactHandle.newBuilder()
		                    .setId( ctx.getResultFactHandle().getId() )
		                    .setRecency( ctx.getResultFactHandle().getRecency() )
		                    .build();
					map.put(key, _ifh);
				}
				memory = map;
			} else if (enm instanceof EmbeddedRIANodeMemory) {
				EmbeddedRIANodeMemory ernm = (EmbeddedRIANodeMemory)enm;
				Map<TupleKey, FactHandle> map = new HashMap<TupleKey, FactHandle>();
				for (EmbeddedRIANodeMemory.Context ctx : ernm.getContexts()){
					TupleKey key = new TupleKey(ctx.getTupleFactHandleArray());
					ProtobufMessages.FactHandle _ifh = FactHandle.newBuilder()
		                    .setId( ctx.getResultFactHandle().getId() )
		                    .setRecency( ctx.getResultFactHandle().getRecency() )
		                    .build();
					map.put(key, _ifh);
				}
				memory = map;
			} else if (enm instanceof EmbeddedFromNodeMemory) {
				EmbeddedFromNodeMemory efnm = (EmbeddedFromNodeMemory)enm;
				Map<TupleKey, List<FactHandle>> map = new HashMap<TupleKey, List<FactHandle>>();
				for (EmbeddedFromNodeMemory.Context ctx : efnm.getContexts()) {
					TupleKey key = new TupleKey(ctx.getTupleFactHandleArray());
					// have to instantiate a modifiable list
					List<FactHandle> factHandles = new ArrayList<FactHandle>();
					for (EmbeddedFactHandle handle:ctx.getHandleList()) {
						FactHandle _ifh = FactHandle.newBuilder()
		                    .setId( handle.getId() )
		                    .setRecency( handle.getRecency() )
		                    .build();
						factHandles.add(_ifh);
					}
					map.put(key,factHandles);
				}
				memory = map;
			} else if (enm instanceof EmbeddedQueryElementNodeMemory) {
				EmbeddedQueryElementNodeMemory eqenm = (EmbeddedQueryElementNodeMemory)enm;
				Map<TupleKey, QueryElementContext> map = new HashMap<TupleKey, QueryElementContext>();
				for (EmbeddedQueryElementNodeMemory.Context ctx : eqenm.getContexts()) {
					int[] handles = ctx.getTupleFactHandleArray();
					TupleKey key = new TupleKey(handles);
			        ProtobufMessages.Tuple.Builder _tuple = ProtobufMessages.Tuple.newBuilder();
			        for( int i = 0; i < handles.length ; i++ ) {
			            _tuple.addHandleId( handles[i] );
			        }
					FactHandle _ifh = FactHandle.newBuilder()
		                    .setId(ctx.getFactHandle().getId() )
		                    .setRecency( ctx.getFactHandle().getRecency() )
		                    .build();
		            QueryContext.Builder _context = QueryContext.newBuilder()
		                    .setTuple( _tuple.build() )
		                    .setHandle( _ifh);

		            for (EmbeddedFactHandle result: ctx.getResultFactHandleList()) {
		                _context.addResult( ProtobufMessages.FactHandle.newBuilder()
		                        .setId( result.getId() )
		                        .setRecency( result.getRecency() )
		                        .build() );
		            }
		            QueryContext _ctx = _context.build(); 
					// we have to use a "cloned" query element context as we
					// need to write on it during deserialization process and
					// the
					// protobuf one is read-only
					map.put(key, new QueryElementContext(_ctx));
				}
				memory = map;
			} else  {
				throw new IllegalArgumentException("Unknown node type " + enm.getClass().getName() + " while deserializing session.");
			}
			context.nodeMemories.put(enm.getNodeId(), memory);
		}
		ruleData.setMarshallerReaderContext(context);
	}

}
