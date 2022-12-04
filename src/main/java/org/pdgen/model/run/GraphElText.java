// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;

import javax.swing.*;
import java.io.PrintStream;
import java.awt.*;

public class GraphElText extends GraphElContent
{

    private static final long serialVersionUID = 7L;
    String txt;
    CellStyle style;

    private GraphElText(GraphElText from)
    {
        super(from);
        txt = from.txt;
        style = from.style;
    }

    public GraphElText(String txt, CellStyle style, CellDef src, ImageIcon backgroundImage)
    {
        super(style.getBackground(), src, backgroundImage);
        this.txt = txt;
        this.style = style;
    }

    public void print(JoriaPrinter pr)
    {
        pr.printGEText(this);
    }

    public void dump(PrintStream w)
    {
        w.println("text\t" + x + "\t" + y + "\t" + width + "\t" + height + "\t" + xContent + "\t" + yContent + "\t" + txt.substring(0, Math.min(15, txt.length())));
    }

    public GraphElContent copy()
    {
        return new GraphElText(this);
    }

    public float getContentWidth(Graphics2D g)
    {
        return style.getWidth(txt, g);
    }

    public CellStyle getStyle()
    {
        return style;
    }

    public String getText()
    {
        return txt;
    }

    public String toString()
    {
        return txt + " " + super.toString();
    }
}
