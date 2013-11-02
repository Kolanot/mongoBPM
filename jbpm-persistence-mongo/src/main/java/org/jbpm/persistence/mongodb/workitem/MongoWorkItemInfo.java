package org.jbpm.persistence.mongodb.workitem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.jbpm.persistence.mongodb.object.MongoJavaSerializable;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class MongoWorkItemInfo {

    private long id;
    private String name;
    private int state;
	private final Map<String, java.io.Serializable> parameters = new HashMap<String, java.io.Serializable>();
    private final Map<String, java.io.Serializable> results = new HashMap<String, java.io.Serializable>();
    private long processInstanceId;
    private String deploymentId;
    private boolean partialSerialized;
    private String serializedError = "";

    public MongoWorkItemInfo(){}
    
	public MongoWorkItemInfo(WorkItem workItem) {
    	this(workItem, workItem.getId());
    }
	
    public MongoWorkItemInfo(WorkItem workItem, long workItemId) {
		this.id = workItemId;
		internalSetWorkItem(workItem);
	}

	private void internalSetWorkItem(WorkItem workItem) {
		this.name = workItem.getName();
		this.state = workItem.getState();
		this.processInstanceId = workItem.getProcessInstanceId();
		if (workItem instanceof WorkItemImpl) {
			this.deploymentId = ((WorkItemImpl)workItem).getDeploymentId();
		} else {
			this.deploymentId = null;
		}
		for (Map.Entry<String, Object> entry:workItem.getParameters().entrySet()) {
			if(entry.getValue() == null) {
				this.parameters.put(entry.getKey(), null);
			} else if (entry.getValue() instanceof java.io.Serializable) {
				this.parameters.put(entry.getKey(), new MongoJavaSerializable((java.io.Serializable)entry.getValue()));
			} else {
				partialSerialized = true;
				serializedError = serializedError + "<p>WorkItem.parameter, key:" + entry.getKey() + ", value.class:" + entry.getValue().getClass() + ", value:" + entry.getValue() + "</p>";
			}
		}
		for (Map.Entry<String, Object> entry:workItem.getResults().entrySet()) {
			if(entry.getValue() == null) {
				this.results.put(entry.getKey(), null);
			} else if (entry.getValue() instanceof java.io.Serializable) {
				this.results.put(entry.getKey(), new MongoJavaSerializable((java.io.Serializable)entry.getValue()));
			} else {
				partialSerialized = true;
				serializedError = serializedError + "<p>WorkItem.result, key:" + entry.getKey() + ", value.class:" + entry.getValue().getClass() + ", value:" + entry.getValue() + "</p>";
			}
		}
	}
	
	public WorkItem getWorkItem() {
		WorkItemImpl workItem = new WorkItemImpl();
		workItem.setId(this.id);
		workItem.setName(this.name);
		workItem.setParameters(converToObjectMap(this.parameters));
		workItem.setResults(converToObjectMap(this.results));
		workItem.setState(this.state);
		workItem.setProcessInstanceId(this.processInstanceId);
		workItem.setDeploymentId(this.deploymentId);
		return workItem;
	}
	
	public void setWorkItem(WorkItem workItem) {
		this.id = workItem.getId();
		internalSetWorkItem(workItem);
	}
	
    private static Map<String, Object> converToObjectMap(Map<String, java.io.Serializable> serializedMap) {
    	Map<String, Object> objectMap = new HashMap<String, Object>();
    	objectMap.putAll(serializedMap);
		return objectMap;
    }

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getState() {
		return state;
	}

	public Map<String, java.io.Serializable> getParameters() {
		return parameters;
	}

	public Map<String, java.io.Serializable> getResults() {
		return results;
	}

	public long getProcessInstanceId() {
		return processInstanceId;
	}

	public String getDeploymentId() {
		return deploymentId;
	}
	
	public boolean isPartialSerialized() {
		return partialSerialized;
	}
	
	public String getSerializedError() {
		return serializedError;
	}
}
