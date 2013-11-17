package org.jbpm.persistence.mongodb.rule;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.reteoo.ObjectTypeConf;
import org.jbpm.persistence.mongodb.rule.tms.EmbeddedTruthMaintenanceSystem;


import org.mongodb.morphia.annotations.Embedded;

@Embedded
public class EmbeddedEntryPoint implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class ObjectTypeConfiguration implements java.io.Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String typeName;
		private Boolean TMSEnabled;
		public ObjectTypeConfiguration() {}
		public ObjectTypeConfiguration(ObjectTypeConf otc) {
			this.typeName = otc.getTypeName();
			TMSEnabled = otc.isTMSEnabled();
		}
		public String getTypeName() {
			return typeName;
		}
		public Boolean isTMSEnabled() {
			return TMSEnabled;
		}
		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}
		public void setTMSEnabled(Boolean tMSEnabled) {
			TMSEnabled = tMSEnabled;
		}
	}
	private String entryPointId;
	private final List<EmbeddedFactHandle> objectStore = new ArrayList<EmbeddedFactHandle>();
	private final List<ObjectTypeConfiguration> otcList = new ArrayList<ObjectTypeConfiguration>();
	private EmbeddedTruthMaintenanceSystem tms;
	
	public EmbeddedEntryPoint() {}
	
	public EmbeddedEntryPoint(String entryPointId) {
		this.entryPointId = entryPointId;
	}
	
	public String getEntryPointId() {
		return entryPointId;
	}
	
	public void setEntryPointId(String entryPointId) {
		this.entryPointId = entryPointId;
	}
	
	public List<EmbeddedFactHandle> getObjectStore() {
		return objectStore;
	}
	
	public void addHandle(EmbeddedFactHandle handle) {
		objectStore.add(handle);
	}
	
	public List<ObjectTypeConfiguration> getOtcList() {
		return otcList;
	}
	
	public void addOtc(ObjectTypeConfiguration otc) {
		otcList.add(otc);
	}

	public EmbeddedTruthMaintenanceSystem getTms() {
		return tms;
	}

	public void setTms(EmbeddedTruthMaintenanceSystem tms) {
		this.tms = tms;
	}
}
