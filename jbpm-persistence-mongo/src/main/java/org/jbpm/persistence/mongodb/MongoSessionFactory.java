
package org.jbpm.persistence.mongodb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.internal.runtime.manager.SessionFactory;
import org.kie.internal.runtime.manager.SessionNotFoundException;

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
