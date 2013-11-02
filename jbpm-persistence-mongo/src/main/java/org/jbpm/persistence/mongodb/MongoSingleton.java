package org.jbpm.persistence.mongodb;

import com.mongodb.MongoClient;

public enum MongoSingleton {
	INSTANCE;

	private MongoClient mongo;
	
	public MongoClient getMongo() {
		return mongo;
	}
	
	private MongoSingleton() {
		try {
			mongo = new MongoClient("localhost");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
