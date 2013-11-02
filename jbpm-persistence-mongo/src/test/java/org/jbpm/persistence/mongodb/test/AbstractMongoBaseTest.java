package org.jbpm.persistence.mongodb.test;

import static org.kie.api.runtime.EnvironmentName.GLOBALS;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.core.base.MapGlobalResolver;
import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.persistence.mongodb.MongoKnowledgeService;
import org.jbpm.persistence.mongodb.MongoSessionStore;
import org.jbpm.test.AbstractBaseTest;
import org.junit.After;
import org.junit.Before;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 * This test looks at the behavior of the  {@link MongoProcessInstanceManager} 
 * with regards to created (but not started) process instances 
 * and whether the process instances are available or not after creation.
 */
public abstract class AbstractMongoBaseTest extends AbstractBaseTest {
    
    private Environment env;
    private KieBase kbase;
    private KieSessionConfiguration conf = null;
    private int sessionId;
    private StatefulKnowledgeSession ksession;
    private ProcessListener listener;
    private MongoSessionStore ds = new MongoSessionStore();

    @Before
    public void before() throws Exception {
        env = EnvironmentFactory.newEnvironment();
        env.set( GLOBALS, new MapGlobalResolver() );
        env.set(MongoSessionStore.envKey, ds);
        ds.removeSessions();
        createBase();
        //createNewSession();
    }

	private void createNewSession() {
		ksession = MongoKnowledgeService.newStatefulKnowledgeSession(kbase, conf, env);
        assertTrue("Valid KnowledgeSession could not be created.", ksession != null);

        listener = new ProcessListener();
        ksession.addEventListener(listener);
        sessionId = ksession.getId();
        System.out.println("AbstractMongoBaseTest.createNewSession, ksessionid = " + ksession.getId());
	}

    @After
    public void tearDown() throws Exception {
    	disposeKnowledgeSession();
    }

    protected Environment getEnv() {
    	return env;
    }

    protected KieBase getKBase() {
    	return kbase;
    }

    protected KieSessionConfiguration getConfig() {
    	return conf;
    }

    protected StatefulKnowledgeSession getKSession() {
    	return ksession;
    }
    
    protected ProcessListener getListener() {
    	return listener;
    }
    /**
     * Helper functions
     */
    
    protected void createBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        addClassPathProcessResources(kbuilder);
        addReaderProcessResources(kbuilder);
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());
        this.kbase = kbuilder.newKnowledgeBase();
    }

    
    protected void createBaseWithClassPathResources(ResourceType rt, String... classPathProcessResources) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (int i = 0; i < classPathProcessResources.length; ++i) {
            kbuilder.add(ResourceFactory.newClassPathResource(classPathProcessResources[i]), rt);
        }
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());

        this.kbase = kbuilder.newKnowledgeBase();
    }

    protected void createBaseWithClassPathResources(ResourceType rt1, String classPathProcessResource1, 
    		ResourceType rt2, String classPathProcessResource2) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(classPathProcessResource1), rt1);
        kbuilder.add(ResourceFactory.newClassPathResource(classPathProcessResource2), rt2);
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());
        
        this.kbase = kbuilder.newKnowledgeBase();
    }

    protected void createBaseWithResourceStrings(ResourceType rt, String... stringProcessResources) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        for (int i = 0; i < stringProcessResources.length; ++i) {
        	kbuilder.add( ResourceFactory.newByteArrayResource( stringProcessResources[i].getBytes() ), rt );
        }
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());
        
        this.kbase = kbuilder.newKnowledgeBase();
    }

    private void addClassPathProcessResources(KnowledgeBuilder kbuilder) {
        List<String> classPathProcessResources = new ArrayList<String>();
		populateClassPathProcessResourceList(classPathProcessResources);
        for (Iterator<String> itr = classPathProcessResources.iterator(); itr.hasNext();) {
        	String procResource = itr.next();
            kbuilder.add(ResourceFactory.newClassPathResource(procResource), ResourceType.DRF);
        }
	}
    
	private void addReaderProcessResources(KnowledgeBuilder kbuilder) {
        List<Reader> readerProcessResources = new ArrayList<Reader>();
		populateReaderProcessResourceList(readerProcessResources);
        for (Iterator<Reader> itr = readerProcessResources.iterator(); itr.hasNext();) {
        	Reader procResource = itr.next();
            kbuilder.add(ResourceFactory.newReaderResource(procResource), ResourceType.DRF);
        }
	}
    
    protected void populateReaderProcessResourceList(List<Reader> processResources) {}

    protected void populateClassPathProcessResourceList(List<String> processResources) {}

    protected void setConfig(KieSessionConfiguration conf) {
    	this.conf = conf;
    }
    protected StatefulKnowledgeSession createKnowledgeSession() {
    	createNewSession();
        return ksession;
    }
    
    protected StatefulKnowledgeSession reloadKnowledgeSession() {
    	if (sessionId == 0)
    		return createKnowledgeSession();
        ksession =  MongoKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, conf, env);
        return ksession;
    }

    protected void disposeKnowledgeSession() {
    	if (ksession != null)
    		ksession.dispose();
    }
    
    protected StatefulKnowledgeSession reloadKnowledgeSessionByProcessInstanceId(long procInstId) {
        ksession =  MongoKnowledgeService.loadStatefulKnowledgeSessionByProcessInstanceId(procInstId, kbase, conf, env);
        sessionId = ksession.getId();
        return ksession;
    }

    protected void saveSession() {
    	MongoKnowledgeService.saveStatefulKnowledgeSession(ksession);
    }
    /*
    protected StatefulKnowledgeSession reloadKnowledgeSession(long processInstanceId) {
        ksession =  MongoKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        // load process instance
        ksession.getProcessInstance(processInstanceId);
        return ksession;
    }
*/
    protected StatefulKnowledgeSession reloadKnowledgeSession(StatefulKnowledgeSession ksession) {
        ksession.dispose();
        return reloadKnowledgeSession();
    }

    public static class ProcessListener extends DefaultProcessEventListener {
        private final List<String> processesStarted = new ArrayList<String>();
        private final List<String> processesCompleted = new ArrayList<String>();

        public void afterProcessStarted(ProcessStartedEvent event) {
            processesStarted.add(event.getProcessInstance().getProcessId());
        }

        public void afterProcessCompleted(ProcessCompletedEvent event) {
            processesCompleted.add(event.getProcessInstance().getProcessId());
        }

        public boolean isProcessStarted(String processId) {
            return processesStarted.contains(processId);
        }

        public boolean isProcessCompleted(String processId) {
            return processesCompleted.contains(processId);
        }
    }
}