package galoot.types;

public class TextFragment implements DocumentFragment
{
    private StringBuffer contents;

    public TextFragment()
    {
        contents = new StringBuffer();
    }

    /**
     * Returns the contents, as a string.
     */
    public Object getContents()
    {
        return contents.toString();
    }

    public void addContent(Object content) throws IllegalArgumentException
    {
        if (content == null)
            return;

        if (content instanceof StringBuffer)
            contents.append((StringBuffer) content);
        else if (content instanceof String)
            contents.append((String) content);
        else if (content instanceof TextFragment)
            contents.append((String) ((TextFragment) content).getContents());
        else
            throw new IllegalArgumentException("Cannot add content of type: "
                    + content.getClass().getName());
    }
}
