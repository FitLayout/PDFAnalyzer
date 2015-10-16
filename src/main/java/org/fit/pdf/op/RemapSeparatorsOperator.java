/**
 * RemapSeparatorsOperator.java
 *
 * Created on 16. 10. 2015, 14:50:00 by burgetr
 */
package org.fit.pdf.op;

import java.util.List;
import java.util.Vector;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;

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
        List<SepPair> pairs = findSeparatorPairs(root);
        System.out.println("Sep pairs:");
        for (SepPair pair : pairs)
            System.out.println("  " + pair);
        System.out.println("Incomplete:");
        for (SepPair pair : pairs)
            if (!pair.isComplete())
                System.out.println("  " + pair);
    }
    
    //==============================================================================
    
    
    private List<SepPair> findSeparatorPairs(Area root)
    {
        Vector<SepPair> ret = new Vector<SepPair>();
        recursivelyFindPairs(root, ret);
        return ret;
    }
    
    private void recursivelyFindPairs(Area root, List<SepPair> pairs)
    {
        if (root.isSeparator())
        {
            //try to complete an existing pair
            final SepPair expair = findPairFor(root, pairs);
            if (expair != null)
                expair.addPair(root);
            //and create a new one
            pairs.add(new SepPair(root));
        }
        for (int i = 0; i < root.getChildCount(); i++)
            recursivelyFindPairs(root.getChildArea(i), pairs);
    }
    
    private SepPair findPairFor(Area sep, List<SepPair> pairs)
    {
        SepPair cand = null;
        int mindist = Integer.MAX_VALUE;
        for (SepPair pair : pairs)
        {
            if (!pair.isComplete() && pair.allowsPair(sep))
            {
                final int dist = pair.candidateDistance(sep);
                if (dist < mindist)
                {
                    cand = pair;
                    mindist = dist;
                }
            }
        }
        return cand;
    }
    
    //==============================================================================
    
    
    
    private class SepPair
    {
        //left or top
        public Area s1;
        //right or bottom 
        public Area s2;
        
        public SepPair(Area s1)
        {
            this.s1 = s1;
            this.s2 = null;
        }
        
        public SepPair(Area s1, Area s2)
        {
            this.s1 = s1;
            this.s2 = s2;
        }
        
        public void addPair(Area s2)
        {
            this.s2 = s2;
        }
        
        public boolean isComplete()
        {
            return (s2 != null);
        }
        
        public boolean allowsPair(Area cand)
        {
            if (s1.isHorizontalSeparator() && cand.isHorizontalSeparator())
            {
                //return s1.getX1() == cand.getX1() && s1.getX2() == cand.getX2();
                return s1.getBounds().intersectsX(cand.getBounds());
            }
            else if (s1.isVerticalSeparator() && cand.isVerticalSeparator())
            {
                //return s1.getY1() == cand.getY1() && s1.getY2() == cand.getY2();
                return s1.getBounds().intersectsY(cand.getBounds());
            }
            else
                return false;
        }
        
        public int candidateDistance(Area cand)
        {
            if (s1.isHorizontalSeparator() && cand.isHorizontalSeparator())
            {
                return cand.getY1() - s1.getY2();
            }
            else if (s1.isVerticalSeparator() && cand.isVerticalSeparator())
            {
                return cand.getX1() - s1.getX2();
            }
            else
                return -1;
        }
        
        @Override
        public String toString()
        {
            return "[" + s1 + " : " + s2 + "]";
        }
        
    }
    
}
