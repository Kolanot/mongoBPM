package org.jbpm.persistence.mongodb.test;

import org.jbpm.persistence.mongodb.MongoProcessStore;
import org.junit.Test;


public class MongoStoreTest extends AbstractMongoBPMBaseTest {
	@Test
	public void storeExists() {
		MongoProcessStore store = getProcessStore();
		assertNotNull(store);
	}

	@Test
	public void getWorkItemId() {
		MongoProcessStore store = getProcessStore();
		long nextWorkItemId = store.getNextWorkItemId();
		System.out.println("Next work item id:" + nextWorkItemId);
		assertTrue(nextWorkItemId > 0);
	}

	@Test
	public void getProcessInstanceId() {
		MongoProcessStore store = getProcessStore();
		long nextProcessInstanceId = store.getNextProcessInstanceId();
		System.out.println("Next process instance id:" + nextProcessInstanceId);
		assertNotNull(nextProcessInstanceId);
	}

	private MongoProcessStore getProcessStore() {
		MongoProcessStore store = ((MongoProcessStore) getEnv().get( MongoProcessStore.envKey ));
		return store;
	}

}
