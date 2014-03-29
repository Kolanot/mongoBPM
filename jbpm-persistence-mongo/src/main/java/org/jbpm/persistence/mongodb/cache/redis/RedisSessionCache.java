package org.jbpm.persistence.mongodb.cache.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jbpm.persistence.mongodb.MongoPersistUtil;
import org.jbpm.persistence.mongodb.cache.MongoSessionCache;
import org.jbpm.persistence.mongodb.correlation.MongoCorrelationKey;
import org.jbpm.persistence.mongodb.instance.MongoProcessData;
import org.jbpm.persistence.mongodb.instance.MongoProcessInstanceInfo;
import org.jbpm.persistence.mongodb.session.MongoSessionInfo;
import org.jbpm.persistence.mongodb.session.MongoSessionNotFoundException;
import org.jbpm.persistence.mongodb.workitem.MongoWorkItemInfo;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.process.CorrelationKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import redis.clients.jedis.Jedis;

import java.io.Serializable;

public class RedisSessionCache implements MongoSessionCache {
	private static final Logger log = LoggerFactory.getLogger(RedisSessionCache.class.getName());
	private static String PROCESS_PREFIX = "~process:"; 
	private static String SESSION_PREFIX = "~session:"; 
	private static String INSTANCE_PREFIX = "~instance:"; 
	private static String CORRELATION_PREFIX = "~correlation:"; 
	private static String WORKITEM_PREFIX = "~workitem:"; 
	
	private Jedis jedis = RedisServiceLocator.STANDALONE.getRedis();
	private Gson gson = new Gson(); 
	public RedisSessionCache() {}
	
	public Set<MongoSessionInfo> getAllSessions() {
		Set<MongoSessionInfo> allSessions = new HashSet<MongoSessionInfo>();
		Set<String> sessionIds = getAllMembersFromRedisSet(SESSION_PREFIX);
		for (String sessionIdStr:sessionIds) {
			int sessionId = Integer.valueOf(sessionIdStr);
			allSessions.add(getCachedSession(sessionId));
		}
		return allSessions;
	}
	
	public MongoSessionInfo getCachedSession (int sessionId) {
		byte[] sessionKey = (SESSION_PREFIX + sessionId).getBytes();
		return (MongoSessionInfo)getFromRedisCache(sessionKey);
	}
	
	public void removeCachedSession(int sessionId) {
		byte[] sessionKey = (SESSION_PREFIX + sessionId).getBytes();
		delFromRedisCache(sessionKey);
		removeFromRedisSet(SESSION_PREFIX, "" + sessionId);
	}
	
	private Set<String> getAllMembersFromRedisSet(String setKey) {
		return jedis.smembers(setKey);
	}
	
	private void addToRedisSet(String setKey, String value) {
		jedis.sadd(setKey, value);
	}
	
	private void removeFromRedisSet(String setKey, String value) {
		jedis.srem(setKey, value);
	}
	
	private void setToRedisCache(String key, String bs) {
		jedis.set(key, bs);
	}
	
	private void setToRedisCache(byte[] key, byte[] value) {
		jedis.set(key, value);
	}
	
	private String getFromRedisCache(String key) {
		return jedis.get(key);
	}
	
	private Serializable getFromRedisCache(byte[] key) {
		return fromByteArray(jedis.get(key));
	}

	private void delFromRedisCache(byte[] key) {
		jedis.del(key);
	}
	
	private byte[] toByteArray(Serializable obj) {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream(); 
			ObjectOutputStream out = new ObjectOutputStream(bo);
			out.writeObject(obj);
			out.close();
			return bo.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
		
	
	private Serializable fromByteArray(byte[] byteArray) {
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(byteArray);
			ObjectInputStream in = new ObjectInputStream(bi);
			Object obj = in.readObject();
			in.close();
			return (Serializable)obj;
		} catch (IOException e) {
			log.error("Caught an IOException when convert byteArray");
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			log.error("Caught a ClassNotFoundException when convert byteArray");
			e.printStackTrace();
			return null;
		}
	}

	private String toJsonString(Object obj) {
		return gson.toJson(obj);
	}
	
	public void addSession(MongoSessionInfo sessionInfo) {
		String sessionIdStr = "" + sessionInfo.getId();
		byte[] sessionByteArray = toByteArray(sessionInfo);
		if (sessionByteArray == null) {
			log.error("unable to convert sessionInfo:" + sessionInfo.getId() + " to byte array");
			return;
		}
		setToRedisCache((SESSION_PREFIX+sessionIdStr).getBytes(), sessionByteArray);
		addToRedisSet(SESSION_PREFIX, sessionIdStr);
		
		MongoProcessData procData = sessionInfo.getProcessdata();
		for (MongoProcessInstanceInfo procInstInfo: procData.getProcessInstances()) {
			String procInstanceIdStr = "" + procInstInfo.getProcessInstanceId();
			setToRedisCache(INSTANCE_PREFIX+procInstanceIdStr, sessionIdStr);
			addToRedisSet(PROCESS_PREFIX+procInstInfo.getProcessId(), procInstanceIdStr);
			log.info("add to cache, key:" + procInstInfo.getProcessId() + ", value:" + procInstanceIdStr);
			if (procInstInfo.getCorrelationKey() != null) {
				log.info("procInstInfo, class: " + procInstInfo.getCorrelationKey().getClass()+ ":" + procInstInfo.getCorrelationKey());
				setToRedisCache(CORRELATION_PREFIX+toJsonString(procInstInfo.getCorrelationKey()), "" + sessionIdStr);
			}
		}
		Collection<MongoWorkItemInfo> workItemSet = procData.getWorkItems();
		for (Iterator<MongoWorkItemInfo> itr = workItemSet.iterator(); itr.hasNext();) {
			long workItemId = itr.next().getId();
			setToRedisCache(WORKITEM_PREFIX+workItemId, sessionIdStr);
		}
	}
	
