package org.jbpm.persistence.mongodb.correlation;

import java.util.List;

import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;

public class MongoCorrelationKeyFactory implements CorrelationKeyFactory {

    public CorrelationKey newCorrelationKey(String businessKey) {
        MongoCorrelationKey correlationKey = new MongoCorrelationKey(null, null, businessKey);
        return correlationKey;
    }
    
    public CorrelationKey newCorrelationKey(List<String> properties) {
        MongoCorrelationKey correlationKey = new MongoCorrelationKey();
        for (String businessKey : properties) {
            correlationKey.addProperty(null, businessKey);
        }
        return correlationKey;
    }
}
