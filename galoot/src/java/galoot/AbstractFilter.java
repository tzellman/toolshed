package galoot;

/**
 * AbstractFilter provides the getName() method of the Filter interface. It just
 * uses the lower-cased simpleName of the class as the filter name.
 * 
 * For example:
 * My_Cool_Filter --> my_cool_filter
 * MyFilter --> myfilter
 */
public abstract class AbstractFilter implements Filter
{
    private String name;

    public AbstractFilter()
    {
        name = this.getClass().getSimpleName().toLowerCase();
    }

    public String getName()
    {
        return name;
    }

}
