/**
 * PdfHTMLOutputOperator.java
 *
 * Created on 12. 1. 2016, 11:42:43 by burgetr
 */
package org.fit.pdf.op;

import java.util.ArrayList;
import java.util.List;

import org.fit.layout.api.Parameter;
import org.fit.layout.impl.ParameterBoolean;
import org.fit.layout.impl.ParameterString;
import org.fit.layout.tools.io.HTMLOutputOperator;

/**
 * This operator serializes the area tree to an HTML file. It extends the standard
 * HTMLOutputOperator with further optimizations specific for PDF files. 
 * 
 * @author burgetr
 */
public class PdfHTMLOutputOperator extends HTMLOutputOperator
{
    
    public PdfHTMLOutputOperator()
    {
        super();
    }

    public PdfHTMLOutputOperator(String filename, boolean produceHeader)
    {
        super(filename, produceHeader, false);
    }

    @Override
    public String getId()
    {
        return "FitLayout.Pdf.HTMLOutput";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(2);
        ret.add(new ParameterString("filename"));
        ret.add(new ParameterBoolean("produceHeader"));
        return ret;
    }
    
    //=====================================================================================================
    
    
}
