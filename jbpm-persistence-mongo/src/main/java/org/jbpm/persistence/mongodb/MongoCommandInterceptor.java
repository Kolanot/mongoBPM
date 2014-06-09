package org.jbpm.persistence.mongodb;

import org.drools.core.command.impl.AbstractInterceptor;
import org.drools.core.command.impl.DefaultCommandService;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.DisposeCommand;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.persistence.mongodb.task.service.MongoTaskPersistenceContext;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemManager;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.services.task.commands.TaskContext;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.Context;

public class MongoCommandInterceptor extends AbstractInterceptor {
    private KieSession ksession;
    private Environment env;
    private Context context;

    public MongoCommandInterceptor(Context context, KieSession ksession, Environment env) {
        setNext(new DefaultCommandService(context));
        this.context = context;
        this.ksession = ksession;
        this.env = env;
    }
    
    public MongoCommandInterceptor(Environment env) {
       	context = new TaskContext();
    	((TaskContext)context).setPersistenceContext(new MongoTaskPersistenceContext(env));
        this.env = env;
    }
    
    @Override
    public Context getContext() {
    	return context;
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
                if (ksession != null) {
                	result = ksession.execute(command);
                } else {
                	throw new UnsupportedOperationException("Null Kesssion, cannot run BatchExcutionCommand");
                }
            } else { 
                result = executeNext( (GenericCommand<T>) command );
            }
            if (ksession != null) {
	            MongoProcessStore store = (MongoProcessStore)env.get(MongoProcessStore.envKey);
	            store.commit();
	            InternalProcessRuntime internalProcessRuntime = (InternalProcessRuntime) ((InternalKnowledgeRuntime) ksession).getProcessRuntime();
	            internalProcessRuntime.clearProcessInstances();
	            ((MongoWorkItemManager) ksession.getWorkItemManager()).clearWorkItems();
            }    
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
