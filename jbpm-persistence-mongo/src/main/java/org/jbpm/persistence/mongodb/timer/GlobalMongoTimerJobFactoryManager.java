/*
 * Copyright 2012 JBoss Inc
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
package org.jbpm.persistence.mongodb.timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.core.command.CommandService;
import org.drools.core.time.InternalSchedulerService;
import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.SelfRemovalJob;
import org.drools.core.time.SelfRemovalJobContext;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.TimerJobFactoryManager;
import org.drools.core.time.impl.TimerJobInstance;
import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;

public class GlobalMongoTimerJobFactoryManager implements TimerJobFactoryManager {
    
    private CommandService commandService;
    private Map<Integer, Map<Long, TimerJobInstance>> timerInstances;
    private Map<Long, TimerJobInstance> singleTimerInstances;
    
    public GlobalMongoTimerJobFactoryManager() {
        timerInstances = new ConcurrentHashMap<Integer, Map<Long, TimerJobInstance>>();
        singleTimerInstances = new ConcurrentHashMap<Long, TimerJobInstance>();
    }
    
    public void setCommandService(CommandService commandService) {
        this.commandService = commandService;
    }
    
    public TimerJobInstance createTimerJobInstance(Job job,
                                                   JobContext ctx,
                                                   Trigger trigger,
                                                   JobHandle handle,
                                                   InternalSchedulerService scheduler) {
        Map<Long, TimerJobInstance> local = null;
        if (ctx instanceof ProcessJobContext) {
            int sessionId = ((ProcessJobContext) ctx).getSessionId();
            Map<Long, TimerJobInstance> instances = timerInstances.get(sessionId);
            if (instances == null) {
                instances = new ConcurrentHashMap<Long, TimerJobInstance>();
                timerInstances.put(sessionId, instances);
            }
            local = timerInstances.get(sessionId);
        } else {
            local = singleTimerInstances;
        }
        ctx.setJobHandle( handle );
        
        if (scheduler instanceof GlobalTimerService) {
        	// override GlobalTimerService.TimerJobFactoryManager, which is default to JPATimerJobFactoryManager
        	((GlobalTimerService)scheduler).setTimerJobFactoryManager(this);
        }
        
        GlobalMongoTimerJobInstance jobInstance = new GlobalMongoTimerJobInstance( new SelfRemovalJob( job ),
                                                                   new SelfRemovalJobContext( ctx,
                                                                                              local ),
                                                                   trigger,
                                                                   handle,
                                                                   scheduler);
    
        return jobInstance;
    }
    
    public void addTimerJobInstance(TimerJobInstance instance) {
    
        JobContext ctx = instance.getJobContext();
        if (ctx instanceof SelfRemovalJobContext) {
            ctx = ((SelfRemovalJobContext) ctx).getJobContext();
        }
        Map<Long, TimerJobInstance> instances = null;
        if (ctx instanceof ProcessJobContext) {
            int sessionId = ((ProcessJobContext)ctx).getSessionId();
            instances = timerInstances.get(sessionId);
            if (instances == null) {
                instances = new ConcurrentHashMap<Long, TimerJobInstance>();
                timerInstances.put(sessionId, instances);
            }
        } else {
            instances = singleTimerInstances;
        }
        instances.put( instance.getJobHandle().getId(),
                                 instance );        
    }
    
    public void removeTimerJobInstance(TimerJobInstance instance) {
    
        JobContext ctx = instance.getJobContext();
        if (ctx instanceof SelfRemovalJobContext) {
            ctx = ((SelfRemovalJobContext) ctx).getJobContext();
        }
        Map<Long, TimerJobInstance> instances = null;
        if (ctx instanceof ProcessJobContext) {
            int sessionId = ((ProcessJobContext)ctx).getSessionId();
            instances = timerInstances.get(sessionId);
            if (instances == null) {
                instances = new ConcurrentHashMap<Long, TimerJobInstance>();
                timerInstances.put(sessionId, instances);
            }
        } else {
            instances = singleTimerInstances;
        }
        instances.remove( instance.getJobHandle().getId() );        
    }
    
    
    public Collection<TimerJobInstance> getTimerJobInstances() {
        return singleTimerInstances.values();
    }
    
    public Collection<TimerJobInstance> getTimerJobInstances(Integer sessionId) {
        Map<Long, TimerJobInstance> sessionTimerJobs = timerInstances.get(sessionId);
        if (sessionTimerJobs == null) {
            return new ArrayList<TimerJobInstance>();
        }
        return sessionTimerJobs.values();
    }
    
    public CommandService getCommandService() {
        return this.commandService;
    }
}
