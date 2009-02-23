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

import java.util.Stack;

public class Document implements DocumentFragment
{

    // ! Keeps track of the block stack depth
    private Stack<BlockFragment> blockStack;

    // ! Top-level "block"
    private BlockFragment documentBlock;

    public Document()
    {
        blockStack = new Stack<BlockFragment>();
        documentBlock = new BlockFragment("");
        // empty string is unique according to the rules
    }

    /**
     * Adds the given content to the stack. If the content is a BlockFragment,
     * we push it onto the block stack.
     */
    public void addContent(Object content) throws IllegalArgumentException
    {
        if (content == null)
            return;
        if (blockStack.isEmpty())
            documentBlock.addContent(content);
        else
            blockStack.peek().addContent(content);

        // if it is a BlockFragment, push it onto our stack
        if (content instanceof BlockFragment)
            blockStack.push((BlockFragment) content);
    }

    /**
     * Returns the top-level "block", which is a BlockFragment.
     */
    public Object getContents()
    {
        return getDocumentBlock();
    }

    /**
     * Returns the top-level "block", which is a BlockFragment.
     * 
     * @return
     */
    public BlockFragment getDocumentBlock()
    {
        return documentBlock;
    }

    /**
     * When finished with a block (which was added via the addContent method),
     * you must pop it off the stack. This method does that for you.
     */
    public void popBlock()
    {
        if (!blockStack.isEmpty())
            blockStack.pop();
    }

    /**
     * @see BlockFragment#hasBlock(String)
     */
    public boolean hasBlock(String name)
    {
        return documentBlock.hasBlock(name);
    }

    /**
     * Returns the depth at which the current block is
     * 
     * @return
     */
    public int getBlockDepth()
    {
        return blockStack.size();
    }

    /**
     * @return
     * @see galoot.types.BlockFragment#evaluateAsString()
     */
    public String evaluateAsString()
    {
        return documentBlock.evaluateAsString();
    }

    /**
     * @param name
     * @return
     * @see galoot.types.BlockFragment#getBlock(java.lang.String)
     */
    public BlockFragment getBlock(String name)
    {
        return documentBlock.getBlock(name);
    }

    /**
     * @param newBlock
     * @return
     * @see galoot.types.BlockFragment#replaceBlock(galoot.types.BlockFragment)
     */
    public boolean replaceBlock(BlockFragment newBlock)
    {
        return documentBlock.replaceBlock(newBlock);
    }

}
