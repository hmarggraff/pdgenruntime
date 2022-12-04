// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.style.FrameStyle;
import org.pdgen.model.style.PageStyle;
import org.pdgen.model.style.StyleBase;
import org.pdgen.model.TemplateModel;

// Created 16.01.2008 by hmf
public class NestedFrameColumnLayouter extends ColumnLayouter
{
	public NestedFrameColumnLayouter(final float[] colWidths, final float[] maxColWidths, final float[] colPos, final float[][] colReq, final int dynaCols, final TemplateModel template, final PageStyle ps)
	{
		super(colWidths, maxColWidths, colPos, colReq, dynaCols, template, ps);
	}

	public float layout(float fPos, float w)
	{
		boolean[] flowerCol = calcColWidths();

		float minWidth = 0;
		float maxWidth = 0;
		for (int cix = 0; cix < dynaCols; cix++)
		{
		    minWidth += colWidths[cix];
		    maxWidth += maxColWidths[cix];
		}

		FrameStyle frameStyle = template.getFrame().getCascadedFrameStyle();

		Integer fill = frameStyle.getFill();
		//------------
		if (maxWidth == w || (!StyleBase.FillBoth.equals(fill) && !StyleBase.FillHorizontal.equals(fill) && !StyleBase.FillSymmetric.equals(fill))) // space matches requirement
		{
		    for (int cix = 0; cix < dynaCols; cix++)
		    {
		        colPos[cix] = fPos;
		        fPos += colWidths[cix];
		    }
		}
		else if (maxWidth <= w) // spread all
		{
		    spread(w, maxWidth, fPos);
		}
		else if (minWidth <= w) // spread flowing cells only
		{
		    if (!spread4Reflow(flowerCol, w, minWidth, fPos)) // no reflowers, just spans
		        spread(w, minWidth, fPos);
		}
		else
		// squeeze
		{
		    squeeze(w, minWidth, fPos);
		}
		colPos[dynaCols] = colPos[dynaCols - 1] + colWidths[dynaCols - 1]; // right end of columns: keeping this makes life easier for frame borders etc.
		return w;

	}
}
