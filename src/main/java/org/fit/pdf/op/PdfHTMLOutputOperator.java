/**
 * PdfHTMLOutputOperator.java
 *
 * Created on 12. 1. 2016, 11:42:43 by burgetr
 */
package org.fit.pdf.op;

import org.fit.layout.tools.io.HTMLOutputOperator;

/**
 * This operator serializes the area tree to an HTML file. It extends the standard
 * HTMLOutputOperator with further optimizations specific for PDF files. 
 * 
 * @author burgetr
 */
public class PdfHTMLOutputOperator extends HTMLOutputOperator
{
    protected final String[] paramNames = { "filename", "produceHeader" };
    protected final ValueType[] paramTypes = { ValueType.STRING, ValueType.BOOLEAN };
    
    public PdfHTMLOutputOperator()
    {
        super();
    }

    public PdfHTMLOutputOperator(String filename, boolean produceHeader)
    {
        super(filename, produceHeader);
    }

    @Override
    public String getId()
    {
        return "FitLayout.Pdf.HTMLOutput";
    }

    @Override
    public String[] getParamNames()
    {
        return paramNames;
    }

    @Override
    public ValueType[] getParamTypes()
    {
        return paramTypes;
    }

    //=====================================================================================================
    
    
}
