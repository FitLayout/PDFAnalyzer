/**
 * RemapSeparatorsOperator.java
 *
 * Created on 16. 10. 2015, 14:50:00 by burgetr
 */
package org.fit.pdf.op;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.fit.layout.impl.AreaGrid;
import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.Border;
import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;
import org.fit.layout.model.Border.Side;
import org.fit.pdf.PdfArea;
import org.fit.segm.grouping.AreaImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author burgetr
 */
public class RemapSeparatorsOperator extends BaseOperator
{
    private static Logger log = LoggerFactory.getLogger(RemapSeparatorsOperator.class);
    
    protected final String[] paramNames = { "maxEmDistX", "maxEmDistY" };
    protected final ValueType[] paramTypes = { ValueType.FLOAT, ValueType.FLOAT };
    
    private final static short TOP = 0;
    private final static short RIGHT = 1;
    private final static short BOTTOM = 2;
    private final static short LEFT = 3;
    
    private float maxEmDistX = 1.5f;
    private float maxEmDistY = 1.0f;
    
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
    
    public float getMaxEmDistX()
    {
        return maxEmDistX;
    }

    public void setMaxEmDistX(float maxEmDistX)
    {
        this.maxEmDistX = maxEmDistX;
    }

    public float getMaxEmDistY()
    {
        return maxEmDistY;
    }

    public void setMaxEmDistY(float maxEmDistY)
    {
        this.maxEmDistY = maxEmDistY;
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
        atree.updateTopologies();
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
        
        if (area.getId() == 1279)
            System.out.println("hi!");
        
        Area cand;
        cand = findNeigborTop(area);
        if (cand != null && cand.isHorizontalSeparator())
            ret.neighbors[TOP] = cand;
        cand = findNeigborRight(area);
        if (cand != null && cand.isVerticalSeparator())
            ret.neighbors[RIGHT] = cand;
        cand = findNeigborBottom(area);
        if (cand != null && cand.isHorizontalSeparator())
            ret.neighbors[BOTTOM] = cand;
        cand = findNeigborLeft(area);
        if (cand != null && cand.isVerticalSeparator())
            ret.neighbors[LEFT] = cand;
        
        return ret;
    }
    
