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
    
    private final static short TOP = 0;
    private final static short RIGHT = 1;
    private final static short BOTTOM = 2;
    private final static short LEFT = 3;
 
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
        List<Area> newAreas = createAreasFromPairs(pairs);
        while (findJoinableAreas(newAreas)) { }
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
            {
                //expair.addPair(root);
                SepPair copy = new SepPair(expair.s1);
                copy.addPair(root);
                pairs.add(copy);
            }
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
                    cand.add(pair);
                }
            }
        }
        return cand;
    }
    
    //==============================================================================
    
    private List<Area> createAreasFromPairs(List<SepPair> pairs)
    {
        List<Area> ret = new Vector<Area>();
        for (SepPair pair : pairs)
        {
            //scan the horizontal separators
            if (pair.isComplete() && pair.isHorizontal())
            {
                Area parent = findCommonParent(pair.s1, pair.s2);
                if (parent != null)
                {
                    //minimal area given by the separatos (when not aligned)
                    Rectangular areagp = pair.getMinAreaGP(); 
                    Rectangular areabounds = pair.getMinArea();
                    //check for a matching vertical pair
                    SepPair vpair = findMatchingCompleteVerticalPair(pairs, pair);
                    if (vpair != null)
                    {
                        final Rectangular vgp = vpair.getMinAreaGP();
                        if (vgp.getX1() > areagp.getX1())
                            areagp.setX1(vgp.getX1());
                        if (vgp.getX2() < areagp.getX2())
                            areagp.setX2(vgp.getX2());
                        
                        final Rectangular vbounds = vpair.getMinArea();
                        if (vbounds.getX1() > areabounds.getX1())
                            areabounds.setX1(vbounds.getX1());
                        if (vbounds.getX2() < areabounds.getX2())
                            areabounds.setX2(vbounds.getX2());
                    }
                    else //no complete matching pair found, try an incomplete one on the left or right
                    {
                        vpair = findMathingIncompleteVerticalPairLeft(pairs, pair);
                        if (vpair != null)
                        {
                            final Rectangular vgp = vpair.s1.getTopology().getPosition();
                            if (vgp.getX1() + 1 > areagp.getX1())
                                areagp.setX1(vgp.getX1() + 1);
                            final Rectangular vbounds = vpair.s1.getBounds();
                            if (vbounds.getX1() + 1 > areabounds.getX1())
                                areabounds.setX1(vbounds.getX1() + 1);
                        }
                        vpair = findMathingIncompleteVerticalPairRight(pairs, pair);
                        if (vpair != null)
                        {
                            final Rectangular vgp = vpair.s1.getTopology().getPosition();
                            if (vgp.getX2() - 1 < areagp.getX2())
                                areagp.setX2(vgp.getX2() - 1);
                            final Rectangular vbounds = vpair.s1.getBounds();
                            if (vbounds.getX2() - 1 < areabounds.getX2())
                                areabounds.setX2(vbounds.getX2() - 1);
                        }
                    }
                    //find the areas inside
                    if (areabounds.getWidth() > 0 && areabounds.getHeight() > 0) //if the discovered area is not zero-sized
                    {
                        Vector<Area> selected = new Vector<Area>();
                        for (int i = 0; i < parent.getChildCount(); i++)
                        {
                            final Area child = parent.getChildArea(i);
                            final Rectangular cbounds = child.getBounds(); 
                            if (isBetweenSeparators(areabounds, cbounds, child.isSeparator()))
                            {
                                selected.add(child);
                                //crop the child to the area bounds
                                cbounds.copy(cbounds.intersection(areabounds));
                            }
                        }
                        Area newArea = parent.createSuperArea(areagp, selected, "<areaS>");
                        ret.add(newArea);
                    }
                }
                else
                    log.error("Separator pair {} has no common parent", pair);
            }
        }
        return ret;
    }
    
    private Area findCommonParent(Area a1, Area a2)
    {
        if (a1.getParentArea() == a2.getParentArea())
            return a1.getParentArea();
        else
            return null;
    }
    
    private boolean isBetweenSeparators(Rectangular sepBounds, Rectangular childBounds, boolean isSep)
    {
        if (!isSep) //not a separator, allow partial overlaps
        {
            if (sepBounds.encloses(childBounds))
            {
                //child entirely between separators
                return true;
            }
            else
            {
                //at least half of the child between separators
                Rectangular intr = sepBounds.intersection(childBounds);
                return (intr.getArea() > childBounds.getArea() / 2);
            }
        }
        else //a separator - must be fully inside
        {
            Rectangular inner = new Rectangular(sepBounds.getX1() + 1,
                                                sepBounds.getY1() + 1,
                                                sepBounds.getX2() - 1,
                                                sepBounds.getY2() - 1);
            return inner.encloses(childBounds);
        }
    }
    
    private SepPair findMatchingCompleteVerticalPair(List<SepPair> pairs, SepPair hpair)
    {
        final Rectangular hgp = hpair.getMinArea();
        //find the pair with the largest overlapping area
        SepPair cand = null;
        int candarea = 0;
        for (SepPair vpair : pairs)
        {
            if (vpair.isComplete() && vpair.isVertical())
            {
                final Rectangular vgp = vpair.getMinArea();
                if (vgp.intersects(hgp))
                {
                    Rectangular common = vgp.intersection(hgp);
                    if (common.getArea() > candarea)
                    {
                        cand = vpair;
                        candarea = common.getArea();
                    }
                }
            }
        }
        //do not consider the match when the overlapping area is to small
        if (cand != null)
        {
            float ratio = (float) candarea / hgp.getArea();
            if (ratio < 0.8f)
                cand = null;
        }
        return cand;
    }
    
    private SepPair findMathingIncompleteVerticalPairLeft(List<SepPair> pairs, SepPair hpair)
    {
        final Rectangular hgp = hpair.getMinArea();
        final int leftEdge = hgp.getX1();
        for (SepPair vpair : pairs)
        {
            if (!vpair.isComplete() && vpair.isVertical())
            {
                final Rectangular vgp = vpair.s1.getBounds();
                if (vgp.getX1() <= leftEdge && vgp.getX2() >= leftEdge)
                    return vpair;
            }
        }
        return null;
    }
    
    private SepPair findMathingIncompleteVerticalPairRight(List<SepPair> pairs, SepPair hpair)
    {
        final Rectangular hgp = hpair.getMinArea();
        final int rightEdge = hgp.getX2();
        for (SepPair vpair : pairs)
        {
            if (!vpair.isComplete() && vpair.isVertical())
            {
                final Rectangular vgp = vpair.s1.getBounds();
                if (vgp.getX1() <= rightEdge && vgp.getX2() >= rightEdge)
                    return vpair;
            }
        }
        return null;
    }
    //==============================================================================
    
    /**
     * Tries to find non-separated neighboring areas that may be joined
     * and joins them.
     * @param areas The list of areas to process.
     * @return {@code true} when some change has been performed
     */
    private boolean findJoinableAreas(List<Area> areas)
    {
        final int[] dirs = { RIGHT, BOTTOM };
        for (Area curArea : areas)
        {
            List<Area> siblings = curArea.getParentArea().getChildAreas();
            for (int di = 0; di < 2; di++)
            {
                Area cand = findNeighbor(curArea, siblings, dirs[di], 0);
                if (cand == null)
                    cand = findNeighbor(curArea, siblings, dirs[di], 1);
                if (cand != null && !cand.isSeparator() && areas.contains(cand)) //candidate found: join
                {
                    //join the areas
                    curArea.getBounds().expandToEnclose(cand.getBounds());
                    curArea.appendChildren(cand.getChildAreas());
                    cand.getParentArea().removeChild(cand);
                    areas.remove(cand);
                    return true;
                }
            }
        }
        return false;
    }
    
    private Area findNeighbor(Area area, List<Area> list, int dir, int dist)
    {
        final Rectangular ab = area.getBounds();
        //where do we search the candidate?
        int wx = 0, wy = 0;
        switch (dir)
        {
            case TOP:
                wx = ab.midX(); wy = ab.getY1() - dist;
                break;
            case RIGHT:
                wx = ab.getX2() + dist; wy = ab.midY();
                break;
            case BOTTOM:
                wx = ab.midX(); wy = ab.getY2() + dist;
                break;
            case LEFT:
                wx = ab.getX1() - dist; wy = ab.midY();
                break;
        }
        //look for the candidate
        for (Area cur : list)
        {
            if (cur != area && cur.getBounds().contains(wx, wy))
                return cur;
        }
        return null;
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
        
        public boolean isHorizontal()
        {
            return s1.isHorizontalSeparator();
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
        
        /**
         * Obtains the grid position of the area between the separators.
         * When the separators are not aligned, the minimal area is returned determined by the seam length.
         * @return
         */
        public Rectangular getMinAreaGP()
        {
            final Rectangular sgp1 = s1.getTopology().getPosition();
            final Rectangular sgp2 = s2.getTopology().getPosition();
            if (s1.isHorizontalSeparator())
                return new Rectangular(Math.max(sgp1.getX1(), sgp2.getX1()), sgp1.getY2() + 1,
                                       Math.min(sgp1.getX2(), sgp2.getX2()), sgp2.getY1() - 1);
            else
                return new Rectangular(sgp1.getX2() + 1, Math.max(sgp1.getY1(), sgp2.getY1()),
                                       sgp2.getX1() - 1, Math.min(sgp1.getY2(), sgp2.getY2()));
        }
        
        /**
         * Obtains the absolute position of the area between the separators.
         * When the separators are not aligned, the minimal area is returned determined by the seam length.
         * @return
         */
        public Rectangular getMinArea()
        {
            final Rectangular sgp1 = s1.getBounds();
            final Rectangular sgp2 = s2.getBounds();
            if (s1.isHorizontalSeparator())
                return new Rectangular(Math.max(sgp1.getX1(), sgp2.getX1()), sgp1.getY2() + 1,
                                       Math.min(sgp1.getX2(), sgp2.getX2()), sgp2.getY1() - 1);
            else
                return new Rectangular(sgp1.getX2() + 1, Math.max(sgp1.getY1(), sgp2.getY1()),
                                       sgp2.getX1() - 1, Math.min(sgp1.getY2(), sgp2.getY2()));
        }
        
        @Override
        public String toString()
        {
            return "[" + s1 + " : " + s2 + "]";
        }
        
    }
    
}
