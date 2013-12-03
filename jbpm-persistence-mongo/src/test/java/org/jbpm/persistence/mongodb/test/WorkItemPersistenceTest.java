package org.jbpm.persistence.mongodb.test;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.datatype.impl.type.IntegerDataType;
import org.drools.core.process.core.datatype.impl.type.ObjectDataType;
import org.drools.core.process.core.datatype.impl.type.StringDataType;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.drools.core.reteoo.ReteooRuleBase;
import org.drools.core.runtime.process.ProcessRuntimeFactory;
import org.jbpm.persistence.mongodb.MongoSessionStore;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.session.MongoSessionInfo;
import org.jbpm.persistence.mongodb.test.object.Person;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.instance.ProcessRuntimeFactoryServiceImpl;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.StartNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WorkItemPersistenceTest extends AbstractMongoBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkItemPersistenceTest.class);
    
    protected void populateProcessResourceList(List<String> processResources) {}
    static {
        ProcessRuntimeFactory.setProcessRuntimeFactoryService(new ProcessRuntimeFactoryServiceImpl());
    }
    
    @Test
    @Ignore
    public void testCancelNonRegisteredWorkItemHandler() {
        String processId = "org.drools.actions";
        String workName = "Unnexistent Task";
        RuleFlowProcess process = getWorkItemProcess( processId, workName );
        ((ReteooRuleBase) ((InternalKnowledgeBase) getKBase()).getRuleBase()).addProcess( process );
        StatefulKnowledgeSession ksession = reloadKnowledgeSession();

        ksession.getWorkItemManager().registerWorkItemHandler( workName, new DoNothingWorkItemHandler() );

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put( "UserName", "John Doe" );
        parameters.put( "Person",
                        new Person( new Long(1), "John Doe" ) );

        ProcessInstance processInstance = ksession.startProcess( "org.drools.actions",
                                                                  parameters );
        long processInstanceId = processInstance.getId();
        Assert.assertEquals( ProcessInstance.STATE_ACTIVE,
                           processInstance.getState() );
        ksession.getWorkItemManager().registerWorkItemHandler( workName,
                                                               null );

        try {
            ksession.abortProcessInstance( processInstanceId );
            Assert.fail( "should fail if WorkItemHandler for" + workName + "is not registered" );
        } catch ( WorkItemHandlerNotFoundException wihnfe ) {

        }

        Assert.assertEquals( ProcessInstance.STATE_ABORTED, processInstance.getState() );
    }

    private RuleFlowProcess getWorkItemProcess(String processId, String workName) {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( processId );

        List<Variable> variables = new ArrayList<Variable>();
        Variable variable = new Variable();
        variable.setName( "UserName" );
        variable.setType( new StringDataType() );
        variables.add( variable );
        
        variable = new Variable();
        variable.setName( "MyObject" );
        variable.setType( new ObjectDataType() );
        variables.add( variable );
        variable = new Variable();
        variable.setName( "Number" );
        variable.setType( new IntegerDataType() );
        variables.add( variable );
        process.getVariableScope().setVariables( variables );

        StartNode startNode = new StartNode();
        startNode.setName( "Start" );
        startNode.setId( 1 );

        HumanTaskNode workItemNode = new HumanTaskNode();
        workItemNode.setName( "workItemNode" );
        workItemNode.setId( 2 );
        workItemNode.addInMapping( "Attachment", "MyObject" );
        workItemNode.addOutMapping( "Result", "MyObject" );
        workItemNode.addOutMapping( "Result.length()", "Number" );
        
        Work work = new WorkImpl();
        work.setName( workName );
        
        Set<ParameterDefinition> parameterDefinitions = new HashSet<ParameterDefinition>();
        ParameterDefinition parameterDefinition = new ParameterDefinitionImpl( "ActorId", new StringDataType() );
        parameterDefinitions.add( parameterDefinition );
        parameterDefinition = new ParameterDefinitionImpl( "Content", new StringDataType() );
        parameterDefinitions.add( parameterDefinition );
        parameterDefinition = new ParameterDefinitionImpl( "Comment", new StringDataType() );
        parameterDefinitions.add( parameterDefinition );
        work.setParameterDefinitions( parameterDefinitions );
        
        work.setParameter( "ActorId", "#{UserName}" );
        work.setParameter( "Content", "#{Person.name}" );
        workItemNode.setWork( work );

        EndNode endNode = new EndNode();
        endNode.setName( "End" );
        endNode.setId( 3 );

        connect( startNode, workItemNode );
        connect( workItemNode, endNode );

        process.addNode( startNode );
        process.addNode( workItemNode );
        process.addNode( endNode );

        return process;
    }

    private void connect(Node sourceNode,
                         Node targetNode) {
        new ConnectionImpl( sourceNode,
                             Node.CONNECTION_DEFAULT_TYPE,
                             targetNode,
                             Node.CONNECTION_DEFAULT_TYPE );
    }

    protected void populateReaderProcessResourceList(List<Reader> processResources) {
        Reader source = new StringReader(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
                "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
                "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.humantask\" package-name=\"org.drools\" version=\"1\" >\n" +
                "\n" +
                "  <header>\n" +
                "  </header>\n" +
                "\n" +
                "  <nodes>\n" +
                "    <start id=\"1\" name=\"Start\" />\n" +
                "    <humanTask id=\"2\" name=\"HumanTask\" >\n" +
                "      <work name=\"Human Task\" >\n" +
                "        <parameter name=\"ActorId\" >\n" +
                "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n" +
                "          <value>John Doe</value>\n" +
                "        </parameter>\n" +
                "        <parameter name=\"TaskName\" >\n" +
                "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n" +
                "          <value>Do something</value>\n" +
                "        </parameter>\n" +
                "        <parameter name=\"Priority\" >\n" +
                "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n" +
                "        </parameter>\n" +
                "        <parameter name=\"Comment\" >\n" +
                "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n" +
                "        </parameter>\n" +
                "      </work>\n" +
                "    </humanTask>\n" +
                "    <end id=\"3\" name=\"End\" />\n" +
                "  </nodes>\n" +
                "\n" +
                "  <connections>\n" +
                "    <connection from=\"1\" to=\"2\" />\n" +
                "    <connection from=\"2\" to=\"3\" />\n" +
                "  </connections>\n" +
                "\n" +
                "</process>");
        processResources.add(source);
    }
    @Test
    public void testHumanTask() {
        StatefulKnowledgeSession ksession = createKnowledgeSession();
    	int numProcInsts = ksession.getProcessInstances().size();
    	createBase(); 
        ksession = reloadKnowledgeSession(ksession);
        
        DoNothingWorkItemHandler handler = new DoNothingWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        
        ProcessInstance processInstance = ksession.startProcess("org.drools.humantask");
        
        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
        
        int state = processInstance.getState();
        switch(state) { 
        case ProcessInstance.STATE_ABORTED:
            logger.debug("STATE_ABORTED");
            break;
        case ProcessInstance.STATE_ACTIVE:
            logger.debug("STATE_ACTIVE");
            break;
        case ProcessInstance.STATE_COMPLETED:
            logger.debug("STATE_COMPLETED");
            break;
        case ProcessInstance.STATE_PENDING:
            logger.debug("STATE_PENDING");
            break;
        case ProcessInstance.STATE_SUSPENDED:
            logger.debug("STATE_SUSPENDED");
            break;
        default: 
            logger.debug("Unknown state: {}", state );
        }
       
        int newNumProcInsts = ksession.getProcessInstances().size();
        assertTrue( (newNumProcInsts - numProcInsts) == 1);
        
        MongoSessionStore store = (MongoSessionStore)getEnv().get(MongoSessionStore.envKey);
        long processInstanceId = processInstance.getId();
        MongoSessionInfo sessionInfo = store.findSessionInfoByProcessInstanceId(processInstanceId);
        assertNotNull(sessionInfo);
        assertEquals("two session must be equal", new Long(ksession.getId()), new Long(sessionInfo.getId()));
        MongoProcessInstanceInfo processInstanceInfoMadeInThisTest = sessionInfo.getProcessdata().getProcessInstance(processInstanceId);
        long processInstanceId2 = processInstanceInfoMadeInThisTest.getProcessInstanceId();
        ProcessInstance procInstLoadFromMongo = ksession.getProcessInstance(processInstanceId2);
        assertNotNull("ProcessInstance of ProcessInstanceInfo from this test is not filled and null!", 
        		procInstLoadFromMongo);
        assertEquals(procInstLoadFromMongo, processInstance); 
    }
}
