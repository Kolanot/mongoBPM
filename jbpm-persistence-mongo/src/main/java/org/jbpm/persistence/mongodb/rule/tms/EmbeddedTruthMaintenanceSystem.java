package org.jbpm.persistence.mongodb.rule.tms;

import java.util.ArrayList;
import java.util.List;


public class EmbeddedTruthMaintenanceSystem implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private List<EmbeddedEqualityKey> keys = new ArrayList<EmbeddedEqualityKey>();
	
	public List<EmbeddedEqualityKey> getKeys() {
		return keys;
	}
	public void addKey(EmbeddedEqualityKey key) {
		keys.add(key);
	}
}
