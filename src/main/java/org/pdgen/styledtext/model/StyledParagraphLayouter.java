// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import org.pdgen.data.Trace;

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class holds the transient layout information needed to render a Styled Text
 */
public class StyledParagraphLayouter
{
	private AttributedCharacterIterator acIter;
	ArrayList<QSELine> lines = new ArrayList<QSELine>();
	boolean layoutChanged = true;
	protected StyledParagraph text;
	protected LineBreakMeasurer lineBreak;
	StyledParagraphLayouterList outerLayouter;
	protected float height;
	float y0;// vertical starting point in list of paragraphs relative to cell top
	int startCharPos;

	public StyledParagraphLayouter(StyledParagraph text)
	{
		this.text = text;
	}

	public void paint(Graphics2D g, float x0, float y0, float w, float h)
	{
		float y = y0 + this.y0;
		for (QSELine line : lines)
		{
			//y += line.getHeight()*line.text.getRowSpacing();
			line.draw(g, line.x + x0, y + line.getAscent());
			y += line.getHeight() * line.text.getRowSpacing();
		}
	}

	public void paint(Graphics2D g, float x0, float y0, float w, float h, int mark, int dot)
	{
		float y = y0 + this.y0;
		for (QSELine line : lines)
		{
			//y += line.getHeight()*line.text.getRowSpacing();
			line.draw(g, line.x + x0, y + line.getAscent());
			y += line.getHeight() * line.text.getRowSpacing();
		}
	}

	public void recalc(float y, FontRenderContext frc, float targetWidth, int charCnt)
	{
		startCharPos = charCnt;
		lines.clear();
		y0 = y;
		layoutChanged = false;
		if (text.length() == 0)
		{
			height = text.getSpaceBelow();
			return;
		}
		acIter = text.getIterator();
		if (lineBreak == null)
		{
			lineBreak = new LineBreakMeasurer(acIter, frc);
		}
		final int beginIndex = acIter.getBeginIndex();
		acIter.setIndex(beginIndex);
		lineBreak.setPosition(beginIndex);
		height = 0;
		while (lineBreak.getPosition() < acIter.getEndIndex())
		{
			TextLayout layout = lineBreak.nextLayout(targetWidth);
			if (text.getAlignment() == StyledParagraph.alignJustified && lineBreak.getPosition() < acIter.getEndIndex() - 1)
				layout = layout.getJustifiedLayout(targetWidth);
			QSELine line = new QSELine(text, layout, height, height + y, charCnt, startCharPos);
			lines.add(line);
			height += line.getHeight() * line.getText().getRowSpacing();
			final int lineLength = line.line.getCharacterCount();
			final float advance = line.line.getAdvance();
			if (text.getAlignment() == StyledParagraph.alignCenter)
				line.x = (targetWidth - advance) / 2;
			else if (text.getAlignment() == StyledParagraph.alignRight)
				line.x = (targetWidth - advance);
			else
				line.x = 0;
			charCnt += lineLength;
		}
		height += text.getSpaceBelow();
	}

	public void deleteText(int ix1, int ix2)
	{
		int anf = Math.min(ix1, ix2);
		int end = Math.max(ix1, ix2);
		final int len = end - anf;
		text.delete(anf, len);
		acIter = text.getIterator();
		if (len == 1)
		{
			lineBreak.deleteChar(acIter, anf);
			fireChange();
		}
		else
			forceLayout();
	}

	public boolean isLayoutChanged()
	{
		return layoutChanged;
	}

	public QSELine getLine(int ix)
	{
		final ArrayList<QSELine> list = getLines();
		return list.get(ix);
	}

	public StyledParagraph getText()
	{
		return text;
	}

	public void setText(StyledParagraph text)
	{
		this.text = text;
		forceLayout();
	}

	public int getAlignment()
	{
		return text.getAlignment();
	}

	public void setAlignment(int alignment)
	{
		text.setAlignment(alignment);
		forceLayout();
	}

	public void charInserted(int pos)
	{
		acIter = text.getIterator();
		if (lineBreak != null)// line break can be null, if we had other changes
			lineBreak.insertChar(acIter, pos);
		fireChange();
	}

	public Map<AttributedCharacterIterator.Attribute, Object> getAttributesAt(int caretCharPos)
	{
		Trace.check(acIter);
		final int index = Math.max(Math.min(caretCharPos, acIter.getEndIndex() - 1), acIter.getBeginIndex());
		acIter.setIndex(index);
		//noinspection UnusedDeclaration
		final char c = acIter.current();
		return acIter.getAttributes();
	}

	public int getLineCount()
	{
		return lines.size();
	}

	public float getHeight()
	{
		return height;
	}

	StyleRunIterator getStyleRunIterator()
	{
		return new StyleRunIterator(this);
	}

	public void getLines(ArrayList<QSELine> lines)
	{
		lines.addAll(this.lines);
	}

	public ArrayList<QSELine> getLines()
	{
		return lines;
	}

	public float getY0()
	{
		return y0;
	}

	public void setY0(float y0)
	{
		this.y0 = y0;
	}

	public boolean isInRange(int y)
	{
		y -= y0;
		return (y >= 0 && y < height + text.getSpaceBelow());
	}

	public void forceLayout()
	{
		lineBreak = null;
		fireChange();
	}

	public void fireChange()
	{
		layoutChanged = true;
		if (outerLayouter != null)
			outerLayouter.forceLayout();
	}

	public void forceLayoutQuiet()
	{
		layoutChanged = true;
		lineBreak = null;
	}

	public void addlayoutListener(StyledParagraphLayouterList outerlLayouterList)
	{
		outerLayouter = outerlLayouterList;
	}

	public int getStartCharPos()
	{
		return startCharPos;
	}

	public void setStartCharPos(int startCharPos)
	{
		this.startCharPos = startCharPos;
	}

	public int getEndCharPos()
	{
		return startCharPos + text.length();
	}

	public void clearHighlites()
	{
		for (QSELine line : lines)
		{
		    line.clearHighlites();
		}
		forceLayoutQuiet();
	}

	public void highlightText(String s)
	{
		for (QSELine line : lines)
		{
		    line.highlightText(s);
		}
		//forceLayoutQuiet();
	}
}
