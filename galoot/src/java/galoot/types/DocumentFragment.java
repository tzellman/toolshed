package galoot.types;

/**
 * A Document Fragment is constituent of a document. Document fragments have
 * "content", which is user defined. The content can be anything, and can also
 * be restricted.
 */
public interface DocumentFragment
{
    /**
     * Returns the contents of the fragment.
     * 
     * @return
     */
    public Object getContents();

    /**
     * Add the given contents to the fragment. This throws an
     * {@link IllegalArgumentException} if the contents do not apply to this
     * fragment.
     * 
     * @param content
     */
    public void addContent(Object content) throws IllegalArgumentException;
}
