package org.jbpm.persistence.mongodb.session;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.drools.core.SessionConfiguration;
import org.drools.core.common.AbstractWorkingMemory;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalRuleBase;
import org.drools.core.common.WorkingMemoryFactory;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.marshalling.impl.TimersInputMarshaller;
import org.drools.core.marshalling.impl.ProtobufInputMarshaller.TupleKey;
import org.drools.core.phreak.PhreakTimerNode.Scheduler;
import org.drools.core.reteoo.ReteooRuleBase;
import org.drools.core.spi.FactHandleFactory;
import org.drools.core.spi.GlobalResolver;
import org.drools.core.time.impl.PseudoClockScheduler;
import org.drools.core.time.impl.TimerJobInstance;
import org.jbpm.persistence.mongodb.instance.MongoProcessMarshaller;
import org.jbpm.persistence.mongodb.rule.MongoRuleData;
import org.jbpm.persistence.mongodb.rule.MongoRuleInputMarshaller;
import org.jbpm.persistence.mongodb.rule.MongoRuleOutputMarshaller;
import org.jbpm.persistence.mongodb.session.timer.EmbeddedTimer;
import org.jbpm.persistence.mongodb.session.timer.MongoTimerMarshaller;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class MongoSessionMarshaller {

    private KieBase kbase;
    private Environment                   env;

    /**
     * Exist Info, so load session from here
     */
    public MongoSessionMarshaller( KieBase kbase,
                                     Environment env) {
        this.kbase = kbase;
        this.env = env;
    }

    public MongoSessionInfo getSnapshot(KieSession ksession) throws ClassNotFoundException, IOException {
    	MongoSessionInfo info = new MongoSessionInfo(ksession);
    	
    	syncSnapshot(info);

        return info;
    }

	public void syncSnapshot(MongoSessionInfo info)
			throws ClassNotFoundException, IOException {
		KieSession ksession = info.getSession();
		AbstractWorkingMemory wm = (AbstractWorkingMemory) ((StatefulKnowledgeSessionImpl) ksession).session;
    	info.setId(ksession.getId());
    	info.setPseudoClockTime(ksession.getSessionClock().getCurrentTime());
    	info.setMultithread(false);
    	
    	MongoRuleOutputMarshaller.serialize(info.getRuleData(), wm, kbase, env);
    	
        MongoProcessMarshaller.serialize(info.getProcessdata(), wm);

        writeTimers(info, wm);
	}

    
    public KieSession loadSnapshot(MongoSessionInfo sessionInfo, KieBase kbase, 
    		KieSessionConfiguration conf, Environment env) 
    		throws IOException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        int id = sessionInfo.getId();

        AbstractWorkingMemory session = readSession(id, kbase, conf, env,sessionInfo);
        if ( ((SessionConfiguration) conf).isKeepReference() ) {
            ((ReteooRuleBase) ((KnowledgeBaseImpl) this.kbase).ruleBase).addStatefulSession( session );
        }
        
        // read Process data
        if (sessionInfo.getProcessdata() != null)
        	MongoProcessMarshaller.deserialize(sessionInfo.getProcessdata(), session);
        
        readTimers(sessionInfo, session);
        
        // remove the activations filter
        ((InternalAgenda)session.getAgenda()).setActivationsFilter( null );        
        
        sessionInfo.setSession((StatefulKnowledgeSession) session.getKnowledgeRuntime());
        return sessionInfo.getSession();
    }

    public KieBase getKbase() {
        return kbase;
    }

	private static void writeTimers(MongoSessionInfo sessionInfo, AbstractWorkingMemory wm) {
		sessionInfo.clearTimers();
        Collection<TimerJobInstance> timers = wm.getTimerService().getTimerJobInstances( wm.getId());
		if (!timers.isEmpty()) {
			for (TimerJobInstance timer : timers) {
				EmbeddedTimer et = MongoTimerMarshaller.serialize(timer);
				if (et != null) {
					sessionInfo.getTimers().add(et);
				}
			}
		}
	}
	
	private static AbstractWorkingMemory readSession( int id, KieBase kbase, KieSessionConfiguration conf, Environment env, MongoSessionInfo sessionInfo ) 
			throws IOException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		InternalRuleBase ruleBase = (InternalRuleBase)((KnowledgeBaseImpl) kbase).ruleBase;
		MongoRuleData ruleData = sessionInfo.getRuleData();
        InternalAgenda agenda = MongoRuleInputMarshaller.readAgenda(ruleData, ruleBase);
        
        WorkingMemoryFactory wmFactory = ruleBase.getConfiguration().getComponentFactory().getWorkingMemoryFactory();

        FactHandleFactory handleFactory = MongoRuleInputMarshaller.readFactHandleFactory(ruleData, ruleBase);
        AbstractWorkingMemory session = 
        		( AbstractWorkingMemory ) wmFactory.createWorkingMemory( id,
                                             ruleBase,
                                             handleFactory,
                                             null, // no initialFactHandle, and does not init initial Fact, do it later in deserialize method  
                                             1, // pCTx starts at 1, as InitialFact is 0
                                             (SessionConfiguration) conf,
                                             agenda,
                                             env);
        
        GlobalResolver globalResolver = (GlobalResolver) env.get( EnvironmentName.GLOBALS );
        if ( globalResolver != null ) {
            session.setGlobalResolver( globalResolver );
        }

        if ( session.getTimerService() instanceof PseudoClockScheduler ) {
            PseudoClockScheduler clock = (PseudoClockScheduler) session.getTimerService();
            clock.advanceTime( sessionInfo.getPseudoClockTime(),
                               TimeUnit.MILLISECONDS );
        }

        MongoRuleInputMarshaller.deserialize(ruleData, session);
        new StatefulKnowledgeSessionImpl( session );

        return session;
	}
	
	private static void readTimers(MongoSessionInfo sessionInfo, AbstractWorkingMemory wm) throws ClassNotFoundException {
		for (EmbeddedTimer et : sessionInfo.getTimers()) {
	       MongoTimerMarshaller.deserialize(et, wm, sessionInfo.getRuleData());
		}

		// need to process any eventual left over timer node timers
        if( ! sessionInfo.getRuleData().getMarshallerReaderContext().timerNodeSchedulers.isEmpty() ) {
            for( Map<TupleKey, Scheduler> schedulers : sessionInfo.getRuleData().getMarshallerReaderContext().timerNodeSchedulers.values() ) {
                for( Scheduler scheduler : schedulers.values() ) {
                    scheduler.schedule( scheduler.getTrigger() );
                }
            }
            sessionInfo.getRuleData().getMarshallerReaderContext().timerNodeSchedulers.clear();
        }

	}
}
