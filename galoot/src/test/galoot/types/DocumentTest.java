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
