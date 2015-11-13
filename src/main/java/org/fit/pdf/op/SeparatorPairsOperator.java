/**
 * SeparatorPairsOperator.java
 *
 * Created on 16. 10. 2015, 14:50:00 by burgetr
 */
package org.fit.pdf.op;

import java.util.List;
import java.util.Vector;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.Rectangular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author burgetr
 */
public class SeparatorPairsOperator extends BaseOperator
{
    private static Logger log = LoggerFactory.getLogger(SeparatorPairsOperator.class);
    
    protected final String[] paramNames = { };
    protected final ValueType[] paramTypes = { };
    
    public SeparatorPairsOperator()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Pdf.SeparatorPairs";
    }
    
    @Override
    public String getName()
    {
        return "Separator Pairs";
    }

    @Override
    public String getDescription()
    {
        return "Detects the pairs of vertical or horizontal separators and creates new areas between them.";
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
        /*System.out.println("Incomplete:");
        for (SepPair pair : pairs)
            if (!pair.isComplete())
                System.out.println("  " + pair);*/
        createAreasFromPairs(pairs);
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
            final List<SepPair> expairs = findPairsFor(root, pairs);
            for (SepPair expair : expairs)
                expair.addPair(root);
            //and create a new one
            pairs.add(new SepPair(root));
        }
        for (int i = 0; i < root.getChildCount(); i++)
            recursivelyFindPairs(root.getChildArea(i), pairs);
    }
    
    private List<SepPair> findPairsFor(Area sep, List<SepPair> pairs)
    {
        List<SepPair> cand = new Vector<SepPair>();
        int mindist = Integer.MAX_VALUE;
        //find the minimal distance
        for (SepPair pair : pairs)
        {
            if (pair.allowsPair(sep))
            {
                final int dist = pair.candidateDistance(sep);
                if (dist < mindist)
                    mindist = dist;
            }
        }
        //try find pairs in the minimal distance
        for (SepPair pair : pairs)
        {
            if (pair.allowsPair(sep))
            {
                final int dist = pair.candidateDistance(sep);
                if (dist == mindist && !pair.isComplete())
                {
                    mindist = dist;
                    if (!pair.isComplete())
                        cand.add(pair);
                    else
                        cand.add(new SepPair(pair.s1));
                }
            }
        }
        return cand;
    }
    
    //==============================================================================
    
    private void createAreasFromPairs(List<SepPair> pairs)
    {
        for (SepPair pair : pairs)
        {
            //scan the horizontal separators
            if (pair.isComplete() && !pair.isVertical())
            {
                Area parent = findCommonParent(pair.s1, pair.s2);
                if (parent != null)
                {
                    final Rectangular sgp1 = pair.s1.getTopology().getPosition();
                    final Rectangular sgp2 = pair.s2.getTopology().getPosition();
                    //minimal area given by the separatos (when not aligned)
                    Rectangular mingp = new Rectangular(Math.max(sgp1.getX1(), sgp2.getX1()), sgp1.getY1(),
                                                        Math.min(sgp1.getX2(), sgp2.getX2()), sgp2.getY2());
                    //find the areas inside
                    Rectangular selgp = new Rectangular(mingp);
                    Vector<Area> selected = new Vector<Area>();
                    for (int i = 0; i < parent.getChildCount(); i++)
                    {
                        final Area child = parent.getChildArea(i);
                        final Rectangular cgp = child.getTopology().getPosition(); 
                        if (isBetweenSeparators(mingp, cgp) && !child.isSeparator())
                        {
                            selected.add(child);
                            if (selgp == null)
                                selgp = new Rectangular(cgp);
                            else
                                selgp.expandToEnclose(cgp);
                        }
                    }
                    parent.createSuperArea(selgp, selected, "seps");
                }
                else
                    log.error("Separator pair {} has no common parent", pair);
            }
        }
    }
    
    private Area findCommonParent(Area a1, Area a2)
    {
        if (a1.getParentArea() == a2.getParentArea())
            return a1.getParentArea();
        else
            return null;
    }
    
    private boolean isBetweenSeparators(Rectangular mingp, Rectangular childgp)
    {
        if (mingp.encloses(childgp))
        {
            //child entirely between separators
            return true;
        }
        else
        {
            //at least half of the child between separators
            Rectangular intr = mingp.intersection(childgp);
            return (intr.getArea() > childgp.getArea() / 2);
        }
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
        
        public void addPair(Area s2)
        {
            this.s2 = s2;
        }
        
        public boolean isComplete()
        {
            return (s2 != null);
        }
        
        public boolean isVertical()
        {
            return s1.isVerticalSeparator();
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
