package org.jbpm.persistence.mongodb.rule;

import java.util.ArrayList;
import java.util.List;

public class MongoTupleKey implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Integer> leftTupleHandleIdList = new ArrayList<Integer>();

	public List<Integer> getLeftTupleHandleIdList() {
		return leftTupleHandleIdList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((leftTupleHandleIdList == null) ? 0 : leftTupleHandleIdList
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MongoTupleKey other = (MongoTupleKey) obj;
		if (leftTupleHandleIdList == null) {
			if (other.leftTupleHandleIdList != null)
				return false;
		} else if (!leftTupleHandleIdList.equals(other.leftTupleHandleIdList))
			return false;
		return true;
	}
}
