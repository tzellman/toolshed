package rover.impl;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.LazyDynaBean;

/**
 * DynaClass implementation that always provides a clone of the input bean when
 * you call newInstance().
 * 
 * @author tzellman
 */
public class DynaBeanCloner implements DynaClass
{
    private String name;

    private Map<String, DynaProperty> properties;

    // ! The clone of the input, which acts as the base for all new instances
    private DynaBean cloner;

    public DynaBeanCloner(DynaBean inputBean)
    {
        DynaClass dynaClass = inputBean.getDynaClass();
        DynaProperty[] props = dynaClass.getDynaProperties();
        properties = new TreeMap<String, DynaProperty>();
        for (DynaProperty p : props)
        {
            String name = p.getName();
            properties.put(name, new DynaProperty(name, p.getType(), p
                    .getContentType()));
        }
        this.name = dynaClass.getName();

        // now, create our own bean instance and snag the other bean's data
        cloner = copyBean(inputBean);
    }

    public DynaProperty[] getDynaProperties()
    {
        return properties.values().toArray(new DynaProperty[0]);
    }

    public DynaProperty getDynaProperty(String name)
    {
        return properties.get(name);
    }

    public String getName()
    {
        return name;
    }

    protected DynaBean copyBean(DynaBean bean)
    {
        DynaBean copiedBean = new LazyDynaBean(this);
        for (String name : properties.keySet())
        {
            copiedBean.set(name, bean.get(name));
        }
        return copiedBean;
    }

    public DynaBean newInstance() throws IllegalAccessException,
            InstantiationException
    {
        return copyBean(cloner);
    }

}
