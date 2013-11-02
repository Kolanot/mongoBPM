package org.jbpm.persistence.mongodb.rule;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.drools.core.common.ActivationsFilter;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.phreak.RuleAgendaItem;
import org.drools.core.phreak.RuleExecutor;
import org.drools.core.phreak.StackEntry;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.reteoo.TerminalNode;
import org.drools.core.spi.Activation;
import org.drools.core.spi.AgendaFilter;

public class MongoActivationsFilter implements ActivationsFilter, AgendaFilter {
	private Map<MongoActivationKey, EmbeddedActivation> dormantActivations;
	private Map<MongoActivationKey, EmbeddedActivation> rneActivations;
	private Map<MongoActivationKey, LeftTuple> tuplesCache;
	private Queue<RuleAgendaItem> rneaToFire;

	public MongoActivationsFilter() {
		this.dormantActivations = new HashMap<MongoActivationKey, EmbeddedActivation>();
		this.rneActivations = new HashMap<MongoActivationKey, EmbeddedActivation>();
		this.tuplesCache = new HashMap<MongoActivationKey, LeftTuple>();
		this.rneaToFire = new ConcurrentLinkedQueue<RuleAgendaItem>();
	}

	public Map<MongoActivationKey, EmbeddedActivation> getDormantActivationsMap() {
		return this.dormantActivations;
	}

	public boolean accept(Activation activation,
			InternalWorkingMemory workingMemory, TerminalNode rtn) {
		if (activation.isRuleAgendaItem()) {
			MongoActivationKey key = new MongoActivationKey(activation);
			if (!this.rneActivations.containsKey(key)
					|| this.rneActivations.get(key).isEvaluated()) {
				rneaToFire.add((RuleAgendaItem) activation);
			}
			return true;
		} else {
			MongoActivationKey key = new MongoActivationKey(activation);
			// add the tuple to the cache for correlation
			this.tuplesCache.put(key, activation.getTuple());
			// check if there was an active activation for it
			return !this.dormantActivations.containsKey(key);
		}
	}

	public Map<MongoActivationKey, LeftTuple> getTuplesCache() {
		return tuplesCache;
	}

	public Map<MongoActivationKey, EmbeddedActivation> getRneActivations() {
		return rneActivations;
	}

	public void fireRNEAs(final InternalWorkingMemory wm) {
		RuleAgendaItem rai = null;
		while ((rai = rneaToFire.poll()) != null) {
			RuleExecutor ruleExecutor = rai.getRuleExecutor();
			ruleExecutor.reEvaluateNetwork(wm,
					new org.drools.core.util.LinkedList<StackEntry>(), false);
			ruleExecutor.removeRuleAgendaItemWhenEmpty(wm);
		}
	}

	@Override
	public boolean accept(Activation match) {
		MongoActivationKey key = new MongoActivationKey(match);
		// add the tuple to the cache for correlation
		this.tuplesCache.put(key, match.getTuple());
		// check if there was an active activation for it
		return !this.dormantActivations.containsKey(key);
	}
}
