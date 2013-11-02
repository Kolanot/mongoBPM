package org.jbpm.persistence.mongodb.rule.tms;

import java.util.ArrayList;
import java.util.List;


public class EmbeddedTruthMaintenanceSystem {

	private List<EmbeddedEqualityKey> keys = new ArrayList<EmbeddedEqualityKey>();
	
	public List<EmbeddedEqualityKey> getKeys() {
		return keys;
	}
	public void addKey(EmbeddedEqualityKey key) {
		keys.add(key);
	}
}
