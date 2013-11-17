package org.jbpm.persistence.mongodb.correlation;

import org.kie.internal.process.CorrelationProperty;
import org.mongodb.morphia.annotations.Property;

public class MongoCorrelationProperty implements CorrelationProperty<String>, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Property
	private String name;
	@Property
	private String value;

	public MongoCorrelationProperty() {}
	
	public MongoCorrelationProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
        return String.class.getName();
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		MongoCorrelationProperty other = (MongoCorrelationProperty) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "name:" + name +",value:"+ value;
	}
}
