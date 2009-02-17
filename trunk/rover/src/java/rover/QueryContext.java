package rover;

/**
 * The context of a Query, which is provided as input to a Query operation. This
 * could be a reusable object used for several queries.
 * 
 * @author tzellman
 */
public abstract class QueryContext implements ConnectionProvider
{
    protected SQLTypeConverterRegistry registry;

    protected DatabaseInfoCache cache;

    public QueryContext()
    {
        this(null, null);
    }

    public QueryContext(DatabaseInfoCache cache,
            SQLTypeConverterRegistry registry)
    {
        if (cache == null)
            cache = new DatabaseInfoCache();
        if (registry == null)
            registry = new SQLTypeConverterRegistry();

        this.cache = cache;
        this.registry = registry;
    }

    public SQLTypeConverterRegistry getRegistry()
    {
        return registry;
    }

    public DatabaseInfoCache getCache()
    {
        return cache;
    }

}
