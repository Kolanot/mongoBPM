package org.jbpm.persistence.mongodb.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ParameterMappingTest extends AbstractMongoBaseTest {
    
    private static final String PROCESS_ID = "org.jbpm.processinstance.subprocess";
    private static final String SUBPROCESS_ID = "org.jbpm.processinstance.helloworld";

	protected void populateClassPathProcessResourceList(List<String> list) {
        list.add("processinstance/Subprocess.rf");
        list.add("processinstance/HelloWorld.rf");
    }

    //org.jbpm.processinstance.subprocess
    @Test
    public void testChangingVariableByScript() throws Exception {
    	createKnowledgeSession();
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "script");
        mapping.put("var", "value");

        getKSession().startProcess(PROCESS_ID, mapping);
        ProcessListener listener = getListener();
        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    @Test
    public void testChangingVariableByEvent() throws Exception {
    	createKnowledgeSession();
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "event");
        mapping.put("var", 1);

        getKSession().startProcess(PROCESS_ID, mapping).getId();
        getKSession().signalEvent("pass", "2");

        ProcessListener listener = getListener();
        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    @Test
    public void testChangingVariableByEventSignalWithProcessId() throws Exception {
    	createKnowledgeSession();
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "event");
        mapping.put("var", "value");

        long processId = getKSession().startProcess(PROCESS_ID, mapping).getId();
        getKSession().signalEvent("pass", "new value", processId);

        ProcessListener listener = getListener();
        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    @Test
    public void testNotChangingVariable() throws Exception {
    	createKnowledgeSession();
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "default");
        mapping.put("var", "value");

        getKSession().startProcess(PROCESS_ID, mapping);

        ProcessListener listener = getListener();
        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }

    @Test
    public void testNotSettingVariable() throws Exception {
    	createKnowledgeSession();
        Map<String, Object> mapping = new HashMap<String, Object>();
        mapping.put("type", "default");

        getKSession().startProcess(PROCESS_ID, mapping);

        ProcessListener listener = getListener();
        assertTrue(listener.isProcessStarted(PROCESS_ID));
        assertTrue(listener.isProcessStarted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(SUBPROCESS_ID));
        assertTrue(listener.isProcessCompleted(PROCESS_ID));
    }
}
