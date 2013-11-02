package org.jbpm.persistence.mongodb.test;

import org.jbpm.persistence.mongodb.MongoSessionStore;
import org.junit.Test;


public class MongoStoreTest extends AbstractMongoBaseTest {
	@Test
	public void storeExists() {
		MongoSessionStore store = getSessionStore();
		assertNotNull(store);
	}

	@Test
	public void getSessionId() {
		MongoSessionStore store = getSessionStore();
		assertNotNull(store.getNextSessionId());
	}

	@Test
	public void getWorkItemId() {
		MongoSessionStore store = getSessionStore();
		assertNotNull(store.getNextWorkItemId());
	}

	@Test
	public void getProcessInstanceId() {
		MongoSessionStore store = getSessionStore();
		assertNotNull(store.getNextProcessInstanceId());
	}

	private MongoSessionStore getSessionStore() {
		MongoSessionStore store = ((MongoSessionStore) getEnv().get( MongoSessionStore.envKey ));
		return store;
	}

}
