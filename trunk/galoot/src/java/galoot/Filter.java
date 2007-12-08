package galoot;

public interface Filter
{
    /**
     * Filter an object by applying some type of processing to it. The input
     * object can be anything, so good implementations should only handle known
     * types for the filter.
     * 
     * If the input object does not apply to the operation at hand, you can
     * return null. You could also return the original object.
     * 
     * @param object
     *            the input object
     * @param args
     *            optional arguments, pertinent to the operation
     * @return the resultant object
     */
    public Object filter(Object object, String args);

    /**
     * Return the name of this filter. Filter names are case-sensitive.
     * 
     * @return the name of the filter
     */
    public String getName();
}
