package org.jbpm.persistence.mongodb.test.object;

import java.util.ArrayList;
import java.util.List;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class TestWorkItemHandler implements WorkItemHandler {

	private static TestWorkItemHandler INSTANCE = new TestWorkItemHandler();
	
	private WorkItem workItem;
	private WorkItem aborted;
    private List<WorkItem> workItems = new ArrayList<WorkItem>();

	public static TestWorkItemHandler getInstance() {
		return INSTANCE;
	}
	
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        workItems.add(workItem);
		this.workItem = workItem;
	}

	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		this.aborted = workItem;
	}
	
	public WorkItem getWorkItem() {
		WorkItem result = workItem;
		workItem = null;
		return result;
	}

	public WorkItem getAbortedWorkItem() {
		WorkItem result = aborted;
		aborted = null;
		return result;
	}

    public List<WorkItem> getWorkItems() {
        List<WorkItem> result = new ArrayList<WorkItem>(workItems);
        workItems.clear();
        return result;
    }	
}
