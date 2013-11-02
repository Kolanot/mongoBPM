package org.jbpm.persistence.mongodb.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import org.jbpm.persistence.mongodb.rule.EmbeddedAgenda;
import org.jbpm.persistence.mongodb.rule.EmbeddedEntryPoint;
import org.jbpm.persistence.mongodb.rule.EmbeddedFactHandle;
import org.jbpm.persistence.mongodb.rule.EmbeddedFactHandleFactory;
import org.jbpm.persistence.mongodb.rule.EmbeddedPropagationContext;
import org.jbpm.persistence.mongodb.rule.action.EmbeddedWorkingMemoryAction;
import org.jbpm.persistence.mongodb.rule.memory.EmbeddedNodeMemory;

import org.drools.core.common.InternalFactHandle;
import org.drools.core.marshalling.impl.MarshallerReaderContext;

@Embedded
public class MongoRuleData {

    @Embedded 
    private EmbeddedFactHandleFactory factHandleFactory;

	private long propagationIdCounter;
    
    @Embedded 
    private EmbeddedAgenda agenda;
    
    @Embedded 
    private EmbeddedFactHandle initialFactHandle;
    
    @Embedded 
    private List<EmbeddedNodeMemory> nodeMemories = new ArrayList<EmbeddedNodeMemory>();
    
    @Embedded 
    private Map<String, EmbeddedEntryPoint> entrypoints = new HashMap<String, EmbeddedEntryPoint>();
    
    @Embedded 
    private List<EmbeddedPropagationContext> propagationContexts = new ArrayList<EmbeddedPropagationContext>(); 
    
    @Embedded 
    private List<EmbeddedWorkingMemoryAction> actionQueue = new ArrayList<EmbeddedWorkingMemoryAction>();
    
    @Transient
    private Map<Integer, InternalFactHandle> cachedHandles = new HashMap<Integer, InternalFactHandle>();
    
    @Transient
    private MarshallerReaderContext marshallerReaderContext;

    public MongoRuleData() {
    }

	public EmbeddedFactHandleFactory getFactHandleFactory() {
		return factHandleFactory;
	}

	public void setFactHandleFactory(EmbeddedFactHandleFactory factHandleFactory) {
		this.factHandleFactory = factHandleFactory;
	}

	public long getPropagationIdCounter() {
		return propagationIdCounter;
	}

	public void getPropagationIdCounter(long propagationIdCounter) {
		this.propagationIdCounter = propagationIdCounter;
	}

	public EmbeddedAgenda getAgenda() {
		return agenda;
	}

	public void setAgenda(EmbeddedAgenda agenda) {
		this.agenda = agenda;
	}
	
	public EmbeddedFactHandle getInitialFactHandle() {
		return initialFactHandle;
	}

	public void setInitialFacthandle(EmbeddedFactHandle initialFactHandle) {
		this.initialFactHandle = initialFactHandle;
	}

	public List<EmbeddedNodeMemory> getNodeMemories() {
		return nodeMemories;
	}

	public Map<String, EmbeddedEntryPoint> getEntrypoints() {
		return entrypoints;
	}

	public List<EmbeddedPropagationContext> getPropagationContexts() {
		return propagationContexts;
	}

	public List<EmbeddedWorkingMemoryAction> getActionQueue() {
		return actionQueue;
	}
	
	public void addCachedHandle(InternalFactHandle handle) {
		this.cachedHandles.put(handle.getId(), handle);
	}
	
	public InternalFactHandle getCachedHandle(int id) {
		return cachedHandles.get(id);
	}

	public MarshallerReaderContext getMarshallerReaderContext() {
		return marshallerReaderContext;
	}

	public void setMarshallerReaderContext(
			MarshallerReaderContext marshallerReaderContext) {
		this.marshallerReaderContext = marshallerReaderContext;
	}
}
 