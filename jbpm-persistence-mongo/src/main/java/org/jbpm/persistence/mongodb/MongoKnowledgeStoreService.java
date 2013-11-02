package org.jbpm.persistence.mongodb;

import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;

public interface MongoKnowledgeStoreService {
    KieSession newKieSession(KieBase kbase,
            KieSessionConfiguration configuration,
            Environment environment);

    KieSession loadKieSession(int id,
             KieBase kbase,
             KieSessionConfiguration configuration,
             Environment environment);
    
    void saveKieSession(KieSession session);
}
