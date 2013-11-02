package org.jbpm.persistence.mongodb.rule;

import org.drools.core.spi.FactHandleFactory;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class EmbeddedFactHandleFactory {
	@Property
	private int id;
	@Property
	private long recency;

	public EmbeddedFactHandleFactory() {}
	
	public EmbeddedFactHandleFactory(FactHandleFactory factory) {
		this.id = factory.getId();
		this.recency = factory.getRecency();
	}

	public int getId() {
		return id;
	}

	public long getRecency() {
		return recency;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setRecency(long recency) {
		this.recency = recency;
	}
}