/**
 * HTMLOutputOperator.java
 *
 * Created on 12. 1. 2016, 11:42:43 by burgetr
 */
package org.fit.pdf.op;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Vector;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.Border;
import org.fit.layout.model.Box;
import org.fit.layout.model.Rectangular;
import org.fit.pdf.PdfArea;

/**
 * This operator serializes the area tree to an HTML file.
 * 
 * @author burgetr
 */
public class HTMLOutputOperator extends BaseOperator
{
    /** Should we produce the HTML header and footer? */
    protected boolean produceHeader;
    
    /** Path to the output file/ */
    protected String filename;
    
    protected final String[] paramNames = { "filename", "produceHeader" };
    protected final ValueType[] paramTypes = { ValueType.STRING, ValueType.BOOLEAN };


    
    public HTMLOutputOperator()
    {
        produceHeader = false;
        filename = "out.html";
    }

    public HTMLOutputOperator(String filename, boolean produceHeader)
    {
        this.filename = filename;
        this.produceHeader = produceHeader;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Pdf.HTMLOutput";
    }

    @Override
    public String getName()
    {
        return "HTML serialization of the area tree";
    }

    @Override
    public String getDescription()
    {
        return "Serializes the area tree to an HTML file";
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

    public boolean getProduceHeader()
    {
        return produceHeader;
    }

    public void setProduceHeader(boolean produceHeader)
    {
        this.produceHeader = produceHeader;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    //=====================================================================================================
    
    @Override
    public void apply(AreaTree atree)
    {
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        try
        {
            PrintWriter out = new PrintWriter(filename);
            dumpTo(atree, out);
            out.close();
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't create output HTML file " + filename);
        }
    }

    //=====================================================================================================
    
    /**
     * Formats the complete tag tree to an output stream
     */
    public void dumpTo(AreaTree tree, PrintWriter out)
    {
        if (produceHeader)
        {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>" + tree.getRoot().getPage().getTitle() + "</title>");
            out.println("<meta charset=\"utf-8\">");
            out.println("</head>");
            out.println("<body>");
        }
        recursiveDump(tree.getRoot(), 1, out);
        if (produceHeader)
        {
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    //=====================================================================
    
    private void recursiveDump(Area a, int level, java.io.PrintWriter p)
    {
        String tagName = "div";
        
        String stag = "<" + tagName
                        + " style=\"" + getAreaStyle(a) + "\""
                        + ">";

        String etag = "</" + tagName + ">";
        
        if (a.getChildCount() > 0)
        {
            indent(level, p);
            p.println(stag);
            
            for (int i = 0; i < a.getChildCount(); i++)
                recursiveDump(a.getChildArea(i), level+1, p);
            
            indent(level, p);
            p.println(etag);
        }
        else
        {
            indent(level, p);
            p.println(stag);
            dumpBoxes(a, p, level+1);
            indent(level, p);
            p.println(etag);
        }
        
    }
    
    private void dumpBoxes(Area a, java.io.PrintWriter p, int level)
    {
        Vector<Box> boxes = a.getBoxes();
        for (Box box : boxes)
        {
            indent(level, p);
            String stag = "<span"
                            + " style=\"" + getBoxStyle(a, box) + "\"" 
                            + ">";
            p.print(stag);
            p.print(HTMLEntities(box.getText()));
            p.println("</span>");
        }
    }
    
    protected String getAreaStyle(Area a)
    {
        Area parent = a.getParentArea();
        int px = 0;
        int py = 0;
        if (parent != null)
        {
            px = parent.getX1();
            py = parent.getY1();
            
            Border bleft = parent.getBorderStyle(Border.Side.LEFT);
            if (bleft != null)
                px += bleft.getWidth();
            Border btop = parent.getBorderStyle(Border.Side.TOP);
            if (btop != null)
                py += btop.getWidth();
        }
            
        StringBuilder style = new StringBuilder("position:absolute;");
        style.append("left:").append(a.getX1() - px).append("px;");
        style.append("top:").append(a.getY1() - py).append("px;");
        style.append("width:").append(a.getWidth()).append("px;");
        style.append("height:").append(a.getHeight()).append("px;");
        String bgcol = colorString(a.getBackgroundColor());
        if (!bgcol.isEmpty())
            style.append("background:").append(bgcol).append(';');
        
        return style.toString();
    }
    
    protected String getBoxStyle(Area parent, Box box)
    {
        int px = 0;
        int py = 0;
        if (parent != null)
        {
            px = parent.getX1();
            py = parent.getY1();
            if (parent instanceof PdfArea)
            {
                final PdfArea pa = (PdfArea) parent;
                px += pa.getBorderStyle(Border.Side.LEFT).getWidth();
                py += pa.getBorderStyle(Border.Side.TOP).getWidth();
            }
        }
            
        Rectangular pos = box.getVisualBounds();
        StringBuilder style = new StringBuilder("position:absolute;");
        style.append("top:").append(pos.getY1() - py).append("px;");
        style.append("left:").append(pos.getX1() - px).append("px;");
        style.append("color:").append(colorString(box.getColor())).append(';');
        style.append("font-family:'").append(box.getFontFamily()).append("';");
        style.append("font-size:").append(box.getFontSize()).append("px;");
        style.append("font-weight:").append((box.getFontWeight() < 0.5f)?"normal":"bold").append(";");
        style.append("font-style:").append((box.getFontStyle() < 0.5f)?"normal":"italic").append(";");
        String deco = "";
        if (box.getUnderline() >= 0.5f)
            deco += "underline";
        if (box.getLineThrough() >= 0.5f)
            deco += " line-through";
        if (deco.isEmpty())
            deco = "none";
        style.append("text-decoration:").append(deco).append(';');
        
        return style.toString();
    }
    
    private void indent(int level, java.io.PrintWriter p)
    {
        String ind = "";
        for (int i = 0; i < level*4; i++) ind = ind + ' ';
        p.print(ind);
    }
    
    private String colorString(java.awt.Color color)
    {
        if (color == null)
            return "";
        else
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * Converts the CSS specification rgb(r,g,b) to #rrggbb
     * @param spec the CSS color specification
     * @return a #rrggbb string
     */
    public String colorString(String spec)
    {
        if (spec.startsWith("rgb("))
        {
            String s = spec.substring(4, spec.length() - 1);
            String[] lst = s.split(",");
            try {
                int r = Integer.parseInt(lst[0].trim());
                int g = Integer.parseInt(lst[1].trim());
                int b = Integer.parseInt(lst[2].trim());
                return String.format("#%02x%02x%02x", r, g, b);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        else
            return spec;
    }
    
    private String HTMLEntities(String s)
    {
        return s.replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("&", "&amp;");
    }
    

}
