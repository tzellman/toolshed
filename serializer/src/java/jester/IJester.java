package jester;

import java.io.InputStream;
import java.io.OutputStream;

public interface IJester
{
    /**
     * Serializes an object to an OutputStream
     * 
     * @param object
     * @return
     * @throws Exception
     */
    void out(Object object, OutputStream out) throws Exception;

    /**
     * Serializes an InputStream to an Object
     * 
     * @param stream
     * @return
     * @throws Exception
     */
    Object in(InputStream stream) throws Exception;

    /**
     * @return the content type supported by this serializer
     */
    String getContentType();
}
