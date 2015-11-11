/**
 * PdfArea.java
 *
 * Created on 11. 11. 2015, 10:54:15 by burgetr
 */
package org.fit.pdf;

import org.fit.layout.model.Border;
import org.fit.layout.model.Rectangular;
import org.fit.segm.grouping.AreaImpl;

/**
 * An extended Area implementation that tracks the border style obtained from separators.
 *  
 * @author burgetr
 */
public class PdfArea extends AreaImpl
{
    private Border[] borderStyle;

    public PdfArea(Rectangular r)
    {
        super(r);
        borderStyle = new Border[4];
    }

    @Override
    public String toString()
    {
        return "EE " + super.toString();
    }
    
    public void setBorderStyle(Border.Side side, Border style)
    {
        borderStyle[side.getIndex()] = style;
    }

    public Border getBorderStyle(Border.Side side)
    {
        return borderStyle[side.getIndex()];
    }
    
}
