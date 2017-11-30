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
import org.fit.layout.model.Rectangular;

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
    public String getCategory()
    {
        return "pdf";
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
        recursiveSplitCrossingSeparators(root);
        atree.updateTopologies();
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

    //==============================================================================
    
    private void recursiveSplitCrossingSeparators(Area root)
    {
        List<Area> hseps = findSeparatos(root, true);
        List<Area> vseps = findSeparatos(root, false);
        
        boolean change = false;
        while (!hseps.isEmpty() && !vseps.isEmpty())
        {
            Area hsep = hseps.get(0);
            Area vsep = null;
            for (Area cand : vseps)
            {
                if (separatorsCross(hsep, cand)
                        && (splits(hsep, cand) || splits(cand, hsep)))
                {
                    vsep = cand;
                    break;
                }
            }
            if (vsep != null)
            {
                if (vsep.toString().contains("163") && hsep.toString().contains("2015"))
                    System.out.println("jo!");
                final boolean splitHsep = splits(hsep, vsep); 
                final boolean splitVsep = splits(vsep, hsep);
                final Rectangular hb = new Rectangular(hsep.getBounds()); 
                final Rectangular vb = new Rectangular(vsep.getBounds()); 
                
                if (splitHsep)
                {
                    Area nsep = hsep.copy();
                    hsep.getBounds().setX2(vb.getX2());
                    nsep.getBounds().setX1(vb.getX2() + 1);
                    hseps.add(1, nsep);
                    change = true;
                }
                if (splitVsep)
                {
                    Area nsep = vsep.copy();
                    vsep.getBounds().setY2(hb.getY2());
                    nsep.getBounds().setY1(hb.getY2() + 1);
                    vseps.add(1, nsep);
                    change = true;
                }
            }
            else
                hseps.remove(0); //does not cross with anything, remove it
        }
        
        if (change)
            root.updateTopologies();
        
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveSplitCrossingSeparators(root.getChildArea(i));
    }

    private boolean separatorsCross(Area hsep, Area vsep)
    {
        Rectangular hb1 = hsep.getBounds();
        Rectangular hb2 = new Rectangular(hb1);
        if (hb2.getX1() > 0) hb2.setX1(hb2.getX1() - 1);
        hb2.setX2(hb2.getX2() + 1);
        Rectangular vb1 = vsep.getBounds();
        Rectangular vb2 = new Rectangular(vb1);
        if (vb2.getY1() > 0) vb2.setY1(vb2.getY1() - 1);
        vb2.setY2(vb2.getY2() + 1);
        
        return (hb1.intersects(vb2) || vb1.intersects(hb2));
    }
    
    /**
     * Checks if the splitter divides the subject in two parts.
     * @param subject
     * @param splitter
     * @return
     */
    private boolean splits(Area subject, Area splitter)
    {
        if (subject.isHorizontalSeparator())
        {
            int l1 = splitter.getX1() - subject.getX1();
            int l2 = subject.getX2() - splitter.getX2();
            return (l1 > 0 && l2 > 0);
        }
        else if (subject.isVerticalSeparator())
        {
            int l1 = splitter.getY1() - subject.getY1();
            int l2 = subject.getY2() - splitter.getY2();
            return (l1 > 0 && l2 > 0);
        }
        else
            return false;
    }
    
}
