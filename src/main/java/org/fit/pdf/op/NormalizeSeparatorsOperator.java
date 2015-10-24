/**
 * NormalizeSeparatorsOperator.java
 *
 * Created on 24. 10. 2015, 11:39:56 by burgetr
 */
package org.fit.pdf.op;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;

/**
 * 
 * @author burgetr
 */
public class NormalizeSeparatorsOperator extends BaseOperator
{
    protected final String[] paramNames = { };
    protected final ValueType[] paramTypes = { };
    
    public NormalizeSeparatorsOperator()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Pdf.NormalizeSeparators";
    }
    
    @Override
    public String getName()
    {
        return "Normalize separators";
    }

    @Override
    public String getDescription()
    {
        return "Normalizes the standalone separators, prevents crossing of separators";
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
        recursiveJoinSeparators(root);
    }
    
    //==============================================================================
    
    private void recursiveJoinSeparators(Area root)
    {
        //join horizontal separators
        List<Area> seps = findSeparatos(root, true);
        Collections.sort(seps, new Comparator<Area>() {
            @Override
            public int compare(Area o1, Area o2)
            {
                if (o1.getY1() == o2.getY1())
                    return o1.getX1() - o2.getX1();
                else
                    return o1.getY1() - o2.getY1();
            }
        });
        Area last = null;
        for (Area sep : seps)
        {
            if (last != null && isJoinable(last, sep, true))
            {
                System.out.println("Joining: " + last + " AND " + sep);
                last.getBounds().expandToEnclose(sep.getBounds());
                root.removeChild(sep);
            }
            else
                last = sep;
        }
        
        //join vertical separators
        seps = findSeparatos(root, false);
        Collections.sort(seps, new Comparator<Area>() {
            @Override
            public int compare(Area o1, Area o2)
            {
                if (o1.getX1() == o2.getX1())
                    return o1.getY1() - o2.getY1();
                else
                    return o1.getX1() - o2.getX1();
            }
        });
        last = null;
        for (Area sep : seps)
        {
            if (last != null && isJoinable(last, sep, false))
            {
                System.out.println("Joining: " + last + " AND " + sep);
                last.getBounds().expandToEnclose(sep.getBounds());
                root.removeChild(sep);
            }
            else
                last = sep;
        }
        
        //apply recursively
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveJoinSeparators(root.getChildArea(i));
    }
    
    private List<Area> findSeparatos(Area root, boolean horizontal)
    {
        List<Area> ret = new Vector<Area>();
        for (int i = 0; i < root.getChildCount(); i++)
        {
            final Area child = root.getChildArea(i);
            if ((horizontal && child.isHorizontalSeparator()) || (!horizontal && child.isVerticalSeparator()))
            {
                ret.add(child);
            }
        }
        return ret;
    }
    
    private boolean isJoinable(Area sep1, Area sep2, boolean horizontal)
    {
        if (sep1.isLeaf() && sep2.isLeaf())
        {
            if (horizontal)
            {
                return sep1.getY1() == sep2.getY1()
                        && sep1.getY2() == sep2.getY2()
                        && sep2.getX1() >= sep1.getX1()
                        && sep2.getX1() <= sep1.getX2() + 1;
            }
            else
            {
                return sep1.getX1() == sep2.getX1()
                        && sep1.getX2() == sep2.getX2()
                        && sep2.getY1() >= sep1.getY1()
                        && sep2.getY1() <= sep1.getY2() + 1;
            }
        }
        else
            return false;
    }

}
