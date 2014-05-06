package org.jbpm.persistence.mongodb.task.service;

import org.jbpm.persistence.mongodb.task.model.MongoAttachmentImpl;
import org.jbpm.persistence.mongodb.task.model.MongoBooleanExpressionImpl;
import org.jbpm.persistence.mongodb.task.model.MongoCommentImpl;
import org.jbpm.persistence.mongodb.task.model.MongoContentDataImpl;
import org.jbpm.persistence.mongodb.task.model.MongoContentImpl;
import org.jbpm.persistence.mongodb.task.model.MongoDeadlineImpl;
import org.jbpm.persistence.mongodb.task.model.MongoDeadlinesImpl;
import org.jbpm.persistence.mongodb.task.model.MongoDelegationImpl;
import org.jbpm.persistence.mongodb.task.model.MongoEmailNotificationHeaderImpl;
import org.jbpm.persistence.mongodb.task.model.MongoEmailNotificationImpl;
import org.jbpm.persistence.mongodb.task.model.MongoEscalationImpl;
import org.jbpm.persistence.mongodb.task.model.MongoFaultDataImpl;
import org.jbpm.persistence.mongodb.task.model.MongoGroupImpl;
import org.jbpm.persistence.mongodb.task.model.MongoI18NTextImpl;
import org.jbpm.persistence.mongodb.task.model.MongoLanguageImpl;
import org.jbpm.persistence.mongodb.task.model.MongoNotificationImpl;
import org.jbpm.persistence.mongodb.task.model.MongoPeopleAssignmentsImpl;
import org.jbpm.persistence.mongodb.task.model.MongoReassignmentImpl;
import org.jbpm.persistence.mongodb.task.model.MongoTaskDataImpl;
import org.jbpm.persistence.mongodb.task.model.MongoTaskDefImpl;
import org.jbpm.persistence.mongodb.task.model.MongoTaskImpl;
import org.jbpm.persistence.mongodb.task.model.MongoUserImpl;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelFactory;
import org.kie.internal.task.api.model.BooleanExpression;
import org.kie.internal.task.api.model.ContentData;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.Delegation;
import org.kie.internal.task.api.model.EmailNotification;
import org.kie.internal.task.api.model.EmailNotificationHeader;
import org.kie.internal.task.api.model.Escalation;
import org.kie.internal.task.api.model.FaultData;
import org.kie.internal.task.api.model.Language;
import org.kie.internal.task.api.model.Notification;
import org.kie.internal.task.api.model.Reassignment;
import org.kie.internal.task.api.model.TaskDef;

public class MongoTaskModelFactory implements TaskModelFactory {

	@Override
	public Attachment newAttachment() {
		return new MongoAttachmentImpl();
	}

	@Override
	public BooleanExpression newBooleanExpression() {
		
		return new MongoBooleanExpressionImpl();
	}

	@Override
	public Comment newComment() {
		
		return new MongoCommentImpl();
	}

	@Override
	public ContentData newContentData() {
		
		return new MongoContentDataImpl();
	}

	@Override
	public Content newContent() {
		
		return new MongoContentImpl();
	}

	@Override
	public Deadline newDeadline() {
		
		return new MongoDeadlineImpl();
	}

	@Override
	public Deadlines newDeadlines() {
		
		return new MongoDeadlinesImpl();
	}

	@Override
	public Delegation newDelegation() {
		
		return new MongoDelegationImpl();
	}

	@Override
	public EmailNotificationHeader newEmailNotificationHeader() {
		
		return new MongoEmailNotificationHeaderImpl();
	}

	@Override
	public EmailNotification newEmialNotification() {
		
		return new MongoEmailNotificationImpl();
	}

	@Override
	public Escalation newEscalation() {
		
		return new MongoEscalationImpl();
	}

	@Override
	public FaultData newFaultData() {
		
		return new MongoFaultDataImpl();
	}

	@Override
	public Group newGroup() {
		
		return new MongoGroupImpl();
	}

	@Override
	public I18NText newI18NText() {
		
		return new MongoI18NTextImpl();
	}

	@Override
	public Language newLanguage() {
		
		return new MongoLanguageImpl();
	}

	@Override
	public Notification newNotification() {
		
		return new MongoNotificationImpl();
	}

	@Override
	public OrganizationalEntity newOrgEntity() {
		
		throw new UnsupportedOperationException("OrganizationalEntity not supported");
	}

	@Override
	public PeopleAssignments newPeopleAssignments() {
		
		return new MongoPeopleAssignmentsImpl();
	}

	@Override
	public Reassignment newReassignment() {
		
		return new MongoReassignmentImpl();
	}

	@Override
	public TaskData newTaskData() {
		
		return new MongoTaskDataImpl();
	}

	@Override
	public TaskDef newTaskDef() {
		
		return new MongoTaskDefImpl();
	}

	@Override
	public Task newTask() {
		
		return new MongoTaskImpl();
	}

	@Override
	public User newUser() {
		
		return new MongoUserImpl();
	}

}
