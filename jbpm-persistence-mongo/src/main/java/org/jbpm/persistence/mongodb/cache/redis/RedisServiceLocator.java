package org.jbpm.persistence.mongodb.cache.redis;

import redis.clients.jedis.Jedis;

public enum RedisServiceLocator {
	STANDALONE;
	public Jedis getRedis() {
		return new Jedis("localhost");
	}
}