    private Area findNeigborLeft(AreaImpl area)
    {
        final AreaGrid grid = ((AreaImpl) area.getParentArea()).getGrid();
        final int spos = grid.getColOfs(area.getGridPosition().getX1());
        int y = area.getGridPosition().midY();
        int x = area.getGridPosition().getX1() - 1;
        while (x >= 0)
        {
            final int epos = grid.getColOfs(x);
            if (spos - epos > maxEmDistX * area.getFontSize())
                break; //distance limit exceeded
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
        final int spos = grid.getColOfs(x);
        while (x < grid.getWidth())
        {
            final int epos = grid.getColOfs(x);
            if (epos - spos > maxEmDistX * area.getFontSize())
                break; //distance limit exceeded
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
        final int spos = grid.getRowOfs(area.getGridPosition().getY1());
        int x = area.getGridPosition().midX();
        int y = area.getGridPosition().getY1() - 1;
        while (y >= 0)
        {
            final int epos = grid.getRowOfs(y);
            if (spos - epos > maxEmDistY * area.getFontSize())
                break; //distance limit exceeded
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
        final int spos = grid.getRowOfs(y);
        while (y < grid.getHeight())
        {
            final int epos = grid.getRowOfs(y);
            if (epos - spos > maxEmDistY * area.getFontSize())
                break; //distance limit exceeded
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
                    coverVerticalSeparator(sep, a);
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
                    coverHorizontalSeparator(sep, a);
                    if (max < a.getX2())
                        max = a.getX2();
                }
            }
            
        }
        else
            System.out.println("  NOT covered at all");
    }
    
    private void coverVerticalSeparator(Area sep, Area a)
    {
        if (a.getParentArea() != null)
        {
            final Rectangular sgp = sep.getBounds();
            if (a.getX1() >= sgp.getX1())
            {
                PdfArea parea;
                if (a.getParentArea() instanceof PdfArea && mayShareY(a.getParentArea(), a))
                {
                    parea = (PdfArea) a.getParentArea();
                    parea.getBounds().setX1(sgp.getX1());
                }
                else
                {
                    Rectangular newgp = new Rectangular(a.getBounds());
                    newgp.setX1(sgp.getX1());
                    parea = new PdfArea(newgp);
                    a.getParentArea().insertParent(parea, a);
                }
                parea.setLeftBorder(sgp.getWidth());
                parea.setBorderStyle(Border.Side.LEFT, getSeparatorStyle(sep));
            }
            else
            {
                PdfArea parea;
                if (a.getParentArea() instanceof PdfArea && mayShareY(a.getParentArea(), a))
                {
                    parea = (PdfArea) a.getParentArea();
                    parea.getBounds().setX2(sgp.getX2());
                }
                else
                {
                    Rectangular newgp = new Rectangular(a.getBounds());
                    newgp.setX2(sgp.getX2());
                    parea = new PdfArea(newgp);
                    a.getParentArea().insertParent(parea, a);
                }
                parea.setRightBorder(sgp.getWidth());
                parea.setBorderStyle(Border.Side.RIGHT, getSeparatorStyle(sep));
            }
        }
    }
    
    private void coverHorizontalSeparator(Area sep, Area a)
    {
        if (a.getParentArea() != null)
        {
            final Rectangular sgp = sep.getBounds();
            if (a.getY1() >= sgp.getY1())
            {
                PdfArea parea;
                if (a.getParentArea() instanceof PdfArea && mayShareX(a.getParentArea(), a))
                {
                    parea = (PdfArea) a.getParentArea();
                    parea.getBounds().setY1(sgp.getY1());
                }
                else
                {
                    Rectangular newgp = new Rectangular(a.getBounds());
                    newgp.setY1(sgp.getY1());
                    parea = new PdfArea(newgp);
                    a.getParentArea().insertParent(parea, a);
                }
                parea.setTopBorder(sgp.getHeight());
                parea.setBorderStyle(Border.Side.TOP, getSeparatorStyle(sep));
            }
            else
            {
                PdfArea parea;
                if (a.getParentArea() instanceof PdfArea && mayShareX(a.getParentArea(), a))
                {
                    parea = (PdfArea) a.getParentArea();
                    parea.getBounds().setY2(sgp.getY2());
                }
                else
                {
                    Rectangular newgp = new Rectangular(a.getBounds());
                    newgp.setY2(sgp.getY2());
                    parea = new PdfArea(newgp);
                    a.getParentArea().insertParent(parea, a);
                }
                parea.setBottomBorder(sgp.getHeight());
                parea.setBorderStyle(Border.Side.BOTTOM, getSeparatorStyle(sep));
            }
        }
    }
    
    private Border getSeparatorStyle(Area sep)
    {
        final Vector<Box> boxes = sep.getBoxes();
        if (boxes.size() == 1)
        {
            final Box sbox = boxes.firstElement();
            if (sep.isVerticalSeparator())
            {
                Border ret = sbox.getBorderStyle(Side.LEFT);
                if (ret == null || ret.getStyle() == Border.Style.NONE)
                    ret = sbox.getBorderStyle(Side.RIGHT);
                return ret;
            }
            else
            {
                Border ret = sbox.getBorderStyle(Side.TOP);
                if (ret == null || ret.getStyle() == Border.Style.NONE)
                    ret = sbox.getBorderStyle(Side.BOTTOM);
                return ret;
            }
        }
        else
        {
            log.warn("getSeparatorColor(): Found a separator with strange number of boxes: {}", sep);
            return null;
        }
    }
    
    private boolean mayShareX(Area a1, Area a2)
    {
        //return a1.getX1() == a2.getX1() && a1.getX2() == a2.getX2();
        return a1.getBounds().intersects(a2.getBounds());
    }
    
    private boolean mayShareY(Area a1, Area a2)
    {
        //return a1.getY1() == a2.getY1() && a1.getY2() == a2.getY2();
        return a1.getBounds().intersects(a2.getBounds());
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
