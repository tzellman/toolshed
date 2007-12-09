package galoot.types;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BlockFragment implements DocumentFragment
{
    private Deque<DocumentFragment> contents;

    private Map<String, BlockFragment> blocks;

    private String name;

    public BlockFragment(String name)
    {
        this.name = name;
        contents = new LinkedList<DocumentFragment>();
        blocks = new HashMap<String, BlockFragment>();
    }

    /**
     * Returns an unmodifiable Collection, represesnting the fragment stack.
     */
    public Object getContents()
    {
        Collection<DocumentFragment> collection = Collections
                .unmodifiableCollection(contents);
        return collection;
    }

    public void addContent(Object content) throws IllegalArgumentException
    {
        if (content == null)
            return;

        if (content instanceof String || content instanceof StringBuffer
                || content instanceof TextFragment)
        {
            if (contents.isEmpty()
                    || !(contents.peekLast() instanceof TextFragment))
                contents.addLast(new TextFragment());
            contents.peekLast().addContent(content);
        }
        else if (content instanceof DocumentFragment)
        {
            if (content instanceof BlockFragment)
            {
                BlockFragment blockContent = (BlockFragment) content;
                blocks.put(blockContent.getName(), blockContent);
            }
            contents.addLast((DocumentFragment) content);
        }
    }

    public String getName()
    {
        return name;
    }

    /**
     * Returns whether or not a block with this name exists in this block. This
     * will look recursively. The best use-case is to call this from a
     * top-level.
     * 
     * @param name
     * @return
     */
    public boolean hasBlock(String name)
    {
        if (blocks.containsKey(name))
            return true;
        for (BlockFragment block : blocks.values())
        {
            if (block.hasBlock(name))
                return true;
        }
        return false;
    }

}
