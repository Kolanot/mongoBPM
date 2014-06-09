package org.jbpm.persistence.mongodb.task.util;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.mongodb.task.model.MongoAttachmentImpl;
import org.jbpm.persistence.mongodb.task.model.MongoBooleanExpressionImpl;
import org.jbpm.persistence.mongodb.task.model.MongoCommentImpl;
import org.jbpm.persistence.mongodb.task.model.MongoDeadlineImpl;
import org.jbpm.persistence.mongodb.task.model.MongoEscalationImpl;
import org.jbpm.persistence.mongodb.task.model.MongoGroupImpl;
import org.jbpm.persistence.mongodb.task.model.MongoI18NTextImpl;
import org.jbpm.persistence.mongodb.task.model.MongoUserImpl;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.model.BooleanExpression;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.Escalation;

public class MongoPersistenceUtil {

	public static MongoUserImpl convertToUserImpl(User user) { 
        if( user == null ) { 
            return null;
        }
        if( user instanceof MongoUserImpl ) { 
            return (MongoUserImpl) user;
        } else { 
            return new MongoUserImpl(user);
        }
    }

    public static MongoGroupImpl convertToGroupImpl(Group group) { 
        if( group == null ) { 
            return null;
        }
        if( group instanceof MongoGroupImpl ) { 
            return (MongoGroupImpl) group;
        } else { 
            return new MongoGroupImpl(group);
        }
    }

    public static MongoCommentImpl convertToCommentImpl(Comment comment) {
    	if (comment == null) {
    		return null;
    	}
    	if (comment instanceof MongoCommentImpl) {
    		return (MongoCommentImpl) comment;
    	} else {
    		return new MongoCommentImpl(comment);
    	}
    }

    public static List<Comment> convertToCommentImpl(List<Comment> comments) {
    	boolean isMongoComment = true;
    	List<Comment> list = new ArrayList<Comment>();
    	if (comments == null) return list;
    	for (Comment comment:comments) {
    		if (comment instanceof MongoCommentImpl) {
    			list.add(comment);
    		} else {
    			isMongoComment = false;
        		list.add(new MongoCommentImpl(comment));
    		}
    	}
    	return isMongoComment? comments:list;
    }

    public static List<Attachment> convertToAttachmentImpl(List<Attachment> attachments) {
    	boolean isMongoAttachment = true;
    	List<Attachment> list = new ArrayList<Attachment>();
    	if (attachments == null) return list;
    	for (Attachment attachment: attachments) {
    		if (attachment instanceof MongoAttachmentImpl) {
    			list.add(attachment);
    		} else {
    			isMongoAttachment = false;
        		list.add(new MongoAttachmentImpl(attachment));
    		}
    	}
    	return isMongoAttachment? attachments:list;
    }

    public static List<Deadline> convertToDeadlineImpl(List<Deadline> deadlines) {
    	boolean isMongoDeadline = true;
    	List<Deadline> list = new ArrayList<Deadline>();
    	if (deadlines == null) return list;
    	for (Deadline Deadline: deadlines) {
    		if (Deadline instanceof MongoDeadlineImpl) {
    			list.add(Deadline);
    		} else {
    			isMongoDeadline = false;
        		list.add(new MongoDeadlineImpl(Deadline));
    		}
    	}
    	return isMongoDeadline? deadlines:list;
    }

    public static List<I18NText> convertToI18NTextImpl(List<I18NText> i18NTexts) {
    	boolean isMongoI18NText = true;
    	List<I18NText> list = new ArrayList<I18NText>();
    	if (i18NTexts == null) return list;
    	for (I18NText i18NText: i18NTexts) {
    		if (i18NText instanceof MongoI18NTextImpl) {
    			list.add(i18NText);
    		} else {
    			isMongoI18NText = false;
        		list.add(new MongoI18NTextImpl(i18NText));
    		}
    	}
    	return isMongoI18NText? i18NTexts:list;
    }

    public static List<Escalation> convertToEscalationImpl(List<Escalation> escalations) {
    	boolean isMongoEscalation = true;
    	List<Escalation> list = new ArrayList<Escalation>();
    	if (escalations == null) return list;
    	for (Escalation escalation: escalations) {
    		if (escalation instanceof MongoEscalationImpl) {
    			list.add(escalation);
    		} else {
    			isMongoEscalation = false;
        		list.add(new MongoEscalationImpl(escalation));
    		}
    	}
    	return isMongoEscalation? escalations:list;
    }

    public static List<BooleanExpression> convertToBooleanExpressionImpl(List<BooleanExpression> booleanExpressions) {
    	boolean isMongoBooleanExpression = true;
    	List<BooleanExpression> list = new ArrayList<BooleanExpression>();
    	if (booleanExpressions == null) return list;
    	for (BooleanExpression booleanExpression: booleanExpressions) {
    		if (booleanExpression instanceof MongoBooleanExpressionImpl) {
    			list.add(booleanExpression);
    		} else {
    			isMongoBooleanExpression = false;
        		list.add(new MongoBooleanExpressionImpl(booleanExpression));
    		}
    	}
    	return isMongoBooleanExpression? booleanExpressions:list;
    }

	public static List<OrganizationalEntity> convertToPersistentOrganizationalEntity(
			List<OrganizationalEntity> orgEntList) {
		List<OrganizationalEntity> persistentOrgEnts = orgEntList;
		if (persistentOrgEnts != null && !persistentOrgEnts.isEmpty()) {
			persistentOrgEnts = new ArrayList<OrganizationalEntity>(
					orgEntList.size());
			for (OrganizationalEntity orgEnt : orgEntList) {
				if (orgEnt instanceof MongoUserImpl
						|| orgEnt instanceof MongoGroupImpl) {
					persistentOrgEnts.add(orgEnt);
				} else if (orgEnt instanceof User) {
					persistentOrgEnts.add(new MongoUserImpl(orgEnt.getId()));
				} else if (orgEnt instanceof Group) {
					persistentOrgEnts.add(new MongoGroupImpl(orgEnt.getId()));
				} else {
					throw new IllegalStateException(
							"Unknown user or group object: "
									+ orgEnt.getClass().getName());
				}
			}
		}
		return persistentOrgEnts;
	}

}
