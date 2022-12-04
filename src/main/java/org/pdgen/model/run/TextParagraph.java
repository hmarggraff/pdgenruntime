// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.NotYetImplementedError;
import org.pdgen.data.Trace;

import java.awt.*;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

public class TextParagraph implements Paragraph
{

	CellDef unSplit;
	int linePos;
	int savedLinePos;
	int lastLinePos;
	String[] lines;
    boolean [] brokenLines;
	float totalHeight;
	float contentX; // border modifier for pos of graphic content
	float contentY; // border modifier for pos of graphic content
	float contentWidth; // border modifier for pos of graphic content
	float innerX; // border modifier for pos of graphic content
	float innerY; // border modifier for pos of graphic content
	float envelopeWidth; // border modifier for pos of graphic content
	float hDiff;
	float wDiff;
	int remainingSpan;

	public TextParagraph(CellDef unSplit, String[] text, Boolean[] brokenLines, float height)
	{
		this.unSplit = unSplit;
		lines = text;
		totalHeight = height;
        if(brokenLines != null)
        {
            this.brokenLines = new boolean[brokenLines.length];
            for(int i = 0; i < brokenLines.length; i++)
                this.brokenLines[i] = brokenLines[i];
        }
	}

	public void setup(float contentX, float contentY, float contentWidth, float innerX, float innerY, float hDiff, float wDiff, float envelopeWidth)
	{
		this.contentX = contentX;
		this.contentY = contentY;
		this.contentWidth = contentWidth;
		this.innerX = innerX;
		this.innerY = innerY;
		this.envelopeWidth = envelopeWidth;
		this.hDiff = hDiff;
		this.wDiff = wDiff;
	}

	public static TextParagraph makeParagraph(CellDef cd, String text, float w, Graphics2D g, Locale locale) // initial slice
	{
		if (text == null || text.length() == 0)
			return null;
		CellStyle cs = cd.getCascadedStyle();
		int tt = cs.getTextType();
		if (cd.isReflowing())
			return reFlow(cd, text, w, g, locale);
		else if (tt == CellStyle.plainType || tt == CellStyle.vertBottomUp || tt == CellStyle.vertTopDown)
		{
			if (text.startsWith("<html>"))
				throw new NotYetImplementedError("html text formatting");
			else if (text.indexOf('\n') < 0 && text.indexOf('\r') < 0)
				return null;
			else
				return multiLine(cd, text);
		}
		else if (tt == CellStyle.htmlType)
			throw new NotYetImplementedError("html text formatting");
		else
			throw new JoriaAssertionError("This text type not implemented " + tt);
	}


	@SuppressWarnings("UnusedDeclaration")
    public int getLinePos()
	{
		return linePos;
	}

	public boolean more()
	{
		return linePos < lines.length;
	}

	public float nextSlice(TableBorderRequirements b, float height, float x, float y, float w, Graphics2D g, boolean fixedHeight, FillPagedFrame fpf)
	{
		CellStyle cs = unSplit.getCascadedStyle();
		float lh = cs.getLineSpacing();
        Float lineSpacingNumber = cs.getLineSpacingNumber();
        if(lineSpacingNumber != null)
            lh *= lineSpacingNumber;
        int n;

        if(!cs.getBreakable() || fixedHeight)
        {
            float possibleLines = (height - b.hDiff) / lh;
            if(possibleLines < lines.length && !fixedHeight) // the cell does not fit
            {
                b.hContent = 0;
                return 0;
            }
            n = lines.length;
        }
        else
        {
            n = Math.min((int) ((height - b.hDiff) / lh), lines.length - linePos);
            if (n <= 0) // no line fit on page
            {
                b.hContent = 0;
                return 0;
            }
        }
        final float sliceHeight = n * lh;
		b.hContent = sliceHeight;
		String[] slice = new String[n];
		System.arraycopy(lines, linePos, slice, 0, n);
        b.wContent = 0;
        BreakIterator bi = BreakIterator.getWordInstance(fpf.getRunEnv().getLocale());
        for (String s : slice)
        {
            b.wContent = Math.max(b.wContent, cs.getWidth(s, g));
        }
        String[][] lineParts = null;
        float[][] partLengths = null;
        float[] lineWidths = null;
        if(cs.getAlignmentHorizontal().isBlock())
        {
            lineParts = new String[n][];
            partLengths = new float[n][];
            lineWidths = new float[n];

            for(int i = 0; i < n; i++)
            {
                String s = slice[i];
                bi.setText(s);
                ArrayList<String> parts = new ArrayList<String>();
                ArrayList<Float> lengths = new ArrayList<Float>();
                int start = bi.first();
                String sub = null;
                if(brokenLines[i])
                {
                    float lineWidth = 0;
                    for(int end = bi.next(); BreakIterator.DONE != end; start = end, end= bi.next())
                    {
                        String part = s.substring(start, end).trim();
                        if(part.length() == 0 && sub != null)
                        {
                            parts.add(sub);
                            float width = cs.getWidth(sub, g);
                            lengths.add(width);
                            lineWidth += width;
                            sub = null;
                        }
                        else if(part.length() > 0)
                        {
                            if(sub == null)
                                sub = part;
                            else
                                sub += part;
                        }
                    }
                    if(sub != null)
                    {
                        parts.add(sub);
                        float width = cs.getWidth(sub, g);
                        lengths.add(width);
                        lineWidth += width;
                    }
                    if(parts.size() > 1)
                    {
                        lineWidths[i] = lineWidth;
                        lineParts[i] = parts.toArray(new String[parts.size()]);
                        partLengths[i] = new float[lengths.size()];
                        for(int j = 0; j < lengths.size(); j++)
                            partLengths[i][j] = lengths.get(j);
                    }
                }
                // check if non broken lines need something
            }
        }
        b.grel = new GraphElTextLines(slice, cs, unSplit, cs.getBackgroundImage(fpf.getRunEnv().getLocale()), lineParts, partLengths, lineWidths);
        b.grel.drilldownObject = b.drillDownObject;
		if (hDiff != 0)
			b.hDiff = hDiff;
		if (wDiff != 0)
			b.wDiff = wDiff;
		lastLinePos = linePos;
		linePos += n;
		return sliceHeight;
	}

