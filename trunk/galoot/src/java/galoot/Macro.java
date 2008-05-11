package galoot;

import galoot.node.PEntity;

import java.util.Collection;
import java.util.LinkedList;

public class Macro
{
    protected String name;

    protected Collection<String> arguments;

    protected Collection<PEntity> entities;

    public Macro(String name, Collection<String> arguments,
            Collection<PEntity> entities)
    {
        this.name = name;
        this.arguments = new LinkedList<String>(arguments);
        this.entities = new LinkedList<PEntity>(entities);
    }

    public Iterable<String> getArguments()
    {
        return arguments;
    }

    public Iterable<PEntity> getEntities()
    {
        return entities;
    }

    public String getName()
    {
        return name;
    }

    public int getNumArguments()
    {
        return arguments.size();
    }

}
