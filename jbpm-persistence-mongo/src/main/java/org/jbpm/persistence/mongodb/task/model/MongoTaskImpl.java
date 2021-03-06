/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.persistence.mongodb.task.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import org.jbpm.persistence.mongodb.task.util.CollectionUtils;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.Delegation;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.SubTasksStrategy;
import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class MongoTaskImpl implements InternalTask {
	private Long id = 0L;
	
	private int version = 0;

	private int priority;

	private List<I18NText> names = Collections.emptyList();

	private List<I18NText> subjects = Collections.emptyList();

	private List<I18NText> descriptions = Collections.emptyList();

	@Embedded
	private MongoPeopleAssignmentsImpl peopleAssignments;

	@Embedded
	private MongoDelegationImpl delegation;

	@Embedded
	private MongoTaskDataImpl taskData;

	@Embedded
	private MongoDeadlinesImpl deadlines;

	// Default Behaviour
	private SubTasksStrategy subTaskStrategy = SubTasksStrategy.NoAction;

	private String taskType;

	private String formName;

	private Short archived = 0;

	public MongoTaskImpl() {
	}

	public MongoTaskImpl(Task task) {
		this.names = task.getNames();
		this.descriptions = task.getDescriptions();
		this.subjects = task.getSubjects();
		this.priority = task.getPriority();
		this.id = task.getId();
		this.peopleAssignments = new MongoPeopleAssignmentsImpl(task.getPeopleAssignments());
		this.taskData = new MongoTaskDataImpl(task.getTaskData());
		this.taskType = task.getTaskType();
		if (task instanceof InternalTask) {
			InternalTask internalTask = (InternalTask)task;
			this.archived = internalTask.getArchived();
			this.version = internalTask.getVersion();
			this.formName = internalTask.getFormName();
			this.delegation = new MongoDelegationImpl(internalTask.getDelegation());
			this.deadlines = new MongoDeadlinesImpl(internalTask.getDeadlines());
			this.subTaskStrategy = internalTask.getSubTaskStrategy();
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeInt(priority);
		out.writeShort(archived);
		out.writeUTF(taskType);
		out.writeUTF(formName);
		CollectionUtils.writeI18NTextList(names, out);
		CollectionUtils.writeI18NTextList(subjects, out);
		CollectionUtils.writeI18NTextList(descriptions, out);

		if (subTaskStrategy != null) {
			out.writeBoolean(true);
			out.writeUTF(subTaskStrategy.toString());
		} else {
			out.writeBoolean(false);
		}

		if (peopleAssignments != null) {
			out.writeBoolean(true);
			peopleAssignments.writeExternal(out);
		} else {
			out.writeBoolean(false);
		}

		if (delegation != null) {
			out.writeBoolean(true);
			delegation.writeExternal(out);
		} else {
			out.writeBoolean(false);
		}

		if (taskData != null) {
			out.writeBoolean(true);
			taskData.writeExternal(out);
		} else {
			out.writeBoolean(false);
		}

		if (deadlines != null) {
			out.writeBoolean(true);
			deadlines.writeExternal(out);
		} else {
			out.writeBoolean(false);
		}

	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		id = in.readLong();
		priority = in.readInt();
		archived = in.readShort();
		taskType = in.readUTF();
		formName = in.readUTF();
		names = CollectionUtils.readI18NTextList(in);
		subjects = CollectionUtils.readI18NTextList(in);
		descriptions = CollectionUtils.readI18NTextList(in);

		if (in.readBoolean()) {
			subTaskStrategy = SubTasksStrategy.valueOf(in.readUTF());
		}

		if (in.readBoolean()) {
			peopleAssignments = new MongoPeopleAssignmentsImpl();
			peopleAssignments.readExternal(in);
		}

		if (in.readBoolean()) {
			delegation = new MongoDelegationImpl();
			delegation.readExternal(in);
		}

		if (in.readBoolean()) {
			taskData = new MongoTaskDataImpl();
			taskData.readExternal(in);
		}

		if (in.readBoolean()) {
			deadlines = new MongoDeadlinesImpl();
			deadlines.readExternal(in);
		}

	}

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Boolean isArchived() {
		if (archived == null) {
			return null;
		}
		return (archived == 1) ? Boolean.TRUE : Boolean.FALSE;
	}

	public void setArchived(Boolean archived) {
		if (archived == null) {
			this.archived = null;
		} else {
			this.archived = (archived == true) ? new Short("1")
					: new Short("0");
		}
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public List<I18NText> getNames() {
		return names;
	}

	public void setNames(List<I18NText> names) {
		this.names = names;
	}

	public List<I18NText> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<I18NText> subjects) {
		this.subjects = subjects;
	}

	public List<I18NText> getDescriptions() {
		return descriptions;
	}

	public void setDescriptions(List<I18NText> descriptions) {
		this.descriptions = descriptions;
	}

	public PeopleAssignments getPeopleAssignments() {
		return peopleAssignments;
	}

	public void setPeopleAssignments(PeopleAssignments peopleAssignments) {
		this.peopleAssignments = (MongoPeopleAssignmentsImpl) peopleAssignments;
	}

	public Delegation getDelegation() {
		return delegation;
	}

	public void setDelegation(Delegation delegation) {
		this.delegation = (MongoDelegationImpl) delegation;
	}

	public TaskData getTaskData() {
		return taskData;
	}

	public void setTaskData(TaskData taskData) {
		this.taskData = (MongoTaskDataImpl) taskData;
	}

	public Deadlines getDeadlines() {
		return deadlines;
	}

	public void setDeadlines(Deadlines deadlines) {
		this.deadlines = (MongoDeadlinesImpl) deadlines;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getFormName() {
		return formName;
	}

	public void setFormName(String formName) {
		this.formName = formName;
	}

	public Short getArchived() {
		return archived;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + priority;
		result = prime * result + archived.hashCode();
		result = prime * result
				+ ((taskType == null) ? 0 : taskType.hashCode());
		result = prime * result + CollectionUtils.hashCode(descriptions);
		result = prime * result + CollectionUtils.hashCode(names);
		result = prime * result + CollectionUtils.hashCode(subjects);
		result = prime
				* result
				+ ((peopleAssignments == null) ? 0 : peopleAssignments
						.hashCode());
		result = prime * result
				+ ((delegation == null) ? 0 : delegation.hashCode());
		result = prime * result
				+ ((taskData == null) ? 0 : taskData.hashCode());
		result = prime * result
				+ ((deadlines == null) ? 0 : deadlines.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MongoTaskImpl))
			return false;
		MongoTaskImpl other = (MongoTaskImpl) obj;
		if (this.archived != other.archived) {
			return false;
		}
		if (taskType == null) {
			if (other.taskType != null)
				return false;
		} else if (!taskType.equals(other.taskType))
			return false;
		if (deadlines == null) {
			if (other.deadlines != null) {

			}
		} else if (!deadlines.equals(other.deadlines))
			return false;
		if (delegation == null) {
			if (other.delegation != null)
				return false;
		} else if (!delegation.equals(other.delegation))
			return false;
		if (peopleAssignments == null) {
			if (other.peopleAssignments != null)
				return false;
		} else if (!peopleAssignments.equals(other.peopleAssignments))
			return false;

		if (priority != other.priority)
			return false;
		if (taskData == null) {
			if (other.taskData != null)
				return false;
		} else if (!taskData.equals(other.taskData))
			return false;
		return (CollectionUtils.equals(descriptions, other.descriptions)
				&& CollectionUtils.equals(names, other.names) && CollectionUtils
					.equals(subjects, other.subjects));
	}

	public SubTasksStrategy getSubTaskStrategy() {
		return subTaskStrategy;
	}

	public void setSubTaskStrategy(SubTasksStrategy subTaskStrategy) {
		this.subTaskStrategy = subTaskStrategy;
	}

	@Override
	public int getVersion() {
		return version;
	}

}
