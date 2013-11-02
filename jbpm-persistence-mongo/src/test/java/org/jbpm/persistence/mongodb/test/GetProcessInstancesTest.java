package org.jbpm.persistence.mongodb.test;

import java.util.List;

import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 * This test looks at the behavior of the  {@link JPAProcessInstanceManager} 
 * with regards to created (but not started) process instances 
 * and whether the process instances are available or not after creation.
 */
public class GetProcessInstancesTest extends AbstractMongoBaseTest {
    @Override
	protected void populateClassPathProcessResourceList(List<String> list) {
		list.add("processinstance/HelloWorld.rf");
	}

	
	@Test
    public void getEmptyProcessInstances() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        assertEquals(0, ksession.getProcessInstances().size());
    }
	
	@Test
    public void create1ProcessInstance() throws Exception {
        long processId;

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        processId = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        ksession.dispose();
        ksession = createKnowledgeSession();
        ProcessInstance instance = ksession.getProcessInstance(processId);
        assertNull("Process instance " + processId + " should not exist in this new session!", instance);
    }

	@Test
    public void create2ProcessInstances() throws Exception {
        long[] processId = new long[2];

        StatefulKnowledgeSession ksession = reloadKnowledgeSession();
        processId[0] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        processId[1] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        ksession.dispose();

        assertProcessInstancesExist(processId);
    }

    @Test
    public void create2ProcessInstancesAndReload() throws Exception {
        long[] processId = new long[2];

        
        StatefulKnowledgeSession ksession = reloadKnowledgeSession();
        processId[0] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        processId[1] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        assertEquals(2, ksession.getProcessInstances().size());
        
        ksession = reloadKnowledgeSession(ksession);
        assertEquals(2, ksession.getProcessInstances().size());
        assertProcessInstancesExist(processId);
    }

    @Test
    public void createAndAbort2ProcessInstances() throws Exception {
        long[] processId = new long[2];

        
        StatefulKnowledgeSession ksession = reloadKnowledgeSession();
        processId[0] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        processId[1] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        assertEquals(2, ksession.getProcessInstances().size());
        
        ksession = reloadKnowledgeSession(ksession);
        assertEquals(2, ksession.getProcessInstances().size());
        assertProcessInstancesExist(processId);
        ksession.abortProcessInstance(processId[0]);
        ksession.abortProcessInstance(processId[1]);
        assertProcessInstancesNotExist(processId);
    }

    private void assertProcessInstancesExist(long[] processId) {
        StatefulKnowledgeSession ksession = reloadKnowledgeSession();

        for (long id : processId) {
            assertNotNull("Process instance " + id + " should not exist!", ksession.getProcessInstance(id));
        }
    }

    private void assertProcessInstancesNotExist(long[] processId) {
        StatefulKnowledgeSession ksession = reloadKnowledgeSession();

        for (long id : processId) {
            assertNull(ksession.getProcessInstance(id));
        }
    }
}