	static TextParagraph reFlow(CellDef scd, String txt, float w, Graphics2D g, Locale locale)
	{
		//ArrayList layouts = new ArrayList();
		ArrayList<String> subStrings = new ArrayList<String>();
        ArrayList<Boolean> brokenLine = new ArrayList<Boolean>();
		CellStyle cs = scd.getCascadedStyle();
		int ix = 0;
		int start = 0;
		int end = txt.length();
		ArrayList<String> srcLines = new ArrayList<String>();
		while (ix < end)
		{
			char ch = txt.charAt(ix);
			if (ch == '\n')
			{
				String l = txt.substring(start, ix);
				srcLines.add(l);
				start = ix + 1;
			}
			ix++;
		}
		String l = txt.substring(start, end);
		if (l.length() > 0)
			srcLines.add(l);
		float resultHeight = 0.0f;
        for (String srcLine : srcLines)
        {
            String s = srcLine;
            if (s != null && s.length() == 0)
                s = " ";
	        AttributedString as;
	        if (s != null && s.length() != 0)
            {
                as = new AttributedString(s);
                as.addAttribute(TextAttribute.FONT, cs.getStyledFont());
                if (cs.getUnderlined())
                    as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
            }
            else
            {
                return null;
            }
            AttributedCharacterIterator ati = as.getIterator();
            LineBreakMeasurer lbm = new LineBreakMeasurer(ati, BreakIterator.getLineInstance(locale),g.getFontRenderContext());
            int lastPos = 0;
            while (lbm.getPosition() < ati.getEndIndex())
            {
                TextLayout tl = lbm.nextLayout(w);
                resultHeight += tl.getAscent() + tl.getDescent() + tl.getLeading();
                subStrings.add(s.substring(lastPos, lbm.getPosition()).trim());
                brokenLine.add(lbm.getPosition() < ati.getEndIndex());
                lastPos = lbm.getPosition();
            }
        }
		Trace.logDebug(Trace.layout, "floatHeight = " + resultHeight);
		return new TextParagraph(scd, subStrings.toArray(new String[subStrings.size()]), brokenLine.toArray(new Boolean[brokenLine.size()]), resultHeight);
	}

	static TextParagraph multiLine(CellDef scd, String txt)
	{
		int ix = 0;
		int start = 0;
		int end = txt.length();
		ArrayList<String> srcLines = new ArrayList<String>();
		while (ix < end)
		{
			char ch = txt.charAt(ix);
			if (ch == '\n' || ch == '\r')
			{
				String l = txt.substring(start, ix);
				srcLines.add(l);
				start = ix + 1;
				if (start < end && ch == '\r' && txt.charAt(start) == '\n')
				{	// skip \r\n (windows eol)
					start++;
					ix++;
				}
			}
			ix++;
		}
		String l = txt.substring(start, end);
		if (l.length() > 0)
			srcLines.add(l);
        float height = scd.getCascadedStyle().getLineSpacing() * srcLines.size();
        if(scd.getCascadedStyle().getLineSpacingNumber() != null)
            height = scd.getCascadedStyle().getLineSpacingNumber()* scd.getCascadedStyle().getLineSpacing() * (srcLines.size()-1) + scd.getCascadedStyle().getLineSpacing();
		return new TextParagraph(scd, srcLines.toArray(new String[srcLines.size()]), null, height);
	}

	@SuppressWarnings("UnusedDeclaration")
    public float getTotalHeight()
	{
		return totalHeight;
	}

	public void backupSlice()
	{
		linePos = lastLinePos;
		lastLinePos = 0;
	}

    public CellDef getUnSplit()
    {
        return unSplit;
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
		CellStyle cs = unSplit.getCascadedStyle();
		float lh = cs.getLineSpacing();
		@SuppressWarnings("UnnecessaryLocalVariable")
        float ret = (((int) (smoothColHeight / lh)) + 1) * lh;
		return ret;
	}

	public void saveSliceState()
	{
		savedLinePos = linePos;
	}

	public void restoreSliceState()
	{
		linePos = savedLinePos;
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
        return envelopeWidth;
    }

    public int getRemainingSpan()
    {
        return remainingSpan;
    }
}
