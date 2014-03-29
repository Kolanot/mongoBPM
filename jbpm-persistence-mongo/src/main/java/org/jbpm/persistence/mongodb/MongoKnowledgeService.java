/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.persistence.mongodb;

import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class MongoKnowledgeService {
	private static MongoKnowledgeStoreService provider;

	public static StatefulKnowledgeSession newStatefulKnowledgeSession(
			KieBase kbase, KieSessionConfiguration configuration,
			Environment environment) {
		return (StatefulKnowledgeSession) getMongoKnowledgeServiceProvider()
				.newKieSession(kbase, configuration, environment);
	}

	public static StatefulKnowledgeSession loadStatefulKnowledgeSessionByProcessInstanceId
		(long procInstId,
			KieBase kbase, KieSessionConfiguration configuration,
			Environment environment) {
		long sessionId = MongoPersistUtil.resolveFirstIdFromPairing(procInstId);
		return (StatefulKnowledgeSession) getMongoKnowledgeServiceProvider()
				.loadKieSession((int)sessionId, kbase, configuration, environment);
	}

	public static StatefulKnowledgeSession loadStatefulKnowledgeSessionByWorkItemId
		(long workItemId,
			KieBase kbase, KieSessionConfiguration configuration,
			Environment environment) {
		long sessionId = MongoPersistUtil.resolveFirstIdFromPairing(workItemId);
		return (StatefulKnowledgeSession) getMongoKnowledgeServiceProvider()
				.loadKieSession((int)sessionId, kbase, configuration, environment);
	}

	public static StatefulKnowledgeSession loadStatefulKnowledgeSession(int id,
			KieBase kbase, KieSessionConfiguration configuration,
			Environment environment) {
		return (StatefulKnowledgeSession) getMongoKnowledgeServiceProvider()
				.loadKieSession(id, kbase, configuration, environment);
	}

	public static void saveStatefulKnowledgeSession(KieSession session) {
		getMongoKnowledgeServiceProvider().saveKieSession(session);
	}

	private static synchronized void setMongoKnowledgeServiceProvider(
			MongoKnowledgeStoreService provider) {
		MongoKnowledgeService.provider = provider;
	}

	private static synchronized MongoKnowledgeStoreService getMongoKnowledgeServiceProvider() {
		if (provider == null) {
			loadProvider();
		}
		return provider;
	}

	private static void loadProvider() {
		try {
			// we didn't find anything in properties so lets try and us
			// reflection
			setMongoKnowledgeServiceProvider(new MongoKnowledgeStoreServiceImpl());
		} catch (Exception e) {
			throw new RuntimeException(
					"Provider MongoKnowledgeStoreServiceImpl could not be set.",
					e);
		}
	}
}