	public MongoProcessInstanceInfo getProcessInstance(long procInstId, boolean readOnly) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(procInstId);
		log.info("procInstId" + procInstId);
		log.info("sessionId" + sessionId);
		MongoSessionInfo sessionInfo  = getCachedSession(sessionId);
		if (sessionInfo == null) return null;
		MongoProcessData procData = sessionInfo.getProcessdata();
		if (procData != null) {
			if (!readOnly) sessionInfo.setModifiedSinceLastSave(true);
			return procData.getProcessInstance(procInstId);
		}
		return null;
	}

	public Set<ProcessInstance> findProcessInstancesByEvent(int sessionId, String type) {
		MongoSessionInfo sessionInfo  = getCachedSession(sessionId);
		if (sessionInfo == null) 
			throw new MongoSessionNotFoundException("not found session by id " + sessionId);
		Set<ProcessInstance> instances = new HashSet<ProcessInstance>(); 
		for (MongoProcessInstanceInfo procInstInfo: sessionInfo.getProcessdata().getProcessInstances()) {
			ProcessInstance instance = procInstInfo.getProcessInstance();
			if (instance.getEventTypes() != null) {
				for (String eventType: instance.getEventTypes()) {
					if (eventType.equals(type)) {
						instances.add(instance);
					}
				}
			}
		}
		return instances;
	}
	
	public MongoProcessInstanceInfo findProcessInstanceByCorrelationKey(CorrelationKey key) {
		MongoCorrelationKey mongoKey = new MongoCorrelationKey(key);
		log.info("mongoKey:" + mongoKey);
		String sessionIdStr = getFromRedisCache(CORRELATION_PREFIX+toJsonString(mongoKey));
		if (sessionIdStr != null) {
			MongoSessionInfo sessionInfo  = getCachedSession(Integer.valueOf(sessionIdStr));
			MongoProcessData procData = sessionInfo.getProcessdata();
			if (procData != null) { 
				for (MongoProcessInstanceInfo procInstance:procData.getProcessInstances()) {
					if (procInstance.getCorrelationKey().equals(mongoKey))
						return procInstance;
				}
			}
		}
		return null;
	}

	public Set<MongoProcessInstanceInfo> findProcessInstancesByProcessId(String processId) {
		Set<MongoProcessInstanceInfo> result = new HashSet<MongoProcessInstanceInfo>();
		Set<String> procInstSet = jedis.smembers(PROCESS_PREFIX+processId);
		for (String procInstStr: procInstSet) {
			long procInstId = Long.valueOf(procInstStr);
			MongoProcessInstanceInfo procInstInfo = getProcessInstance(procInstId, true);
			result.add(procInstInfo);
		}
		return result;
	}

	public boolean isSessionCached(int sessionId) {
		return getCachedSession(sessionId) != null;
	}
	
	public boolean isProcessInstanceCached(long procInstId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(procInstId);
		return isSessionCached(sessionId);
	}
	
	public boolean isWorkItemCached(long workItemId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(workItemId);
		return isSessionCached(sessionId);
	}
	
	public void removeProcessInstance(long procInstId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(procInstId);
		MongoSessionInfo sessionInfo  = getCachedSession(sessionId);
		MongoProcessData procData = sessionInfo.getProcessdata();
		if (procData != null) {
			MongoProcessInstanceInfo procInstInfo = procData.getProcessInstance(procInstId);
			procData.removeProcessInstance(procInstId);
			jedis.del(INSTANCE_PREFIX+procInstId, "" + sessionId);
			jedis.srem(PROCESS_PREFIX+procInstInfo.getProcessId(), "" + procInstId);
		}
		sessionInfo.setModifiedSinceLastSave(true);
	}
	
	public MongoSessionInfo getSessionByWorkItemId(long workItemId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(workItemId);
		MongoSessionInfo sessionInfo  = getCachedSession(sessionId);
		return sessionInfo;
	}
	
	public MongoWorkItemInfo getWorkItem(long workItemId) {
		int sessionId = MongoPersistUtil.resolveFirstIdFromPairing(workItemId);
		MongoSessionInfo sessionInfo  = getCachedSession(sessionId);
		MongoProcessData procData = sessionInfo.getProcessdata();
		if (procData != null) {
			return procData.getWorkItem(workItemId);
		} else {
			return null;
		}
	}
}
