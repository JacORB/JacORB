package demo.ac;

public class PrinterImpl
    extends PrinterPOA
{
    private String name;

    public PrinterImpl(String name)
    {
        this.name = name;
    }

    public java.lang.String name()
    {
        return name;
    }

    public void printDocument(demo.ac.Document doc)
    {
    }

}
