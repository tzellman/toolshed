/* =============================================================================
 * This file is part of Galoot
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Galoot is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package galoot.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public final class BlockFragment implements DocumentFragment
{
    private LinkedList<DocumentFragment> contents;

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
                    || !(contents.getLast() instanceof TextFragment))
                contents.addLast(new TextFragment());
            contents.getLast().addContent(content);
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
        return getBlock(name) != null;
    }

    /**
     * Returns the block with the given name, or null if it doesn't exist
     * 
     * @param name
     * @return
     */
    public BlockFragment getBlock(String name)
    {
        if (blocks.containsKey(name))
            return blocks.get(name);
        for (BlockFragment block : blocks.values())
        {
            BlockFragment b = block.getBlock(name);
            if (b != null)
                return b;
        }
        return null;
    }

    /**
     * Manually set the contents of fragment
     */
    protected void setContent(Collection<DocumentFragment> content)
    {
        this.contents.clear();
        for (DocumentFragment fragment : content)
            this.contents.addLast(fragment);
    }

    /**
     * Replace the block with the same name as the newBlock with the contents of
     * the newBlock, and return the BlockFragments that will now not be
     * available.
     * 
     * @param newBlock
     * @return true iff the block was replaced
     */
    public boolean replaceBlock(BlockFragment newBlock)
    {
        BlockFragment oldBlock = getBlock(newBlock.getName());
        if (oldBlock == null)
            return false;

        oldBlock.setContent((Collection<DocumentFragment>) newBlock
                .getContents());
        return true;
    }

    /**
     * Evaluates the fragment (and all child fragments) recursively, returning
     * the String contents.
     * 
     * @return
     */
    public String evaluateAsString()
    {
        StringBuffer buf = new StringBuffer();
        for (DocumentFragment fragment : contents)
        {
            if (fragment instanceof BlockFragment)
                buf.append(((BlockFragment) fragment).evaluateAsString());
            else if (fragment instanceof TextFragment)
                buf.append((String) ((TextFragment) fragment).getContents());
        }
        return buf.toString();
    }

}
