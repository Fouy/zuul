package com.moguhu.zuul.util;

import java.io.*;

/**
 * Deep copy of an Object. The Object must be Serializable
 *
 * @author Mikey Cohen
 *         Date: 1/31/12
 *         Time: 11:54 AM
 */
public class DeepCopy {
    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     *
     * @param orig an <code>Object</code> value
     * @return a deep copy of that Object
     * @throws NotSerializableException if an error occurs
     */
    public static Object copy(Object orig) throws NotSerializableException {
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        } catch (NotSerializableException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }
}
