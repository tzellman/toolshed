/**
 * 
 */
package galoot;

import java.util.Map;

public interface InputAdapter
{
    /**
     * Retrieve a map suitable for use with a {@link Context}
     * 
     * @return a {@link Map} suitable for use with a {@link Context}
     */
    public abstract Map<String, Object> getContextInput();
}
