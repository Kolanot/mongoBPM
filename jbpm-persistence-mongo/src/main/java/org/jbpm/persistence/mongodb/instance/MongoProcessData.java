package org.jbpm.persistence.mongodb.instance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.process.instance.WorkItem;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

@Embedded
public class MongoProcessData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Transient
	private Map<Long, MongoProcessInstanceInfo> processInstanceMap = new HashMap<Long, MongoProcessInstanceInfo>();
	@Transient
	private Map<Long, MongoWorkItemInfo> workItemMap = new HashMap<Long, MongoWorkItemInfo>();
	
	@Embedded
	private List<MongoProcessInstanceInfo> processInstances = new ArrayList<MongoProcessInstanceInfo>();

	@Embedded
	private List<MongoProcessInstanceInfo> closedProcessInstances = new ArrayList<MongoProcessInstanceInfo>(); 
	
	@Embedded
	private List<MongoWorkItemInfo> workItems = new ArrayList<MongoWorkItemInfo>();
	
	private long processTimerId;

	@PrePersist
	void prePersist() {
		processInstances.clear();
		processInstances.addAll(processInstanceMap.values());
		workItems.clear();
		workItems.addAll(workItemMap.values());
	}
	
	@PostLoad
	void postLoad() {
		for (MongoProcessInstanceInfo procInstInfo:processInstances) {
			processInstanceMap.put(procInstInfo.getProcessInstanceId(), procInstInfo);
		}
		for (MongoWorkItemInfo workItemInfo:workItems) {
			workItemMap.put(workItemInfo.getId(), workItemInfo);
		}
	}
	
	public Collection<MongoProcessInstanceInfo> getProcessInstances() {
		return processInstanceMap.values();
	}
	
	public MongoProcessInstanceInfo getProcessInstance(long procInstId) {
		return processInstanceMap.get(procInstId);
	}
	
	public void addProcessInstance(MongoProcessInstanceInfo procInstInfo) {
		processInstanceMap.put(procInstInfo.getProcessInstanceId(), procInstInfo);
	}

	public void removeProcessInstance(MongoProcessInstanceInfo processInstance) {
		closedProcessInstances.add(processInstance);
		processInstanceMap.remove(processInstance.getProcessInstanceId());
	}

	public void removeProcessInstance(long processInstanceId) {
		MongoProcessInstanceInfo procInstInfo = processInstanceMap.get(processInstanceId);
		if (procInstInfo != null)
			removeProcessInstance(procInstInfo);
	}
	
	public Collection<MongoWorkItemInfo> getWorkItems() {
		return workItemMap.values();
	}

	public void addWorkItem(MongoWorkItemInfo workItem) {
		workItemMap.put(workItem.getId(), workItem);
	}
	
	public void addWorkItem(WorkItem workItem) {
		MongoWorkItemInfo info = new MongoWorkItemInfo(workItem);
		addWorkItem(info);
	}
	
	public void addWorkItem(WorkItem workItem, long workItemId) {
		workItemMap.put(workItemId, new MongoWorkItemInfo(workItem, workItemId));
	}
	
	public boolean hasWorkItem(long workItemId) {
		return workItemMap.containsKey(workItemId);
	}

	public MongoWorkItemInfo getWorkItem(long workItemId) {
		return workItemMap.get(workItemId);
	}
	
	public void removeWorkItem(long workItemId) {
		workItemMap.remove(workItemId);
	}
	
	public long getProcessTimerId() {
		return processTimerId;
	}

	public void setProcessTimerId(long processTimerId) {
		this.processTimerId = processTimerId;
	}
}