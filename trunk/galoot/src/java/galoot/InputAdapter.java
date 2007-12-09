/**
 * 
 */
package galoot;

import java.util.Map;

public interface InputAdapter
{
    /**
     * Retrieve a map suitable for use with a {@link ContextStack}
     * 
     * @return a {@link Map} suitable for use with a {@link ContextStack}
     */
    public abstract Map<String, Object> getContextStackInput();
}
