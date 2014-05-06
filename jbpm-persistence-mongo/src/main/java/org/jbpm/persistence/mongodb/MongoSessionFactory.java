/*
 * Copyright 2013 JBoss Inc
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
package org.jbpm.persistence.mongodb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.SessionFactory;
import org.kie.internal.runtime.manager.SessionNotFoundException;

/**
 * SessionFactory implementation backed with in memory store of used sessions. Does not preserve the state
 * between server restarts or even <code>RuntimeManager</code> close. For more permanent store 
 * <code>JPASessionFactory</code> should be used
 *
 * @see JPASessionFactory
 */
public class MongoSessionFactory implements SessionFactory {

    private RuntimeEnvironment environment;
    // TODO all sessions stored here should be proxied so it can be removed on dispose/destroy
    private Map<Integer, KieSession> sessions = new ConcurrentHashMap<Integer, KieSession>();
    
    public MongoSessionFactory(RuntimeEnvironment environment) {
        this.environment = environment;
    }
    
    @Override
    public KieSession newKieSession() {
        KieSession ksession = MongoKnowledgeService.newStatefulKnowledgeSession(environment.getKieBase(), environment.getConfiguration(), environment.getEnvironment());
        this.sessions.put(ksession.getId(), ksession);
        return ksession;
    }

    @Override
    public KieSession findKieSessionById(Integer sessionId) {
        if (sessions.containsKey(sessionId)) {
            return sessions.get(sessionId);
        } else {
            throw new SessionNotFoundException("Session with id " + sessionId + " was not found");
        }
    }

    @Override
    public void close() {
        sessions.clear();
    }

}
