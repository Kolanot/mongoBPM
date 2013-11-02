package org.jbpm.persistence.mongodb.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.persistence.mongodb.test.object.TestWorkItemHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentStatefulSessionTest extends AbstractMongoBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(PersistentStatefulSessionTest.class);
    
    @Rule
    public TestName testName = new TestName();
    

    private static String ruleString = ""
        + "package org.drools.test\n"
        + "global java.util.List list\n"
        + "rule rule1\n"
        + "when\n"
        + "  Integer($i : intValue > 0)\n"
        + "then\n"
        + "  list.add( $i );\n"
        + "end\n"
        + "\n";
        
    @Test
    public void testLocalTransactionPerStatement() {
    	createBaseWithResourceStrings(ResourceType.DRL, new String[]{ruleString});
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        List<?> list = new ArrayList<Object>();

        ksession.setGlobal( "list",
                            list );

        ksession.insert( 1 );
        ksession.insert( 2 );
        ksession.insert( 3 );

        ksession.fireAllRules();

        assertEquals( 3,
                      list.size() );

    }

    @Test
    public void testUserTransactions() throws Exception {
    	createBaseWithResourceStrings(ResourceType.DRL, new String[]{ruleString});
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        List<?> list = new ArrayList<Object>();

        // insert and commit
        ksession.setGlobal( "list",
                            list );
        ksession.insert( 1 );
        ksession.insert( 2 );

        // check we rolled back the state changes from the 3rd insert
        ksession.fireAllRules();
        assertEquals( 2,
                      list.size() );

        // insert and commit
        ksession.insert( 3 );
        ksession.insert( 4 );

        ksession.fireAllRules();

        assertEquals( 4,
                      list.size() );
        
        ksession.insert( 7 );
        ksession.insert( 8 );

        ksession.fireAllRules();

        assertEquals( 6,
                      list.size() );
    }

    @Test
    public void testPersistenceWorkItems() {
    	createBaseWithClassPathResources(ResourceType.DRF, new String[]{"rf/WorkItemsProcess.rf"});
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        ProcessInstance processInstance = ksession.startProcess( "org.drools.test.TestProcess" );
        ksession.insert( "TestString" );
        logger.debug( "Started process instance {}", "" + processInstance.getId() + " state:" + processInstance.getState());

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        /*
        assertEquals( origNumObjects + 1,
                      ksession.getObjects().size() );
        for ( Object o : ksession.getObjects() ) {
            logger.debug( o.toString() );
        }
        */
        assertNull( processInstance );

    }
    
    @Test
    public void testPersistenceWorkItems2() throws Exception {
    	createBaseWithClassPathResources(ResourceType.DRF, new String[]{"rf/WorkItemsProcess.rf"});
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        
        ProcessInstance processInstance = ksession.startProcess( "org.drools.test.TestProcess" );
        ksession.insert( "TestString" );
        logger.debug( "Started process instance {}", processInstance.getId() );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );
        
        workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertEquals(1, ksession.getObjects().size() );
        for ( Object o : ksession.getObjects() ) {
            logger.info( "testPersistenceWorkItems2: show ksession object:" + o.toString() );
        }
        assertNull( processInstance );

    }
    
    @Test
    public void testPersistenceWorkItems3() {
    	createBaseWithClassPathResources(ResourceType.DRF, new String[]{"rf/WorkItemsProcess.rf"});
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        ksession.getWorkItemManager().registerWorkItemHandler("MyWork", new SystemOutWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess( "org.drools.test.TestProcess" );
        ksession.insert( "TestString" );
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }
    
    @Test
    public void testPersistenceState() {
    	createBaseWithClassPathResources(ResourceType.DRF, new String[]{"rf/StateProcess.rf"});
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        
        ProcessInstance processInstance = ksession.startProcess( "org.drools.test.TestProcess" );
        long processInstanceId = processInstance.getId();
        logger.debug( "Started process instance {}", processInstanceId );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSessionByProcessInstanceId(processInstanceId);
        ksession.insert(new ArrayList<Object>());
        ksession.fireAllRules();

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNull( processInstance );
    }
    
    @Test
    public void testPersistenceRuleSet() {
    	createBaseWithClassPathResources(ResourceType.DRF, "rf/RuleSetProcess.rf", 
    			ResourceType.DRL, "drl/RuleSetRules.drl");
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        
        ProcessInstance processInstance = ksession.startProcess( "org.drools.test.TestProcess" );
        long processInstanceId = processInstance.getId();
        ksession = reloadKnowledgeSessionByProcessInstanceId(processInstanceId);
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSessionByProcessInstanceId(processInstanceId);
        ksession.insert(new ArrayList<Object>());
        ksession.fireAllRules();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNull( processInstance );
    }
    
    @Test
    public void testPersistenceEvents() {
    	createBaseWithClassPathResources(ResourceType.DRF, new String[]{ "rf/EventsProcess.rf"});
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        
        ProcessInstance processInstance = ksession.startProcess( "org.drools.test.TestProcess" );
        logger.debug( "Started process instance {}", processInstance.getId() );
        long processInstanceId = processInstance.getId();

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession = reloadKnowledgeSessionByProcessInstanceId(processInstanceId);
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSessionByProcessInstanceId(processInstanceId);
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );
        
        ksession = reloadKnowledgeSessionByProcessInstanceId(processInstanceId);
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession.signalEvent("MyEvent1", null, processInstance.getId());

        ksession = reloadKnowledgeSessionByProcessInstanceId(processInstanceId);
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession.signalEvent("MyEvent2", null, processInstance.getId());
        
        ksession = reloadKnowledgeSessionByProcessInstanceId(processInstanceId);
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNull( processInstance );
    }
    
    @Test
    public void testProcessListener() {
    	createBaseWithClassPathResources(ResourceType.DRF, new String[]{ "rf/WorkItemsProcess.rf"});
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        final List<ProcessEvent> events = new ArrayList<ProcessEvent>();
        ProcessEventListener listener = new ProcessEventListener() {
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                logger.debug("After node left: {}", event.getNodeInstance().getNodeName());
                events.add(event);              
            }
            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                logger.debug("After node triggered: {}", event.getNodeInstance().getNodeName());
                events.add(event);              
            }
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                logger.debug("After process completed");
                events.add(event);              
            }
            public void afterProcessStarted(ProcessStartedEvent event) {
                logger.debug("After process started");
                events.add(event);              
            }
            public void beforeNodeLeft(ProcessNodeLeftEvent event) {
                logger.debug("Before node left: {}", event.getNodeInstance().getNodeName());
                events.add(event);              
            }
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                logger.debug("Before node triggered: {}", event.getNodeInstance().getNodeName());
                events.add(event);              
            }
            public void beforeProcessCompleted(ProcessCompletedEvent event) {
                logger.debug("Before process completed");
                events.add(event);              
            }
            public void beforeProcessStarted(ProcessStartedEvent event) {
                logger.debug("Before process started");
                events.add(event);              
            }
            public void afterVariableChanged(ProcessVariableChangedEvent event) {
                logger.debug("After Variable Changed");
                events.add(event);  
            }
            public void beforeVariableChanged(ProcessVariableChangedEvent event) {
                logger.debug("Before Variable Changed");
                events.add(event); 
            }
        };
        ksession.addEventListener(listener);
        
        ProcessInstance processInstance = ksession.startProcess( "org.drools.test.TestProcess" );
        logger.debug( "Started process instance {}", processInstance.getId() );
        
        assertEquals(12, events.size());
        assertTrue(events.get(0) instanceof ProcessStartedEvent);
        assertTrue(events.get(1) instanceof ProcessNodeTriggeredEvent);
        assertTrue(events.get(2) instanceof ProcessNodeLeftEvent);
        assertTrue(events.get(3) instanceof ProcessNodeTriggeredEvent);
        assertTrue(events.get(4) instanceof ProcessNodeLeftEvent);
        assertTrue(events.get(5) instanceof ProcessNodeTriggeredEvent);
        assertTrue(events.get(6) instanceof ProcessNodeTriggeredEvent);
        assertTrue(events.get(7) instanceof ProcessNodeLeftEvent);
        assertTrue(events.get(8) instanceof ProcessNodeTriggeredEvent);
        assertTrue(events.get(9) instanceof ProcessNodeLeftEvent);
        assertTrue(events.get(10) instanceof ProcessNodeTriggeredEvent);
        assertTrue(events.get(11) instanceof ProcessStartedEvent);
        
        ksession.removeEventListener(listener);
        events.clear();
        
        processInstance = ksession.startProcess( "org.drools.test.TestProcess" );
        logger.debug( "Started process instance {}", processInstance.getId() );
        
        assertTrue(events.isEmpty());
    }

    @Test
    public void testPersistenceSubProcess() {
    	createBaseWithClassPathResources(ResourceType.DRF, new String[]{"rf/SuperProcess.rf", "rf/SubProcess.rf"});
        StatefulKnowledgeSession ksession = createKnowledgeSession();
        
        ProcessInstance processInstance = ksession.startProcess( "com.sample.SuperProcess" );
        logger.debug( "Started process instance {}", processInstance.getId() );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(),
                                                       null );

        workItem = handler.getWorkItem();
        assertNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNull( "Process did not complete.", processInstance );
    }
    
    @Test
    public void testPersistenceVariables() {
    	createBaseWithClassPathResources(ResourceType.DRF, new String[]{ "rf/VariablesProcess.rf"});
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "John Doe");
        ProcessInstance processInstance = ksession.startProcess( "org.drools.test.TestProcess", parameters );
        long processInstanceId = processInstance.getId();
        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        assertEquals( "John Doe", workItem.getParameter("name"));

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstanceId );
        assertNotNull( processInstance );

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        assertEquals( "John Doe", workItem.getParameter("text"));
        
        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNotNull( processInstance );
        RuleFlowProcessInstance ruleFlow = (RuleFlowProcessInstance)processInstance;
        assertEquals(ruleFlow.getNodeInstances().size(), 1);
        NodeInstance nodeInstance = ruleFlow.getNodeInstances().iterator().next();
        assertTrue(nodeInstance instanceof CompositeContextNodeInstance);
        assertFalse(((org.jbpm.workflow.instance.node.CompositeContextNodeInstance)nodeInstance).getNodeInstances().isEmpty());
        NodeInstance workItemNodeInstance = ((org.jbpm.workflow.instance.node.CompositeContextNodeInstance)nodeInstance).getNodeInstances().iterator().next();
        assertTrue(workItemNodeInstance instanceof WorkItemNodeInstance);

        ksession = reloadKnowledgeSession();
        ksession.getWorkItemManager().completeWorkItem( workItem.getId(), null );

        workItem = handler.getWorkItem();
        assertNull( workItem );

        ksession = reloadKnowledgeSession();
        processInstance = ksession.getProcessInstance( processInstance.getId() );
        assertNull( processInstance );
    }

    @Test
    public void testSetFocus() {
        String str = "";
        str += "package org.drools.test\n";
        str += "global java.util.List list\n";
        str += "rule rule1\n";
        str += "agenda-group \"badfocus\"";
        str += "when\n";
        str += "  Integer(intValue > 0)\n";
        str += "then\n";
        str += "  list.add( 1 );\n";
        str += "end\n";
        str += "\n";

    	createBaseWithResourceStrings(ResourceType.DRL, new String[]{ str});
        StatefulKnowledgeSession ksession = createKnowledgeSession();

        List<?> list = new ArrayList<Object>();

        ksession.setGlobal( "list",
                            list );

        ksession.insert( 1 );
        ksession.insert( 2 );
        ksession.insert( 3 );
        ksession.getAgenda().getAgendaGroup("badfocus").setFocus();

        ksession.fireAllRules();

        assertEquals( 3,
                      list.size() );
    }

}
