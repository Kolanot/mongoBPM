/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.persistence.mongodb;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.drools.core.RuleBase;
import org.drools.core.SessionConfiguration;
import org.drools.core.command.CommandService;
import org.drools.core.command.Interceptor;
import org.drools.core.command.impl.AbstractInterceptor;
import org.drools.core.command.impl.DefaultCommandService;
import org.drools.core.command.impl.FixedKnowledgeCommandContext;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.core.command.runtime.DisposeCommand;
import org.drools.core.common.EndOperationListener;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.time.AcceptsTimerJobFactoryManager;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemManager;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.kie.api.KieBase;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.internal.command.Context;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoSingleSessionCommandService
    implements
    org.drools.core.command.SingleSessionCommandService {

    private static Logger logger = LoggerFactory.getLogger( MongoSingleSessionCommandService.class);

    private KieSession                 ksession;
    private Environment                env;
    private KnowledgeCommandContext    kContext;
    private CommandService             commandService;
    private KieSessionConfiguration    conf;
    private KieBase                    kbase;

    public void checkEnvironment(Environment env) {
        if ( env.get(MongoProcessStore.envKey ) == null) {
            throw new IllegalArgumentException( "Environment must have an MongoProcessStore instance" );
        }
    }

    public MongoSingleSessionCommandService(RuleBase ruleBase,
                                       SessionConfiguration conf,
                                       Environment env) throws ClassNotFoundException, IOException {
        this( new KnowledgeBaseImpl( ruleBase ), conf, env );
    }

    public MongoSingleSessionCommandService(Integer sessionId,
                                       RuleBase ruleBase,
                                       SessionConfiguration conf,
                                       Environment env) {
        this( sessionId, new KnowledgeBaseImpl( ruleBase ), conf, env );
    }

    public MongoSingleSessionCommandService(KieBase kbase,
                                       KieSessionConfiguration conf,
                                       Environment env) throws ClassNotFoundException, IOException {
    	logger.info("enter MongoSingleSessionCommandService");
        if ( conf == null ) {
            conf = new SessionConfiguration();
        }
        this.conf = conf;
        this.env = env;
        this.kbase = kbase;

        checkEnvironment(this.env);

        initNewKnowledgeSession();

        // update the session id to be the same as the session info id
    }

    protected void initNewKnowledgeSession() throws ClassNotFoundException, IOException { 

        // create session but bypass command service
        this.ksession = kbase.newKieSession( this.conf, this.env );

        this.kContext = new FixedKnowledgeCommandContext( null,
                                                          null,
                                                          null,
                                                          this.ksession,
                                                          null );

        this.commandService = new TransactionInterceptor(kContext);

        ((AcceptsTimerJobFactoryManager) ((InternalKnowledgeRuntime) ksession).getTimerService()).getTimerJobFactoryManager().setCommandService( this );

        ((InternalKnowledgeRuntime) this.ksession).setEndOperationListener( new EndOperationListenerImpl( ) );
    }
    
    public MongoSingleSessionCommandService(Integer sessionId,
                                       KieBase kbase,
                                       KieSessionConfiguration conf,
                                       Environment env) {
        if ( conf == null ) {
            conf = new SessionConfiguration();
        }
        
        this.conf = conf;
        this.env = env;
        this.kbase = kbase;
        
        checkEnvironment( this.env );
        
        // update the session id to be the same as the session info id
        //this.sessionId = sessionId;

        try {
            initExistingKnowledgeSession( sessionId);
        } catch ( RuntimeException re ) {
            throw re;
        } catch ( Exception t1 ) {
            throw new RuntimeException( "Wrapped exception see cause", t1 );
        }
    }

    protected void initExistingKnowledgeSession(int sessionId) throws IOException, ClassNotFoundException,
    IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if ( this.ksession != null ) {
            return;
            // nothing to initialize
        }
        // if this.ksession is null, it'll create a new one, else it'll use the existing one

        // The CommandService for the TimerJobFactoryManager must be set before any timer jobs are scheduled. 
        // Otherwise, if overdue jobs are scheduled (and then run before the .commandService field can be set), 
        // they will retrieve a null commandService (instead of a reference to this) and fail.
        ((SessionConfiguration) conf).getTimerJobFactoryManager().setCommandService(this);

        // update the session id to be the same as the session info id
        //((InternalKnowledgeRuntime) ksession).setId( this.sessionInfo.getId() );

        ((InternalKnowledgeRuntime) this.ksession).setEndOperationListener( new EndOperationListenerImpl() );

        if ( this.kContext == null ) {
            // this should only happen when this class is first constructed
            this.kContext = new FixedKnowledgeCommandContext( null,
                                                              null,
                                                              null,
                                                              this.ksession,
                                                              null );
        }

        this.commandService = new TransactionInterceptor(kContext);
    }

	public Context getContext() {
        return this.kContext;
    }

    public synchronized <T> T execute(Command<T> command) {
        return commandService.execute(command);
    }


    public void dispose() {
        if ( ksession != null ) {
        	DisposeCommand cmd = new DisposeCommand();
            execute (cmd);
        }
    }

    @Override
    public void destroy() {
    }

    public KieSession getKieSession() {
        return this.ksession;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptor.setNext( this.commandService );
        this.commandService = interceptor;
    }

	@Override
	public int getSessionId() {
		return this.ksession.getId();
	}

    public static class EndOperationListenerImpl
    implements
    EndOperationListener {
	
	    public EndOperationListenerImpl() {
	    }
	
	    public void endOperation(InternalKnowledgeRuntime kruntime) {
	    }
    }

    private class TransactionInterceptor extends AbstractInterceptor {

        public TransactionInterceptor(Context context) {
            setNext(new DefaultCommandService(context));
        }

        @Override
        public synchronized <T> T execute(Command<T> command) {
            if (command instanceof DisposeCommand) {
                T result = executeNext( (GenericCommand<T>) command );
                return result;
            }

            try {
                T result = null;
                if( command instanceof BatchExecutionCommand ) { 
                    // Batch execution requires the extra logic in 
                    //  StatefulSessionKnowledgeImpl.execute(Context,Command);
                    result = ksession.execute(command);
                } else { 
                    result = executeNext( (GenericCommand<T>) command );
                }
                
                InternalProcessRuntime internalProcessRuntime = (InternalProcessRuntime) ((InternalKnowledgeRuntime) ksession).getProcessRuntime();
                internalProcessRuntime.clearProcessInstances();
                ((MongoWorkItemManager) ksession.getWorkItemManager()).clearWorkItems();
    			
                return result;

            } catch ( RuntimeException re ) {
            	re.printStackTrace();
                throw re;
            } catch ( Exception t1 ) {
            	t1.printStackTrace();
                throw new RuntimeException( "Wrapped exception see cause",
                                            t1 );
            } finally {
                if ( command instanceof DisposeCommand ) {
                    executeNext( (GenericCommand<T>) command );
                }
            }
        }
    }
}
