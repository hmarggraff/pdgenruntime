// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.styledtext.model.QSELine;
import org.pdgen.styledtext.model.StyleRunIterator;
import org.pdgen.styledtext.model.StyledParagraphLayouterList;
import org.pdgen.styledtext.model.StyledParagraphList;

import java.awt.Graphics2D;

/**
 * User: patrick
 * Date: Aug 14, 2006
 * Time: 1:13:04 PM
 */
public class StyledTextParagraph implements Paragraph
{
	public int remainingSpan;
	private final CellDef cd;
	private final QSELine[] lines;
    private final StyleRunIterator[] iterators;
	private int currentLine;
	private int lastSliceLine;
	private int savedSliceLine;
	private float contentX;
	private float contentY;
	private float contentWidth;
	private float innerX;
	private float innerY;
	private float hDiff;
	private float wDiff;
	private float ew;

    public void backupSlice()
	{
		currentLine = lastSliceLine;
	}

	public int getRemainingSpan()
	{
		return remainingSpan;
	}

	public float nextSlice(TableBorderRequirements b, float height, float x, float y, float w, Graphics2D g, boolean fixedHeight, FillPagedFrame fpf)
	{
        CellStyle cs = cd.getCascadedStyle();
        height -= b.hDiff;
        float currentHeight = -lines[currentLine].yCell;
		int newLines = currentLine;
        for (int i = currentLine; i < lines.length; i++)
        {
            if (currentHeight + lines[i].yCell + lines[i].getHeight() > height)
                break;
            newLines++;
        }
        if(!cs.getBreakable() || fixedHeight)
        {
            if(newLines < lines.length && !fixedHeight)
            {
                b.hContent = 0;
                return 0;
            }
            else
            {
                newLines = lines.length;
            }
        }
        else
        {
            if (newLines == currentLine)
            {
                b.hContent = 0;
                return 0;
            }
        }
        currentHeight += lines[newLines-1].yCell + lines[newLines-1].getHeight();

        b.hContent = currentHeight;
		QSELine[] partLines = new QSELine[newLines - currentLine];
		StyleRunIterator[] partIterators = new StyleRunIterator[newLines - currentLine];
		System.arraycopy(lines, currentLine, partLines, 0, newLines - currentLine);
        float moveOffset = partLines[0].yCell;
        System.arraycopy(iterators, currentLine, partIterators, 0, newLines - currentLine);
		b.wContent = 0;
        for (QSELine l : partLines)
        {
            b.wContent = Math.max(b.wContent, l.getWidth());
        }
		b.grel = new GraphElStyledText(partLines, partIterators, moveOffset, cd.getCascadedStyle(), cd, cs.getBackgroundImage(fpf.getRunEnv().getLocale()));
		b.grel.drilldownObject = b.drillDownObject;
		if (hDiff != 0)
			b.hDiff = hDiff;
		if (wDiff != 0)
			b.wDiff = wDiff;
		lastSliceLine = currentLine;
		currentLine = newLines;
		return currentHeight;
	}

	public CellDef getUnSplit()
	{
		return cd;
	}

	public boolean more()
	{
		return currentLine  < lines.length;
	}

	public float getContentWidth()
	{
		return contentWidth;
	}

	public float getContentX()
	{
		return contentX;
	}

	public float getContentY()
	{
		return contentY;
	}

	public float getEnvelopeWidth()
	{
		return ew;
	}

	public float getInnerX()
	{
		return innerX;
	}

	public float getInnerY()
	{
		return innerY;
	}

	public float roundUp(float smoothColHeight)
	{
		float yPar = lines[lastSliceLine].yCell;
		float mark = smoothColHeight + yPar;
		for (int i = lastSliceLine+1; i < lines.length; i++)
		{
			QSELine line = lines[i];
			if (line.yCell > mark)
				return line.yCell+1;
		}
		return smoothColHeight+18;
	}

	public void saveSliceState()
	{
		savedSliceLine = currentLine;
	}

	public void restoreSliceState()
	{
		currentLine = savedSliceLine;
	}

	public static StyledTextParagraph makeParagraph(CellDef cd, StyledParagraphList val, float ew, Graphics2D graphics2D)
	{
		return new StyledTextParagraph(cd, val, ew, graphics2D);
	}

	protected StyledTextParagraph(CellDef cd, StyledParagraphList val, float ew, Graphics2D graphics2D)
	{
		this.cd = cd;
		StyledParagraphLayouterList layouter = new StyledParagraphLayouterList(val, graphics2D.getFontRenderContext(), ew);
		layouter.recalc();
		lines = layouter.getLines();
		iterators = layouter.getIteratorsPerLine();
	}

	public void setup(float contentX, float contentY, float contentWidth, float innerX, float innerY, float hDiff, float wDiff, float ew)
	{
		this.contentX = contentX;
		this.contentY = contentY;
		this.contentWidth = contentWidth;
		this.innerX = innerX;
		this.innerY = innerY;
		this.hDiff = hDiff;
		this.wDiff = wDiff;
		this.ew = ew;
	}
}
