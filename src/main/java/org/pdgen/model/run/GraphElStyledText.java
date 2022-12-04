// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.styledtext.model.QSELine;
import org.pdgen.styledtext.model.StyleRunIterator;

import javax.swing.*;
import java.awt.Graphics2D;

/**
 * User: patrick
 * Date: Aug 14, 2006
 * Time: 8:18:39 AM
 */
public class GraphElStyledText extends GraphElContent
{
    private static final long serialVersionUID = 7L;
    QSELine[] lines;
    float offset;
    CellStyle style;
    StyleRunIterator[] iterators;
    //float[] lineOffsetsX;

    private GraphElStyledText(GraphElStyledText from)
    {
        super(from);
        lines = from.lines;
        offset = from.offset;
        style = from.style;
        iterators = from.iterators; // TODO fishy
    }

    public GraphElStyledText(QSELine[] lines, StyleRunIterator[] iterators, float offset, CellStyle style, CellDef srcCell, ImageIcon backgroundImage)
    {
        super(style.getBackground(), srcCell, backgroundImage);
        this.iterators = iterators;
        this.lines = lines;
        this.style = style;
        this.offset = offset;
    }

    public void setContentX(float xContent, float wEnvelope, float wContent, CellStyle cs, Graphics2D g, float xEnvelope)
    {
        this.wContent = wEnvelope; // Damit nicht nach links geclippt wird Math.min(wContent, wEnvelope);
        this.wEnvelope = wEnvelope;
        this.xContent = xContent;
        this.xEnvelope = xEnvelope;
    }

    public GraphElContent copy()
    {
        return new GraphElStyledText(this);
    }

    public String toString()
    {
        if (lines.length > 0)
            return lines[0] + super.toString();
        else
            return "*empty*" + super.toString();
    }

    public void print(JoriaPrinter pr)
    {
       pr.printGEStyledText(this);
    }
}
