package org.jbpm.persistence.mongodb.rule;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.spi.Activation;

public class MongoActivationKey implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String pkgName;
    private final String ruleName;
    private final List<Integer>  tuple;

    public MongoActivationKey(Activation activation) {
    	this.pkgName = activation.getRule().getPackageName();
    	this.ruleName = activation.getRule().getName();
    	this.tuple = new ArrayList<Integer>(); 
    	EmbeddedActivation.populateFactTupleHandleList(activation.getTuple(), this.tuple);
    }

    public MongoActivationKey(EmbeddedActivation ea) {
    	this.pkgName = ea.getRulePackage();
    	this.ruleName = ea.getRuleName();
    	this.tuple = ea.getTupleFactHandleList(); 
    }

    public MongoActivationKey(String pkgName, String ruleName, List<Integer> tupleFacthandleList) {
    	this.pkgName = pkgName;
    	this.ruleName = ruleName;
    	this.tuple = tupleFacthandleList;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pkgName == null) ? 0 : pkgName.hashCode());
        result = prime * result + ((ruleName == null) ? 0 : ruleName.hashCode());
        result = prime * result +  tuple.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        MongoActivationKey other = (MongoActivationKey) obj;
        if ( pkgName == null ) {
            if ( other.pkgName != null ) return false;
        } else if ( !pkgName.equals( other.pkgName ) ) return false;
        if ( ruleName == null ) {
            if ( other.ruleName != null ) return false;
        } else if ( !ruleName.equals( other.ruleName ) ) return false;
        if ( tuple.equals(other.tuple ) ) return false;
        return true;
    }

}
