package org.jbpm.persistence.mongodb.instance;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.DefaultJobHandle;
import org.drools.core.time.impl.TimerJobInstance;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo.EmbeddedNodeInstance;
import org.jbpm.persistence.mongodb.object.MongoJavaSerializable;
import org.jbpm.persistence.mongodb.timer.EmbeddedProcessTimer;
import org.jbpm.persistence.mongodb.timer.MongoTimerMarshaller;
import org.jbpm.process.core.Context;
import org.jbpm.process.core.context.exclusive.ExclusiveGroup;
import org.jbpm.process.core.context.swimlane.SwimlaneContext;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstance;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.context.exclusive.ExclusiveGroupInstance;
import org.jbpm.process.instance.context.swimlane.SwimlaneContextInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.DynamicNodeInstance;
import org.jbpm.workflow.instance.node.EventNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.jbpm.workflow.instance.node.JoinInstance;
import org.jbpm.workflow.instance.node.MilestoneNodeInstance;
import org.jbpm.workflow.instance.node.RuleSetNodeInstance;
import org.jbpm.workflow.instance.node.StateBasedNodeInstance;
import org.jbpm.workflow.instance.node.StateNodeInstance;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;
import org.jbpm.workflow.instance.node.TimerNodeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoProcessInstanceMarshaller {
    static Logger logger = LoggerFactory.getLogger( MongoProcessInstanceMarshaller.class );	

 	public static void serialize(MongoProcessInstanceInfo procInstInfo) 
 			throws NotSerializableException {        
        WorkflowProcessInstanceImpl workFlow = (WorkflowProcessInstanceImpl) procInstInfo.getProcessInstance();
        procInstInfo.setProcessInstanceId(workFlow.getId());
        procInstInfo.setProcessId(workFlow.getProcessId());
        procInstInfo.setParentProcessInstanceId(workFlow.getParentProcessInstanceId());
        procInstInfo.setNodeInstanceCounter(workFlow.getNodeInstanceCounter());
        procInstInfo.setState(workFlow.getState());
        
        SwimlaneContextInstance swimlaneContextInstance = (SwimlaneContextInstance) workFlow.getContextInstance(SwimlaneContext.SWIMLANE_SCOPE);
        if (swimlaneContextInstance != null) {
        	procInstInfo.getSwimlaneActors().clear();
            procInstInfo.getSwimlaneActors().putAll(swimlaneContextInstance.getSwimlaneActors());
        }
        procInstInfo.getNodeInstances().clear();
        List<NodeInstance> nodeInsts = new ArrayList<NodeInstance>(workFlow.getNodeInstances());
        for (NodeInstance nodeInstance : nodeInsts) {
        	procInstInfo.getNodeInstances().add(serializeNodeInstance(nodeInstance));
        }
        
        List<ContextInstance> exclusiveGroupInstances =
        	workFlow.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
        	if (exclusiveGroupInstances != null) {
        	serializeGroupIds(exclusiveGroupInstances, procInstInfo.getExclusiveGroupInstanceIds());
        }
        // Process Variables
        VariableScopeInstance variableScopeInstance = (VariableScopeInstance) workFlow.getContextInstance(VariableScope.VARIABLE_SCOPE);
        serializeVariables(variableScopeInstance, procInstInfo.getVariables());
        
        // Process Events
        for (String event:workFlow.getEventTypes()) {
        	procInstInfo.getEventTypes().add(event);
        }
        
        // completed nodes
        procInstInfo.getCompletedNodeIds().addAll(workFlow.getCompletedNodeIds());
        
        // IterationLevels
        procInstInfo.getIterationLevels().clear();
        procInstInfo.getIterationLevels().putAll(workFlow.getIterationLevels());
    }

	private static void serializeVariables(VariableScopeInstance variableScopeInstance, Map<String, MongoJavaSerializable> variables) 
			throws NotSerializableException {
		variables.clear();
		Map<String, Object> procVariables = variableScopeInstance.getVariables();
        
        for (Iterator<Map.Entry<String,Object>> itr = procVariables.entrySet().iterator(); itr.hasNext();) {
        	Map.Entry<String, Object> entry = itr.next();
        	String key = entry.getKey();
        	Object object = entry.getValue();
            if(object != null){
            	MongoJavaSerializable var = null;
            	if (object instanceof Serializable) {
            		var = new MongoJavaSerializable((Serializable)object);
            		variables.put(key, var);
            	} else {
            		throw new NotSerializableException("ProcessVariable, class:" +  object.getClass() + " value: " + object);
            	}
            }	
        }
	}

	private static void serializeGroupIds(List<ContextInstance> exclusiveGroupInstances, Set<Long> exclusiveGroupInstanceIds) {
		exclusiveGroupInstanceIds.clear();
		for (ContextInstance contextInstance: exclusiveGroupInstances) {
			ExclusiveGroupInstance exclusiveGroupInstance = (ExclusiveGroupInstance) contextInstance;
			Collection<NodeInstance> groupNodeInstances = exclusiveGroupInstance.getNodeInstances();
			for (NodeInstance nodeInstance: groupNodeInstances) {
				exclusiveGroupInstanceIds.add(nodeInstance.getId());
			}
		}
	}

    private static EmbeddedNodeInstance serializeNodeInstance(NodeInstance nodeInstance) throws NotSerializableException {
    	EmbeddedNodeInstance eni = new EmbeddedNodeInstance();
        eni.setId(nodeInstance.getId());
        eni.setNodeId(nodeInstance.getNodeId());
        eni.setLevel(((org.jbpm.workflow.instance.NodeInstance)nodeInstance).getLevel());
        eni.setNodeClassName(nodeInstance.getClass().getName());
        serializeNodeInstanceContent(eni, nodeInstance);
        return eni;
    }

    private static void serializeNodeInstanceContent(EmbeddedNodeInstance eni, NodeInstance nodeInstance) throws NotSerializableException {
    	InternalKnowledgeRuntime kruntime = ((WorkflowProcessInstanceImpl)nodeInstance.getProcessInstance()).getKnowledgeRuntime();
        TimerManager timerManager = ((InternalProcessRuntime)kruntime.getProcessRuntime()).getTimerManager();
        if (nodeInstance instanceof StateBasedNodeInstance) {
    		for (long timerId : ((StateBasedNodeInstance) nodeInstance).getTimerInstances()) {
    			TimerInstance ti = timerManager.getTimerMap().get(timerId);
    			Trigger trigger = null;
    			if (ti.getJobHandle() instanceof DefaultJobHandle) {
    				TimerJobInstance timerJobInstance = ((DefaultJobHandle)ti.getJobHandle()).getTimerJobInstance();
    				trigger = timerJobInstance == null? null:timerJobInstance.getTrigger();
    			}
    			if (trigger != null) eni.getTimers().add(MongoTimerMarshaller.serialize(ti, trigger));
    		}
    	}
    	
        if (nodeInstance instanceof HumanTaskNodeInstance) {
            eni.setWorkItemId(((HumanTaskNodeInstance) nodeInstance).getWorkItemId());
        } else if (nodeInstance instanceof WorkItemNodeInstance) {
            eni.setWorkItemId(((WorkItemNodeInstance) nodeInstance).getWorkItemId());
        } else if (nodeInstance instanceof SubProcessNodeInstance) {
            eni.setSubProcessInstanceId(((SubProcessNodeInstance) nodeInstance).getProcessInstanceId());
        } else if (nodeInstance instanceof JoinInstance) {
            eni.setTriggers(((JoinInstance) nodeInstance).getTriggers());
        } else if (nodeInstance instanceof CompositeContextNodeInstance) {
        	if (nodeInstance instanceof DynamicNodeInstance) {
        	} else {
        	}
            CompositeContextNodeInstance compositeContextNodeInstance = (CompositeContextNodeInstance) nodeInstance;
            VariableScopeInstance variableScopeInstance = (VariableScopeInstance) compositeContextNodeInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);
            serializeVariables(variableScopeInstance, eni.getVariables());
            List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>(compositeContextNodeInstance.getNodeInstances());
            for (NodeInstance subNodeInstance : nodeInstances) {
            	eni.getNodeInstances().add(serializeNodeInstance(subNodeInstance));
            }
            List<ContextInstance> exclusiveGroupInstances =
            	compositeContextNodeInstance.getContextInstances(ExclusiveGroup.EXCLUSIVE_GROUP);
            if (exclusiveGroupInstances != null) {
            	serializeGroupIds(exclusiveGroupInstances, eni.getExclusiveGroupInstanceIds());
            }
        } else if (nodeInstance instanceof ForEachNodeInstance) {
            ForEachNodeInstance forEachNodeInstance = (ForEachNodeInstance) nodeInstance;
            List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>(forEachNodeInstance.getNodeInstances());
            for (NodeInstance subNodeInstance : nodeInstances) {
                if (subNodeInstance instanceof CompositeContextNodeInstance) {
                	eni.getNodeInstances().add(serializeNodeInstance(subNodeInstance));
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown node instance type: " + nodeInstance);
        }
    }

    public static void deserialize(MongoProcessInstanceInfo mpi, InternalKnowledgeRuntime kruntime) throws ClassNotFoundException {
        WorkflowProcessInstanceImpl processInstance = new RuleFlowProcessInstance();
        
        processInstance.setId(mpi.getProcessInstanceId());
        processInstance.setProcessId(mpi.getProcessId());
        processInstance.setState(mpi.getState());
        processInstance.setParentProcessInstanceId(mpi.getParentProcessInstanceId());
        
        Process process = kruntime.getKieBase().getProcess( processInstance.getProcessId() );
        processInstance.setProcess(process);
        processInstance.setKnowledgeRuntime(kruntime);
        
        mpi.setProcessInstance(processInstance);
        
        for( String completedNodeId : mpi.getCompletedNodeIds() ) { 
            processInstance.addCompletedNodeId(completedNodeId);
        }
        
        if (mpi.getSwimlaneActors() != null) {
            Context swimlaneContext = ((org.jbpm.process.core.Process) process).getDefaultContext(SwimlaneContext.SWIMLANE_SCOPE);
            SwimlaneContextInstance swimlaneContextInstance = (SwimlaneContextInstance) processInstance.getContextInstance(swimlaneContext);
            for (Map.Entry<String, String> entry: mpi.getSwimlaneActors().entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                swimlaneContextInstance.setActorId(name, value);
            }
        }

        for (EmbeddedNodeInstance ein:mpi.getNodeInstances()) {
            readNodeInstance(ein, processInstance, processInstance);
        }

    	for (long groupId:mpi.getExclusiveGroupInstanceIds()) {
            populateExclusiveGroup(processInstance, groupId);
    	}

        // Process Variables
		if (mpi.getVariables() != null && !mpi.getVariables().isEmpty()) {
			Context variableScope = ((org.jbpm.process.core.Process) process)
					.getDefaultContext(VariableScope.VARIABLE_SCOPE);
			VariableScopeInstance variableScopeInstance = (VariableScopeInstance) processInstance
					.getContextInstance(variableScope);
			for (Map.Entry<String, MongoJavaSerializable> entry:mpi.getVariables().entrySet()){
				String name = entry.getKey();
				MongoJavaSerializable value = entry.getValue();
				variableScopeInstance.internalSetVariable(name, value.getSerializedObject());
			}
		}
		processInstance.getIterationLevels().clear();
		processInstance.getIterationLevels().putAll(mpi.getIterationLevels());
        processInstance.internalSetNodeInstanceCounter( mpi.getNodeInstanceCounter());
        if (!mpi.isReconnected()) {
        	((RuleFlowProcessInstance)processInstance).reconnect();
        	mpi.reconnect();
        }
    }

	private static void populateExclusiveGroup(
			WorkflowProcessInstanceImpl processInstance, long groupId) {
		ExclusiveGroupInstance exclusiveGroupInstance = new ExclusiveGroupInstance();
		processInstance.addContextInstance(ExclusiveGroup.EXCLUSIVE_GROUP, exclusiveGroupInstance);
		NodeInstance nodeInstance = processInstance.getNodeInstance(groupId);
		if (nodeInstance == null) {
			throw new IllegalArgumentException("Could not find node instance when deserializing exclusive group instance: " + groupId);
		}
		exclusiveGroupInstance.addNodeInstance(nodeInstance);
	}

    private static NodeInstance readNodeInstance(EmbeddedNodeInstance ein, NodeInstanceContainer nodeInstanceContainer,
            WorkflowProcessInstanceImpl processInstance) throws ClassNotFoundException {
        long id = ein.getId();
        long nodeId = ein.getNodeId();
        int level = ein.getLevel();
        String className = ein.getNodeClassName();
        @SuppressWarnings("unchecked")
		Class<? extends NodeInstance> varClass = (Class<? extends NodeInstance>) Class.forName(className);
        NodeInstanceImpl nodeInstance = readNodeInstanceContent(varClass, ein, processInstance);

        nodeInstance.setNodeId(nodeId);
        nodeInstance.setLevel(level);
        nodeInstance.setNodeInstanceContainer(nodeInstanceContainer);
        nodeInstance.setProcessInstance((org.jbpm.workflow.instance.WorkflowProcessInstance) processInstance);
        nodeInstance.setId(id);

        if (varClass.equals(CompositeContextNodeInstance.class) || varClass.equals(DynamicNodeInstance.class)) {
            if (ein.getVariables() != null && !ein.getVariables().isEmpty()) {
                Context variableScope = ((org.jbpm.process.core.Process) ((org.jbpm.process.instance.ProcessInstance)
                		processInstance).getProcess()).getDefaultContext(VariableScope.VARIABLE_SCOPE);
                VariableScopeInstance variableScopeInstance = (VariableScopeInstance) ((CompositeContextNodeInstance) nodeInstance).getContextInstance(variableScope);
                for (Map.Entry<String, MongoJavaSerializable> var: ein.getVariables().entrySet()) {
                    variableScopeInstance.internalSetVariable(var.getKey(),var.getValue().getSerializedObject());
                }
                for (EmbeddedNodeInstance subEin:ein.getNodeInstances()){
                    readNodeInstance(subEin,
                            (CompositeContextNodeInstance) nodeInstance,
                            processInstance);
                }
                
            	for (long groupId:ein.getExclusiveGroupInstanceIds()) {
                    populateExclusiveGroup(processInstance, groupId);
            	}
            } else if (ein.getClass().equals(ForEachNodeInstance.class)) {
                for (EmbeddedNodeInstance subEin:ein.getNodeInstances()){
                    readNodeInstance(subEin,
                            (CompositeContextNodeInstance) nodeInstance,
                            processInstance);
                }
            } else {
            // do nothing
            }
        }

        return nodeInstance;
    }

    private static NodeInstanceImpl readNodeInstanceContent(Class<?> varClass, EmbeddedNodeInstance ein,
             WorkflowProcessInstance processInstance){
        NodeInstanceImpl nodeInstance = null;
        if (varClass.equals(RuleSetNodeInstance.class)) {
            nodeInstance = new RuleSetNodeInstance();
        } else if (varClass.equals(HumanTaskNodeInstance.class)) {
            nodeInstance = new HumanTaskNodeInstance();
            ((HumanTaskNodeInstance) nodeInstance).internalSetWorkItemId(ein.getWorkItemId());
        } else if (varClass.equals(WorkItemNodeInstance.class)) {
            nodeInstance = new WorkItemNodeInstance();
            ((WorkItemNodeInstance) nodeInstance).internalSetWorkItemId(ein.getWorkItemId());
        } else if (varClass.equals(SubProcessNodeInstance.class)) {
            nodeInstance = new SubProcessNodeInstance();
            ((SubProcessNodeInstance) nodeInstance).internalSetProcessInstanceId(ein.getSubProcessInstanceId());
        } else if (varClass.equals(MilestoneNodeInstance.class)) {
            nodeInstance = new MilestoneNodeInstance();
        } else if (varClass.equals(TimerNodeInstance.class)) {
            nodeInstance = new TimerNodeInstance();
        } else if (varClass.equals(EventNodeInstance.class)) {
            nodeInstance = new EventNodeInstance();
        } else if (varClass.equals(JoinInstance.class)) {
            nodeInstance = new JoinInstance();
            ((JoinInstance) nodeInstance).internalSetTriggers(ein.getTriggers());
        } else if (varClass.equals(CompositeContextNodeInstance.class)) {
            nodeInstance = new CompositeContextNodeInstance();
        } else if (varClass.equals(ForEachNodeInstance.class)) {
            nodeInstance = new ForEachNodeInstance();
        } else if (varClass.equals(DynamicNodeInstance.class)) {
            nodeInstance = new DynamicNodeInstance();
        } else if (varClass.equals(StateNodeInstance.class)) {
            nodeInstance = new StateNodeInstance();
        } else {
            throw new IllegalArgumentException("Unknown node type: " + varClass.getName());
        }
        if (nodeInstance instanceof StateBasedNodeInstance) {
        	if (ein.getTimers() != null && !ein.getTimers().isEmpty()) {
            	InternalKnowledgeRuntime kruntime = ((WorkflowProcessInstanceImpl)processInstance).getKnowledgeRuntime();
                TimerManager timerManager = ((InternalProcessRuntime)kruntime.getProcessRuntime()).getTimerManager();
        		List<Long> timerIds = new ArrayList<Long>();
        		for (EmbeddedProcessTimer ept: ein.getTimers()) {
        			try {
						MongoTimerMarshaller.deserialize(ept, timerManager, kruntime);
	        			timerIds.add(ept.getTimerInstance().getId());
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		((StateBasedNodeInstance)nodeInstance).internalSetTimerInstances(timerIds);
        	}
        }
        return nodeInstance;
    }
}
