package demo.ac;

public class DocumentImpl
    extends DocumentPOA
{
    private String name;
    private String text;

    public DocumentImpl( String name, String text )
    {
        this.name = name;
        this.text = text;
    }

    public String name()
    {
        return name;
    }

    public int size()
    {
        return text.length();
    }

}
