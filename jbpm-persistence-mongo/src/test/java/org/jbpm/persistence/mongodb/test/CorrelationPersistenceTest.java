package org.jbpm.persistence.mongodb.test;

import java.util.Arrays;
import java.util.List;

import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.junit.Before;
import org.junit.Test;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationAwareProcessRuntime;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.utils.ServiceRegistryImpl;
import org.kie.api.runtime.process.ProcessInstance;

public class CorrelationPersistenceTest extends AbstractMongoBPMBaseTest {
    
    private static final String HelloWorld = "org.jbpm.processinstance.helloworld";
    private static final String SubProcess = "org.jbpm.processinstance.subprocess";
    private static final String EventProcess = "org.drools.test.TestProcess";
    private CorrelationKeyFactory factory;
    
	protected void populateClassPathProcessResourceList(List<String> list) {
		list.add("processinstance/HelloWorld.rf");
		list.add("processinstance/Subprocess.rf");
		list.add("rf/EventsProcess.rf");
	}

    @Before
    public void before() throws Exception {
    	super.before();
    	ServiceRegistryImpl.getInstance().addDefault(CorrelationKeyFactory.class, "org.jbpm.persistence.mongodb.correlation.MongoCorrelationKeyFactory");
        factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
    }

    @Test 
    public void testCorrelationKey() {
        CorrelationKey key = factory.newCorrelationKey(Arrays.asList(new String[] {"test123", "test234"}));
        CorrelationKey key2 = factory.newCorrelationKey(Arrays.asList(new String[] {"test123", "test234"}));
        assertEquals(key, key2);
        MongoCorrelationKey mongoKey = new MongoCorrelationKey(key);
        MongoCorrelationKey mongoKey2 = new MongoCorrelationKey(key2);
        assertEquals(mongoKey, mongoKey2);
        assertEquals(mongoKey.hashCode(), mongoKey2.hashCode());
    }
    
    @Test
    public void testCreateCorrelation() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        CorrelationKey key = factory.newCorrelationKey("test123");
        ProcessInstance processInstance = ((CorrelationAwareProcessRuntime)ksession).startProcess(HelloWorld, key, null);
        long processInstanceId = processInstance.getId();
        
        ksession = reloadKnowledgeSession();
        assertEquals(0, ksession.getProcessInstances().size());
        
        ProcessInstance processInstance1 = ksession.getProcessInstance(processInstanceId);
        
        ProcessInstance processInstance2 = ((CorrelationAwareProcessRuntime)ksession).getProcessInstance(key);
        
        assertEquals(processInstance1, processInstance2);
    }
    
    @Test
    public void testCreateCorrelationAndSignal() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        CorrelationKey key = factory.newCorrelationKey(Arrays.asList(new String[] {"test123", "test234"}));
        ProcessInstance processInstance = ((CorrelationAwareProcessRuntime)ksession).startProcess(EventProcess, key, null);
        long processInstanceId = processInstance.getId();
        
        ksession = reloadKnowledgeSession();
        assertEquals(1, ksession.getProcessInstances().size());
        
        ProcessInstance processInstance1 = ksession.getProcessInstance(processInstanceId);
        
        ProcessInstance processInstance2 = ((CorrelationAwareProcessRuntime)ksession).getProcessInstance(key);
        
        assertEquals(processInstance1.getId(), processInstance2.getId());
    }
    
    @Test
    public void testCreateCorrelationMultiValueDoesMatch() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        CorrelationKey key = factory.newCorrelationKey(Arrays.asList(new String[] {"test123", "test234"}));
        ProcessInstance processInstance = ((CorrelationAwareProcessRuntime)ksession).startProcess(HelloWorld, key, null);
        long processInstanceId = processInstance.getId();
        
        ksession = reloadKnowledgeSession();
        assertEquals(0, ksession.getProcessInstances().size());
        
        ProcessInstance processInstance1 = ksession.getProcessInstance(processInstanceId);
        
        CorrelationKey key2 = factory.newCorrelationKey(Arrays.asList(new String[] {"test123", "test234"}));
        assertEquals(key, key2);
        ProcessInstance processInstance2 = ((CorrelationAwareProcessRuntime)ksession).getProcessInstance(key2);
        
        assertEquals(processInstance1, processInstance2);
    }
}
