package org.jbpm.persistence.mongodb.timer;

import org.drools.core.command.impl.GenericCommand;
import org.kie.internal.command.Context;

public class MongoJDKCallableJobCommand
    implements
    GenericCommand<Void> {

    private static final long   serialVersionUID = 4L;

    private GlobalMongoTimerJobInstance job;

    public MongoJDKCallableJobCommand(GlobalMongoTimerJobInstance job) {
        this.job = job;
    }

    public Void execute(Context context) {
        try {
            return job.internalCall();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

}
