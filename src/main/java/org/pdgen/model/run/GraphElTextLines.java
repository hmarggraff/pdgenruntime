// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.HorizontalAlignment;

import javax.swing.*;
import java.awt.*;

public class GraphElTextLines extends GraphElContent
{

    private static final long serialVersionUID = 7L;
    String[] lines;
    String[][] linesParts;
    float [][] partLengths;
    float [][] partOffsets;
    float [] lineWidths;
    CellStyle style;
    float[] lineOffsets;

    private GraphElTextLines(GraphElTextLines from)
    {
        super(from);
        lines = from.lines;
        style = from.style;
        lineOffsets = from.lineOffsets;
        linesParts = from.linesParts;
        partLengths = from.partLengths;
        partOffsets = from.partOffsets;
        lineWidths = from.lineWidths;
    }

    public GraphElTextLines(String[] lines, CellStyle f, CellDef src, ImageIcon backgroundImage, String[][] lp, float[][] pl, float[] lineWidths)
    {
        super(f.getBackground(), src, backgroundImage);
        this.lines = lines;
        style = f;
        linesParts = lp;
        partLengths = pl;
        partOffsets = linesParts == null ? null : new float[linesParts.length][];
        this.lineWidths = lineWidths;
    }

    public void print(JoriaPrinter pr)
    {
        pr.printGETextLines(this);
    }

    public void setContentX(float xContent, float wEnvelope, float wContent, CellStyle cs, Graphics2D g, float xEnvelope)
    {
        this.wContent = wEnvelope; // Damit nicht nach links geclippt wird Math.min(wContent, wEnvelope);
        this.wEnvelope = wEnvelope;
        this.xContent = xContent;
        this.xEnvelope = xEnvelope;
        lineOffsets = new float[lines.length];
        for (int i = 0; i < lines.length; i++)
        {
            String s = lines[i];
            HorizontalAlignment halign = style.getAlignmentHorizontal();
            if(halign.isBlock())
                halign = HorizontalAlignment.LEFT;
            lineOffsets[i] = Math.max(0, (wEnvelope - style.getWidth(s, g))) * halign.getAlign();
            if(linesParts != null && linesParts[i] != null)
            {
                float delta = (wEnvelope - lineWidths[i])/(linesParts[i].length-1);
                float start = 0;
                partOffsets[i] = new float[linesParts[i].length];
                for(int j = 0; j < linesParts[i].length; j++)
                {
                    partOffsets[i][j] = start;
                    start += delta + partLengths[i][j];
                }
            }
        }
    }

    public GraphElContent copy()
    {
        return new GraphElTextLines(this);
    }

    public String toString()
    {
        if (lines.length > 0)
            return lines[0] + super.toString();
        else
            return "*empty*" + super.toString();
    }
}
