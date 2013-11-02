package org.jbpm.persistence.mongodb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MongoPersistUtil {

    /*
     * http://en.wikipedia.org/wiki/Cantor_pairing_function#Cantor_pairing_function
     */
    public static long pairingSessionId(int sessionId, long itemId) {
    	long w = sessionId+itemId;
    	long t = w * (w + 1)/2;
    	return t +itemId; 
    }

    public static int resolveSessionIdFromPairing(long pairId) {
    	long w = (long) Math.floor((Math.sqrt((8*pairId+1) - 1)/2));
    	long t = w * (w + 1)/2;
    	long wi = pairId -t;
    	long sessionId = w - wi;
    	return (int)sessionId; 
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }
}