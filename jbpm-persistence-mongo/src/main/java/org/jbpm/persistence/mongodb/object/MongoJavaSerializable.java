package org.jbpm.persistence.mongodb.object;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jbpm.persistence.mongodb.instance.EmbeddedProcessInstance;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.PreSave;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Serialized;
import org.mongodb.morphia.annotations.Transient;

@Embedded
public class MongoJavaSerializable implements MongoSerializable {
	
	private static enum ObjectType {BYTE, DOUBLE, FLOAT, INTEGER,LONG, SHORT, STRING, DATE, NUM, BOOL, OBJ, PROCESS, EMBEDDED};
	
	@Property ObjectType objectType; 
	@Property public String objectString;
	@Embedded private Serializable embeddedObject;
	@Serialized private byte[] objectArray;
	@Transient private Serializable serializedObject;
	@Property private String serializedObjectClassName;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MongoJavaSerializable() {}
	public MongoJavaSerializable(Serializable object){
		initMongoJavaSerializable(object);
	}
	private void initMongoJavaSerializable(Serializable object){
		if (object instanceof Integer) {
			objectType = ObjectType.INTEGER;
			objectString = object.toString();
		} else if (object instanceof Byte) {
			objectType = ObjectType.BYTE;
			objectString = object.toString();
		} else if (object instanceof Double) {
			objectType = ObjectType.DOUBLE;
			objectString = object.toString();
		} else if (object instanceof Float) {
			objectType = ObjectType.FLOAT;
			objectString = object.toString();
		} else if (object instanceof Long) {
			objectType = ObjectType.LONG;
			objectString = object.toString();
		} else if (object instanceof Short) {
			objectType = ObjectType.SHORT;
			objectString = object.toString();
		} else if (object instanceof String) {
			objectString = (String) object;
			objectType = ObjectType.STRING;
		} else if (object instanceof Date) {
			objectString = DateFormat.getDateInstance().format((Date)object);
			objectType = ObjectType.DATE;
		} else if (object instanceof Boolean) {
			objectString = object.toString();
			objectType = ObjectType.BOOL;
		} else if (object instanceof EmbeddedProcessInstance) {
			objectString = "" + ((EmbeddedProcessInstance)object).getProcessInstanceId();
			objectType = ObjectType.PROCESS;
		} else if (object.getClass().isAnnotationPresent(Embedded.class)) {
			this.embeddedObject = object; 
			objectType = ObjectType.EMBEDDED;
		} else if (object instanceof Serializable){
			this.serializedObject = (Serializable)object;
			populateObjectArray();
			objectType = ObjectType.OBJ;
		}
		this.serializedObjectClassName = object.getClass().getName();
	}
	
	@Override
	public String getSerializationStrategyClass() {
		return SerializablePersistenceStrategy.class.getName();
	}

	@Override
	public void setSerializedObject(Serializable object) {
		initMongoJavaSerializable(object);
	}
	
	public String getObjectClassName() {
		return serializedObjectClassName;
	}
	
	@PreSave
	private void populateObjectArray() {
		if (serializedObject == null) {
			objectArray = null;
			return;
		}
		try {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bs);
			os.writeObject(serializedObject);
			os.close();
			objectArray = bs.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void populateSerializedObject() {
		try {
			if (serializedObject == null && objectArray != null) {
				ByteArrayInputStream bs = new ByteArrayInputStream(objectArray);
				ObjectInputStream os = new ObjectInputStream(bs);
				serializedObject = (Serializable)os.readObject();
				os.close();
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Serializable getSerializedObject() {
		switch (objectType) {
			case BYTE: return Byte.parseByte(objectString);
			case SHORT: return Short.parseShort(objectString);
			case INTEGER: return Integer.parseInt(objectString);
			case LONG: return Long.parseLong(objectString);
			case FLOAT: return Float.parseFloat(objectString);
			case DOUBLE: return Double.parseDouble(objectString);
			case DATE: try {
					return DateFormat.getDateInstance().parse(objectString);
				} catch (ParseException e) {
					e.printStackTrace();
					return objectString;
				}
			case STRING: return objectString;
			case BOOL: return new Boolean(objectString);
			case PROCESS: {
				EmbeddedProcessInstance inst = new EmbeddedProcessInstance();
				inst.setProcessInstanceId(Long.parseLong(objectString));
				return inst;
			}
			case EMBEDDED: {
				return embeddedObject;
			}
			case OBJ: {
				populateSerializedObject();
				return serializedObject;
			}
			default: {
				populateSerializedObject();
				return serializedObject;
			}
		}
	}
}
