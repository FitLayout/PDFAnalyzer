/**
 * RemapSeparatorsOperator.java
 *
 * Created on 16. 10. 2015, 14:50:00 by burgetr
 */
package org.fit.pdf.op;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.fit.layout.impl.AreaGrid;
import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.segm.grouping.AreaImpl;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 * @author burgetr
 */
public class RemapSeparatorsOperator extends BaseOperator
{
    protected final String[] paramNames = { };
    protected final ValueType[] paramTypes = { };
    
    private final static short TOP = 0;
    private final static short RIGHT = 1;
    private final static short BOTTOM = 2;
    private final static short LEFT = 3;
    
    private Map<Area, List<Area>> covering;
    
    public RemapSeparatorsOperator()
    {
        covering = new HashMap<Area, List<Area>>();
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
        recursiveMapSeparators((AreaImpl) root);
        sortCoverings();
        for (Area sep : covering.keySet())
            checkCovering(sep);
    }
    
    //==============================================================================
    
    private void recursiveMapSeparators(AreaImpl root)
    {
        if (root.getParentArea() != null && !root.isSeparator())
        {
            Neighborhood neigh = findSeparatorsAround(root);
            addCoverings(neigh);
        }
        
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveMapSeparators((AreaImpl) root.getChildArea(i));
    }
    
    private Neighborhood findSeparatorsAround(AreaImpl area)
    {
        Neighborhood ret = new Neighborhood(area);
        
        Area cand;
        cand = findNeigborTop(area);
        if (cand != null && cand.isHorizontalSeparator())
            ret.neighbors[TOP] = cand;
        cand = findNeigborRight(area);
        if (cand != null && cand.isVerticalSeparator())
            ret.neighbors[LEFT] = cand;
        cand = findNeigborBottom(area);
        if (cand != null && cand.isHorizontalSeparator())
            ret.neighbors[BOTTOM] = cand;
        cand = findNeigborLeft(area);
        if (cand != null && cand.isVerticalSeparator())
            ret.neighbors[RIGHT] = cand;
        
        return ret;
    }
    
    private Area findNeigborLeft(AreaImpl area)
    {
        final AreaGrid grid = ((AreaImpl) area.getParentArea()).getGrid();
        int y = area.getGridPosition().midY();
        int x = area.getGridPosition().getX1() - 1;
        while (x >= 0)
        {
            final Area cand = grid.getAreaAt(x, y);
            if (cand != null)
                return cand;
            x--;
        }
        return null;
    }
        
    private Area findNeigborRight(AreaImpl area)
    {
        final AreaGrid grid = ((AreaImpl) area.getParentArea()).getGrid();
        int y = area.getGridPosition().midY();
        int x = area.getGridPosition().getX2() + 1;
        while (x < grid.getWidth())
        {
            final Area cand = grid.getAreaAt(x, y);
            if (cand != null)
                return cand;
            x++;
        }
        return null;
    }
        
    private Area findNeigborTop(AreaImpl area)
    {
        final AreaGrid grid = ((AreaImpl) area.getParentArea()).getGrid();
        int x = area.getGridPosition().midX();
        int y = area.getGridPosition().getY1() - 1;
        while (y >= 0)
        {
            final Area cand = grid.getAreaAt(x, y);
            if (cand != null)
                return cand;
            y--;
        }
        return null;
    }
        
    private Area findNeigborBottom(AreaImpl area)
    {
        final AreaGrid grid = ((AreaImpl) area.getParentArea()).getGrid();
        int x = area.getGridPosition().midX();
        int y = area.getGridPosition().getY2() + 1;
        while (y < grid.getHeight())
        {
            final Area cand = grid.getAreaAt(x, y);
            if (cand != null)
                return cand;
            y++;
        }
        return null;
    }
    
    private void addCovering(Area sep, Area area)
    {
        List<Area> list = covering.get(sep);
        if (list == null)
        {
            list = new Vector<Area>();
            covering.put(sep, list);
        }
        list.add(area);
    }
    
    private void addCoverings(Neighborhood neigh)
    {
        for (int i = 0; i < 4; i++)
        {
            if (neigh.neighbors[i] != null)
                addCovering(neigh.neighbors[i], neigh.area);
        }
    }
    
    //==============================================================================
    
    private void checkCovering(Area sep)
    {
        System.out.println("SEPARATOR " + sep);
        List<Area> areas = covering.get(sep);
        if (areas != null && !areas.isEmpty())
        {
            if (sep.isVerticalSeparator())
            {
                int max = sep.getY1();
                for (Area a : areas)
                {
                    final int next = a.getY1();
                    if (next > max)
                    {
                        System.out.println("  Uncovered Y: " + max + ".." + next);
                    }
                    if (max < a.getY2())
                        max = a.getY2();
                }
            }
            else //is horizontal
            {
                int max = sep.getX1();
                for (Area a : areas)
                {
                    final int next = a.getX1();
                    if (next > max)
                    {
                        System.out.println("  Uncovered X: " + max + ".." + next);
                    }
                    if (max < a.getX2())
                        max = a.getX2();
                }
            }
            
        }
        else
            System.out.println("  NOT covered at all");
    }
    
    private void sortCoverings()
    {
        for (Map.Entry<Area, List<Area>> entry : covering.entrySet())
        {
            if (entry.getKey().isVerticalSeparator())
            {
                Collections.sort(entry.getValue(), new Comparator<Area>()
                {
                    @Override
                    public int compare(Area o1, Area o2)
                    {
                        return o1.getY1() - o2.getY1();
                    }
                });
            }
            else //is horizontal
            {
                Collections.sort(entry.getValue(), new Comparator<Area>()
                {
                    @Override
                    public int compare(Area o1, Area o2)
                    {
                        return o1.getX1() - o2.getX1();
                    }
                });
            }
        }
    }
    
    //==============================================================================

    class Neighborhood
    {
        public Area area;
        public Area[] neighbors;
        
        public Neighborhood(Area area)
        {
            this.area = area;
            this.neighbors = new Area[]{null, null, null, null};
        }
    }
    
}
