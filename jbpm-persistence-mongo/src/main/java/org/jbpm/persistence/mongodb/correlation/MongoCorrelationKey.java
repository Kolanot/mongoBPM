package org.jbpm.persistence.mongodb.correlation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jbpm.persistence.mongodb.session.MongoSessionMap;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoCorrelationKey implements CorrelationKey {
	private static final Logger log = LoggerFactory.getLogger(MongoCorrelationKey.class.getName());
    private String name;
    
    private  List<CorrelationProperty<?>> properties;
	public MongoCorrelationKey() {}
	
    public MongoCorrelationKey(CorrelationKey key) {
    	this.name = key.getName();
    	this.properties = new ArrayList<CorrelationProperty<?>> ();
    	for (Iterator<CorrelationProperty<?>> itr = key.getProperties().iterator(); itr.hasNext();) {
    		CorrelationProperty<?> property = itr.next();
    		this.properties.add(new MongoCorrelationProperty(property.getName(), property.getValue() == null? null: property.getValue().toString()));
    		log.info("Property, name:"+ property.getName() +",value:"+property.getValue());
    	}
    }
    
	public MongoCorrelationKey(String keyName,  String propertyName, String propertyValue) {
		this.name = keyName;
		addProperty(propertyName, propertyValue);
		log.info("Property, name:"+ propertyName +",value:"+propertyValue);
	}

	public void addProperty(String name, String value) {
        if (this.properties == null) {
            this.properties = new ArrayList<CorrelationProperty<?>> ();
        }
        this.properties.add(new MongoCorrelationProperty(name, value));
    }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<CorrelationProperty<?>> getProperties() {
		return properties;
	}

	public void setProperties(List<CorrelationProperty<?>> properties) {
		this.properties = properties;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MongoCorrelationKey other = (MongoCorrelationKey) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "name:" + name + ",properties:"+ properties.toString();
	}
}
