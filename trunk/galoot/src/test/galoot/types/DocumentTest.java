package galoot.types;

import java.util.Collection;

import junit.framework.TestCase;

public class DocumentTest extends TestCase
{

    public void testDocument()
    {
        Document doc = new Document();

        // let's add some text to the block
        doc.addContent("some text\n");
        doc.addContent("some more text...");
        doc.addContent("--> foo -- bar");
        // end first top-level fragment, since all text fragments get collapsed

        doc.addContent(new BlockFragment("firstBlock"));
        doc.addContent("first");
        doc.addContent(" block");
        doc.addContent(" text");
        // end second top-level fragment

        doc.addContent(new BlockFragment("firstBlockNested"));
        doc.addContent("nested text");

        // let's pop out of the blocks
        doc.popBlock();
        doc.popBlock();

        doc.addContent(new BlockFragment("secondBlock"));
        doc.addContent("second");
        doc.addContent(" block");
        doc.addContent(" text");
        // end third top-level fragment

        // pop the block
        doc.popBlock();
        
        //finally, add some text into the top-level doc
        doc.addContent("more text");

        DocumentFragment contents = (DocumentFragment) doc.getContents();
        Collection<DocumentFragment> fragments = (Collection<DocumentFragment>) contents
                .getContents();

        assertEquals(fragments.size(), 4);
    }
}
