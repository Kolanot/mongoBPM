package org.jbpm.persistence.mongodb.rule.action;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.jbpm.persistence.mongodb.instance.EmbeddedProcessInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Serialized;

public class EmbeddedSignal {
	public static enum EventType {BYTE, DOUBLE, FLOAT, INTEGER,LONG, SHORT, STRING, DATE, NUM, BOOL, OBJ, PROCESS};
	
	@Property public String stringEvent;
	@Serialized public Object objEvent;
	
	@Property EventType eventType; 
	
	public EmbeddedSignal() {}
	public EmbeddedSignal(Object event) {
		if (event instanceof Integer) {
			eventType = EventType.INTEGER;
			stringEvent = event.toString();
		} else if (event instanceof Byte) {
			eventType = EventType.BYTE;
			stringEvent = event.toString();
		} else if (event instanceof Double) {
			eventType = EventType.DOUBLE;
			stringEvent = event.toString();
		} else if (event instanceof Float) {
			eventType = EventType.FLOAT;
			stringEvent = event.toString();
		} else if (event instanceof Long) {
			eventType = EventType.LONG;
			stringEvent = event.toString();
		} else if (event instanceof Short) {
			eventType = EventType.SHORT;
			stringEvent = event.toString();
		} else if (event instanceof String) {
			stringEvent = (String) event;
			eventType = EventType.STRING;
		} else if (event instanceof Date) {
			stringEvent = DateFormat.getDateInstance().format((Date)event);
			eventType = EventType.DATE;
		} else if (event instanceof Boolean) {
			stringEvent = event.toString();
			eventType = EventType.BOOL;
		} else if (event instanceof EmbeddedProcessInstance) {
			stringEvent = "" + ((EmbeddedProcessInstance)event).getProcessInstanceId();
			eventType = EventType.PROCESS;
		} else {
			objEvent = event;
			eventType = EventType.OBJ;
		}
	}
	
	public Object getEvent() {
		switch (eventType) {
		case BYTE: return Byte.parseByte(stringEvent);
		case SHORT: return Short.parseShort(stringEvent);
		case INTEGER: return Integer.parseInt(stringEvent);
		case LONG: return Long.parseLong(stringEvent);
		case FLOAT: return Float.parseFloat(stringEvent);
		case DOUBLE: return Double.parseDouble(stringEvent);
		case DATE: try {
				return DateFormat.getDateInstance().parse(stringEvent);
			} catch (ParseException e) {
				e.printStackTrace();
				return stringEvent;
			}
		case STRING: return stringEvent;
		case BOOL: return new Boolean(stringEvent);
		case PROCESS: {
			EmbeddedProcessInstance inst = new EmbeddedProcessInstance();
			inst.setProcessInstanceId(Long.parseLong(stringEvent));
			return inst;
		}
		case OBJ: return objEvent;
		default:
			return objEvent;
		}
	}
}
