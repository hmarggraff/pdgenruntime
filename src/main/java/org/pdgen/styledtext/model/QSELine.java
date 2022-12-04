// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.styledtext.model;

import org.pdgen.util.IntPairChain;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;

public class QSELine
{
	public float yPar;
	public float yCell;
	public int startCharIndex;
	public StyledParagraph text;
	public TextLayout line;
	private final int paraStartIndex;
	public float x;
	IntPairChain searchHighLights;

	public QSELine(StyledParagraph text, TextLayout l, float yPar, float yCell, int startCharIndex, int paraStartIndex)
	{
		this.text = text;
		this.startCharIndex = startCharIndex;
		line = l;
		this.paraStartIndex = paraStartIndex;
		this.yPar = yPar;
		this.yCell = yCell;
	}

	public float getHeight()
	{
		return line.getAscent() + line.getDescent() + line.getLeading();
	}

	public float getAscent()
	{
		return line.getAscent();
	}

	public float getBottom()
	{
		return line.getDescent() + line.getLeading();
	}

	public TextHitInfo hitTestChar(float x, float y)
	{
		return line.hitTestChar(x, y - yPar);
	}

	public boolean containsY(float y)
	{
		return y > yPar && y <= yPar + getHeight();
	}

	public void draw(Graphics2D g, float x, float y)
	{
		IntPairChain match = searchHighLights;
		while (match != null)
		{
			int la = Math.max(match.a, 0);
			int le = Math.min(match.b, line.getCharacterCount());
			Shape highlightShape = line.getLogicalHighlightShape(la, le);
			AffineTransform tr = AffineTransform.getTranslateInstance(x, y);
			Shape trHighlight = tr.createTransformedShape(highlightShape);
			g.setColor(new Color(220, 220, 255));
			g.fill(trHighlight);
			g.setColor(Color.black);
			match = match.next;
		}
		line.draw(g, x, y);
	}

	public int endCharIndex()
	{
		return startCharIndex + line.getCharacterCount();
	}

	public float getWidth()
	{
		return line.getAdvance();
	}

	public String toString()
	{
		return text.get(startCharIndex - paraStartIndex, line.getCharacterCount());
	}

	public boolean isLastLineOfParagraph()
	{
		return startCharIndex - paraStartIndex + line.getCharacterCount() == text.length();
	}

	public StyledParagraph getText()
	{
		return text;
	}

	public void clearHighlites()
	{
		searchHighLights = null;
	}

	public void highlightText(String s)
	{
		int lineOffset = startCharIndex - paraStartIndex;
		int charPos = lineOffset;
		int endChar = charPos + line.getCharacterCount();// end of line
		searchHighLights = null;
		IntPairChain match = null;
		for (; ;)
		{
			int at = text.indexOf(s, charPos);
			if (at < 0 || at >= endChar)
				break;
			IntPairChain tmatch = new IntPairChain(at-lineOffset, at + s.length()-lineOffset);
			if (match == null)// first
			{
				searchHighLights = tmatch;
			}
			else
			{
				match.next = tmatch;
			}
			match = tmatch;
			charPos = match.b + lineOffset;
		}
	}
}
