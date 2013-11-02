package org.jbpm.persistence.mongodb.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationProperty;
import org.junit.Test;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.utils.ServiceRegistryImpl;

public class MongoCorrelationKeyTest {

	@Test
	public void testEmptyKey1() {
		MongoCorrelationKey key1 = new MongoCorrelationKey();
		MongoCorrelationKey key2 = new MongoCorrelationKey();
		assertEquals(key1, key2);
		assertEquals(key1.hashCode(), key2.hashCode());
	}

	@Test
	public void testEmptyKey2() {
		MongoCorrelationKey key1 = new MongoCorrelationKey();
		MongoCorrelationKey key2 = new MongoCorrelationKey();
		MongoCorrelationProperty prop = new MongoCorrelationProperty();
		key2.addProperty(prop.getName(), prop.getValue());
		assertNotEquals(key1, key2);
	}

	@Test
	public void testSimpleValue1() {
		MongoCorrelationKey key1 = new MongoCorrelationKey();
		key1.addProperty(null, "test");
		MongoCorrelationKey key2 = new MongoCorrelationKey();
		key2.addProperty(null, "test");
		assertEquals(key1, key2);
		assertEquals(key1.hashCode(), key2.hashCode());
	}

	@Test
	public void testSimpleValue2() {
		MongoCorrelationKey key1 = new MongoCorrelationKey();
		key1.addProperty(null, "test1");
		MongoCorrelationKey key2 = new MongoCorrelationKey();
		key2.addProperty(null, "test2");
		assertNotEquals(key1, key2);
	}

	@Test
	public void testSimpleKey1() {
		MongoCorrelationKey key1 = new MongoCorrelationKey("key",null,"test");
		MongoCorrelationKey key2 = new MongoCorrelationKey("key",null,"test");
		assertEquals(key1, key2);
		assertEquals(key1.hashCode(), key2.hashCode());
	}

	@Test
	public void testSimpleKey2() {
		MongoCorrelationKey key1 = new MongoCorrelationKey("key",null,"test1");
		MongoCorrelationKey key2 = new MongoCorrelationKey("key",null,"test2");
		assertNotEquals(key1, key2);
	}

	@Test
	public void testSimpleKey3() {
		MongoCorrelationKey key1 = new MongoCorrelationKey("key","prop","test");
		MongoCorrelationKey key2 = new MongoCorrelationKey("key",null,"test");
		assertNotEquals(key1, key2);
	}

	@Test
	public void testSimpleKey4() {
		MongoCorrelationKey key1 = new MongoCorrelationKey("key","prop","test");
		MongoCorrelationKey key2 = new MongoCorrelationKey("key","prop","test");
		assertEquals(key1, key2);
		assertEquals(key1.hashCode(), key2.hashCode());
	}

	@Test
	public void testSimpleKey5() {
		MongoCorrelationKey key1 = new MongoCorrelationKey("key","prop","test");
		MongoCorrelationKey key2 = new MongoCorrelationKey("key","prop","test");
		key2.addProperty("prop", "test");
		assertNotEquals(key1, key2);
	}
	
}
