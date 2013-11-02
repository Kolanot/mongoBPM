package org.jbpm.persistence.mongodb.session;

public class MongoSessionNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MongoSessionNotFoundException(String message) {
        super(message);
    }

    public MongoSessionNotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}
