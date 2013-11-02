package org.jbpm.persistence.mongodb.rule;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.common.AgendaGroupQueueImpl;

import org.mongodb.morphia.annotations.Embedded;

@Embedded 
public class EmbeddedAgenda {
	
    @Embedded 
    public static class EmbeddedAgendaGroup {
    	private String name;
    	private boolean active;
    	private boolean autoDeactivate;
    	private long clearedForRecency;
    	private boolean hasRuleFlowListener;
    	private long activatedForRecency;
    	
    	public static class NodeInstance {
    		private final long processInstanceId;
    		private final String nodeInstanceId;
    		public NodeInstance(long processInstanceId, String nodeInstanceId) {
    			this.processInstanceId = processInstanceId;
    			this.nodeInstanceId = nodeInstanceId;
    		}
			public long getProcessInstanceId() {
				return processInstanceId;
			}
			public String getNodeInstanceId() {
				return nodeInstanceId;
			}
    	}
    	private final List<NodeInstance> nodeInstances = new ArrayList<NodeInstance>();
    	
    	public EmbeddedAgendaGroup() {}
    	
        public EmbeddedAgendaGroup(String name, boolean active,
				boolean autoDeactivate, long clearedForRecency,
				boolean hasRuleFlowListener, long activatedForRecency) {
			super();
			this.name = name;
			this.active = active;
			this.autoDeactivate = autoDeactivate;
			this.clearedForRecency = clearedForRecency;
			this.hasRuleFlowListener = hasRuleFlowListener;
			this.activatedForRecency = activatedForRecency;
		}

		public EmbeddedAgendaGroup (AgendaGroupQueueImpl group) {
			this.name = group.getName();
			this.active = group.isActive();
			this.autoDeactivate = group.isAutoDeactivate();
			this.clearedForRecency = group.getClearedForRecency();
			this.hasRuleFlowListener = group.isRuleFlowListener();
			this.activatedForRecency = group.getActivatedForRecency();
    	}

    	public String getName() {
			return name;
		}
		public boolean isActive() {
			return active;
		}
		public long getActivatedForRecency() {
			return activatedForRecency;
		}
		public boolean isAutoDeactivate() {
			return autoDeactivate;
		}
		public long getClearedForRecency() {
			return clearedForRecency;
		}
		public boolean hasRuleFlowListener() {
			return hasRuleFlowListener;
		}
		public List<NodeInstance> getNodeInstances() {
			return nodeInstances;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public void setAutoDeactivate(boolean autoDeactivate) {
			this.autoDeactivate = autoDeactivate;
		}

		public void setClearedForRecency(long clearedForRecency) {
			this.clearedForRecency = clearedForRecency;
		}

		public void setRuleFlowListener(boolean hasRuleFlowListener) {
			this.hasRuleFlowListener = hasRuleFlowListener;
		}

		public void setActivatedForRecency(long activatedForRecency) {
			this.activatedForRecency = activatedForRecency;
		}
		
    }

    @Embedded 
    private List<EmbeddedAgendaGroup> agendaGroups = new ArrayList<EmbeddedAgendaGroup>();
    
    private List<String> groupStack = new ArrayList<String>();

    @Embedded 
    private List<EmbeddedActivation> matches = new ArrayList<EmbeddedActivation>();
    
    @Embedded
    private List<EmbeddedActivation> ruleActivations = new ArrayList<EmbeddedActivation>(); 
    
	public List<EmbeddedAgendaGroup> getAgendaGroups() {
		return agendaGroups;
	}

	public List<String> getGroupStack() {
		return groupStack;
	}

	public List<EmbeddedActivation> getMatches() {
		return matches;
	}
	
	public void addMatch(EmbeddedActivation match) {
		matches.add(match);
	}

	public List<EmbeddedActivation> getRuleActivations() {
		return ruleActivations;
	}
	
	public void addRuleActivation(EmbeddedActivation ruleActivation) {
		matches.add(ruleActivation);
	}
}