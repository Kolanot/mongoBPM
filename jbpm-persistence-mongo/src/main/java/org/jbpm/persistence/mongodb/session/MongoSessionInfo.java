package org.jbpm.persistence.mongodb.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;

import org.jbpm.persistence.mongodb.instance.MongoProcessData;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.rule.MongoRuleData;
import org.jbpm.persistence.mongodb.session.timer.EmbeddedTimer;
import org.kie.api.runtime.KieSession;

@Entity
public class MongoSessionInfo {

	@Id 
	private Integer id;

	@Transient
    boolean  modifiedSinceLastSave = false;
	
	@Transient KieSession session;

    @Version
    private long version;

    private Date startDate;
    private Date lastModificationDate;
    
    private boolean multithread = false;
    
    private long pseudoClockTime = 0;

    @Embedded
    private final MongoRuleData ruleData = new MongoRuleData(); 
    
    @Embedded
    private final MongoProcessData processData = new MongoProcessData();

	@Embedded 
    private List<EmbeddedTimer> timers = new ArrayList<EmbeddedTimer>();
   
    public MongoSessionInfo() {
    }

    public MongoSessionInfo(KieSession session) {
    	this.session = session;
    }
    
    public KieSession getSession() {
    	return session;
    }
    
    public void setSession(KieSession session) {
    	this.session = session;
    }
    
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer ksessionId) {
        this.id = ksessionId;
    }

    public long getVersion() {
        return this.version;
    }
    
    public Date getStartDate() {
        return this.startDate;
    }

    public Date getLastModificationDate() {
        return this.lastModificationDate;
    }

	public void setLastModificationDate(Date date) {
        this.lastModificationDate = date;
    }

    public long getPseudoClockTime() {
		return pseudoClockTime;
	}

	public void setPseudoClockTime(long pseudoClockTime) {
		this.pseudoClockTime = pseudoClockTime;
	}

	public List<EmbeddedTimer> getTimers() {
		return timers;
	}

	public boolean isModifiedSinceLastSave() {
		return modifiedSinceLastSave;
	}

	public void setModifiedSinceLastSave(boolean modifiedSinceLastSave) {
		this.modifiedSinceLastSave = modifiedSinceLastSave;
	}

	public boolean isMultithread() {
		return multithread;
	}

	public void setMultithread(boolean multithread) {
		this.multithread = multithread;
	}

	public MongoRuleData getRuleData() {
		return ruleData;
	}

	public MongoProcessData getProcessdata() {
		return processData;
	}

	public void addProcessInstance(MongoProcessInstanceInfo procInfo) {
		processData.addProcessInstance(procInfo);
	}
	
	public void clearTimers() {
		timers.clear();
	}
}
