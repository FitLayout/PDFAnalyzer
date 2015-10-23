/**
 * RemapSeparatorsOperator.java
 *
 * Created on 16. 10. 2015, 14:50:00 by burgetr
 */
package org.fit.pdf.op;

import java.util.List;
import java.util.Vector;

import org.fit.layout.impl.AreaGrid;
import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.segm.grouping.AreaImpl;

/**
 * 
 * @author burgetr
 */
public class RemapSeparatorsOperator extends BaseOperator
{
    protected final String[] paramNames = { };
    protected final ValueType[] paramTypes = { };
    
    public RemapSeparatorsOperator()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Pdf.RemapSeparators";
    }
    
    @Override
    public String getName()
    {
        return "Remap separators to areas";
    }

    @Override
    public String getDescription()
    {
        return "Maps the standalone separators created by the PDF graphics to the area borders";
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
    
    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
    }
    
    //==============================================================================
    
    private void findNeigborLeft(AreaImpl area)
    {
        final AreaGrid grid = area.getGrid();
        int y = area.getGridPosition().midY();
        
    }
        
    
    
}
