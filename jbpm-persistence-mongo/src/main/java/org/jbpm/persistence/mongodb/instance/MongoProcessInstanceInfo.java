package org.jbpm.persistence.mongodb.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.object.MongoJavaSerializable;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.kie.api.runtime.process.ProcessInstance;
import org.drools.core.process.instance.WorkItem;
import org.kie.internal.process.CorrelationKey;

@Entity
public class MongoProcessInstanceInfo implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	protected Long                     processInstanceId;

    private long                version;

    @Property protected String           processId;
    @Property protected long             parentProcessInstanceId;
    @Property protected long             nodeInstanceCounter;
    @Property protected Date             startDate;
    @Property private Date               lastReadDate;
    @Property private Date               lastModificationDate;
    @Property protected int              state;

    @Property 
	protected Map<String, String>          swimlaneActors = new HashMap<String, String>();
    
    @Embedded
    protected MongoCorrelationKey          correlationKey;
    
    @Embedded
    protected Map<String, MongoJavaSerializable>    variables = new HashMap<String, MongoJavaSerializable>();

	@Property
    protected Set<String>                  eventTypes = new HashSet<String>();
	@Property
    protected Set<Long>                    exclusiveGroupInstanceIds = new HashSet<Long>();
	@Embedded
    protected List<EmbeddedNodeInstance>   nodeInstances = new ArrayList<EmbeddedNodeInstance>();
	@Embedded
	protected Map<String, Integer>         iterationLevels = new HashMap<String, Integer>();
	
	@Transient
    ProcessInstance                      processInstance;
   
    @Transient
    boolean                              modifiedSinceLastSave = false;
    @Transient
    boolean                              reconnected = false;
    @Property
    protected List<String>                 completedNodeIds = new ArrayList<String>();
	@Embedded
	private List<MongoWorkItemInfo> workItems = new ArrayList<MongoWorkItemInfo>();
    
    protected MongoProcessInstanceInfo() {
    }

    public MongoProcessInstanceInfo(ProcessInstance processInstance) {        
 	    setProcessInstance(processInstance);
    }
    
 	public long getProcessInstanceId() { 
 		return processInstanceId== null?0:processInstanceId.longValue();
    }
    
    public void setProcessInstanceId(long processInstanceId) { 
    	this.processInstanceId = processInstanceId;
    }
    
    public String getProcessId() {
    	return processId;
    }

    public void setProcessId(String processId) {
    	this.processId = processId;
    }
    
 	public long getParentProcessInstanceId() { 
 		return parentProcessInstanceId;
    }
     
    public void setParentProcessInstanceId(long parentProcessInstanceId) { 
    	this.parentProcessInstanceId = parentProcessInstanceId;
    }
     
 	public long getNodeInstanceCounter() { 
 		return nodeInstanceCounter;
    }
     
    public void setNodeInstanceCounter(long nodeInstanceCounter) { 
    	this.nodeInstanceCounter = nodeInstanceCounter;
    }
     
    public Date getStartDate() {
	   return startDate;
    }

    public Date getLastModificationDate() {
	   return lastModificationDate;
    }

    public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

	public Date getLastReadDate() {
	   return lastReadDate;
    }

    public void updateLastReadDate() {
	   lastReadDate = new Date();
    }

    public int getState() {
	   return state;
    }

    public void setState(int state) {
    	this.state = state;
    }
    
    public List<String> getCompletedNodeIds() {
    	return completedNodeIds;
    }
    
    public void setCompletedNodeIds(List<String> completedNodeIds) {
    	this.completedNodeIds = completedNodeIds;
    }
    
    public ProcessInstance getProcessInstance() {
       return processInstance;
    }
  
    public List<MongoWorkItemInfo> getWorkItems() {
		return workItems;
	}

    public void addWorkItem(WorkItem workItem, long workItemId) {
    	MongoWorkItemInfo workItemInfo = new MongoWorkItemInfo(workItem, workItemId);
    	workItems.add(workItemInfo);
    }
    
    public boolean hasWorkItem(long workItemId) {
    	return getWorkItem(workItemId) != null;
    }
    
    public MongoWorkItemInfo getWorkItem(long workItemId) {
    	for (MongoWorkItemInfo workItemInfo:workItems) {
    		if (workItemInfo.getId() == workItemId) return workItemInfo;
    	}
    	return null;
    }
    
    public void removeWorkItem(long workItemId) {
    	MongoWorkItemInfo workItemInfo = getWorkItem(workItemId);
    	if (workItemInfo != null) {
    		workItems.remove(workItemInfo);
    	}
    }
	@Override
    public boolean equals(Object obj) {
	   if ( obj == null ) {
		  return false;
	   }
	   if ( getClass() != obj.getClass() ) {
		  return false;
	   }
	   final MongoProcessInstanceInfo other = (MongoProcessInstanceInfo) obj;
	   if ( this.processInstanceId != other.processInstanceId ) {
		  return false;
	   }
	   if ( this.version != other.version ) {
		  return false;
	   }
	   if ( (this.processId == null) ? (other.processId != null) : !this.processId.equals( other.processId ) ) {
		  return false;
	   }
	   if ( this.startDate != other.startDate && (this.startDate == null || !this.startDate.equals( other.startDate )) ) {
		  return false;
	   }
	   if ( this.lastReadDate != other.lastReadDate && (this.lastReadDate == null || !this.lastReadDate.equals( other.lastReadDate )) ) {
		  return false;
	   }
	   if ( this.lastModificationDate != other.lastModificationDate && (this.lastModificationDate == null || !this.lastModificationDate.equals( other.lastModificationDate )) ) {
		  return false;
	   }
	   if ( this.state != other.state ) {
		  return false;
	   }
	   if ( this.eventTypes != other.eventTypes && (this.eventTypes == null || !this.eventTypes.equals( other.eventTypes )) ) {
		  return false;
	   }
	   if ( this.processInstance != other.processInstance && (this.processInstance == null || !this.processInstance.equals( other.processInstance )) ) {
		  return false;
	   }
	   
	   return true;
    }

    @Override
    public int hashCode() {
	   int hash = 7;
	   hash = 61 * hash + (processInstanceId != null ? processInstanceId.hashCode() : 0);
	   hash = 61 * hash + (int)this.version;
	   hash = 61 * hash + (this.processId != null ? this.processId.hashCode() : 0);
	   hash = 61 * hash + (this.startDate != null ? this.startDate.hashCode() : 0);
	   hash = 61 * hash + (this.lastReadDate != null ? this.lastReadDate.hashCode() : 0);
	   hash = 61 * hash + (this.lastModificationDate != null ? this.lastModificationDate.hashCode() : 0);
	   hash = 61 * hash + this.state;
	   hash = 61 * hash + (this.eventTypes != null ? this.eventTypes.hashCode() : 0);
	   hash = 61 * hash + (this.processInstance != null ? this.processInstance.hashCode() : 0);
	   return hash;
    }

    public long getVersion() {
	   return version;
    }
    
    public Set<String> getEventTypes() {
	   return eventTypes;
    }

    public void clearProcessInstance(){
	   processInstance = null;
    }
    
    public MongoCorrelationKey getCorrelationKey() {
		return correlationKey;
	}
    
	public void setCorrelationKey(MongoCorrelationKey correlationKey) {
		this.correlationKey = correlationKey;
	}
	
	public void assignCorrelationKey(CorrelationKey correlationKey) {
		if (correlationKey instanceof MongoCorrelationKey) {
			this.correlationKey = (MongoCorrelationKey)correlationKey;
		} else {
			MongoCorrelationKey key = new MongoCorrelationKey(correlationKey);
			this.correlationKey = key;
		}
	}

	public void assignCorrelationKey(String keyName, String propertyName, String propertyValue) {
		MongoCorrelationKey correlationKey = new MongoCorrelationKey(keyName, propertyName, propertyValue);
		this.correlationKey = correlationKey;
	}

	public boolean isModifiedSinceLastSave() {
		return modifiedSinceLastSave;
	}

	public void setModifiedSinceLastSave(boolean modifiedSinceLastSave) {
		this.modifiedSinceLastSave = modifiedSinceLastSave;
	}

	public boolean isReconnected() {
		return reconnected;
	}

	public void reconnect() {
		reconnected = true;
	}

    public void setProcessInstance(ProcessInstance processInstance) {        
 	    this.processInstance = processInstance;
 	}
    

    public Map<String, String> getSwimlaneActors() {
		return swimlaneActors;
	}

	public Map<String, MongoJavaSerializable> getVariables() {
		return variables;
	}

	public Set<Long> getExclusiveGroupInstanceIds() {
		return exclusiveGroupInstanceIds;
	}

	public List<EmbeddedNodeInstance> getNodeInstances() {
		return nodeInstances;
	}

	public Map<String, Integer> getIterationLevels() {
		return iterationLevels;
	}
	
	@Embedded
    public static class EmbeddedNodeInstance implements Serializable {
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		long id;
    	long nodeId;
    	int level;
    	String nodeClassName;
    	List<Long> timerIds = new ArrayList<Long>();
    	long workItemId;
    	long subProcessInstanceId;
    	Map<Long, Integer> triggers;
    	@Embedded
		Map<String, MongoJavaSerializable>    variables = new HashMap<String, MongoJavaSerializable>();
        private Set<Long>                exclusiveGroupInstanceIds = new HashSet<Long>();
        @Embedded
        private List<EmbeddedNodeInstance>   nodeInstances = new ArrayList<EmbeddedNodeInstance>();

    	public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public long getNodeId() {
			return nodeId;
		}
		public void setNodeId(long nodeId) {
			this.nodeId = nodeId;
		}
		public int getLevel() {
			return level;
		}
		public void setLevel(int level) {
			this.level = level;
		}
		public String getNodeClassName() {
			return nodeClassName;
		}
		public void setNodeClassName(String nodeClassName) {
			this.nodeClassName = nodeClassName;
		}
		
		public List<Long> getTimerIds() {
			return timerIds;
		}
		public void setTimerIds(List<Long> timers) {
			if (timers != null) {
				this.timerIds.clear();
				this.timerIds.addAll(timers);
			}
		}

        public long getWorkItemId() {
			return workItemId;
		}
		public long getSubProcessInstanceId() {
			return subProcessInstanceId;
		}
		public Map<Long, Integer> getTriggers() {
			return triggers;
		}
		public void setWorkItemId(long workItemId) {
			this.workItemId = workItemId;
		}
		public void setSubProcessInstanceId(long subProcessInstanceId) {
			this.subProcessInstanceId = subProcessInstanceId;
		}
		public void setTriggers(Map<Long, Integer> triggers) {
			this.triggers = triggers;
		}
		public Map<String, MongoJavaSerializable> getVariables() {
			return variables;
		}
		public Set<Long> getExclusiveGroupInstanceIds() {
			return exclusiveGroupInstanceIds;
		}
		public List<EmbeddedNodeInstance> getNodeInstances() {
			return nodeInstances;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((nodeClassName == null) ? 0 : nodeClassName.hashCode());
			result = prime * result + (int) (id ^ (id >>> 32));
			result = prime * result + (int) (nodeId ^ (nodeId >>> 32));
			result = prime * result + (int) (level ^ (level >>> 32));
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EmbeddedNodeInstance other = (EmbeddedNodeInstance) obj;
			if (nodeClassName == null) {
				if (other.nodeClassName != null)
					return false;
			} else if (!nodeClassName.equals(other.nodeClassName))
				return false;
			if (id != other.id)
				return false;
			if (nodeId != other.nodeId)
				return false;
			if (level != other.level) 
				return false;
			return true;
		}
    	
    }
}