package org.jbpm.persistence.mongodb.instance;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.core.process.instance.WorkItem;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;

@Embedded
public class MongoProcessData implements Externalizable {
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

	// for externalization
	public MongoProcessData() {}
		
	
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

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		int instanceCount = in.readInt();
		for (int i = 0; i < instanceCount; i++) {
			MongoProcessInstanceInfo instance = (MongoProcessInstanceInfo)in.readObject();
			processInstances.add(instance);
		}
		int closedCount = in.readInt(); 
		for (int i = 0; i < closedCount; i++) {
			MongoProcessInstanceInfo instance = (MongoProcessInstanceInfo)in.readObject();
			closedProcessInstances.add(instance);
		}
		int itemCount = in.readInt();
		for (int i = 0; i < itemCount; i++) {
			MongoWorkItemInfo item = (MongoWorkItemInfo)in.readObject();
			workItems.add(item);
		}
		processTimerId = in.readLong();
		postLoad();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		prePersist();
		out.writeInt(processInstances.size());
		for (MongoProcessInstanceInfo instance: processInstances) {
			out.writeObject(instance);
		}
		out.writeInt(closedProcessInstances.size());
		for (MongoProcessInstanceInfo instance: closedProcessInstances) {
			out.writeObject(instance);
		}
		out.writeInt(workItems.size());
		for (MongoWorkItemInfo item: workItems) {
			out.writeObject(item);
		}
		out.writeLong(processTimerId);
	}
}