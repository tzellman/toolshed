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

}
