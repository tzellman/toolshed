package jester;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface IJester
{
    /**
     * Serializes an object to an OutputStream
     * 
     * @param object
     *            the Object to be serialized
     * @param out
     *            the OutputStream to serialize to
     * @param hints
     *            an optional Map of serialization hints
     * @return
     * @throws Exception
     */
    void out(Object object, OutputStream out, Map hints) throws Exception;

    /**
     * Serializes an InputStream to an Object
     * 
     * @param stream
     *            the InputStream to read from
     * @param hints
     *            optional Map of serialization hints
     * @return
     * @throws Exception
     */
    Object in(InputStream stream, Map hints) throws Exception;

    /**
     * @return the content type supported by this serializer
     */
    String getContentType();

}
