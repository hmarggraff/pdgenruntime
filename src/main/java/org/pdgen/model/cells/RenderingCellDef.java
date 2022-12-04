// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.run.*;
import org.pdgen.model.run.RtfOutput.OutputData;
import org.pdgen.data.JoriaDataException;

public interface RenderingCellDef
{
	/**
	 * create graphic element and set b.wContent
	 * @param b the current cell requirements
	 * @param fillPagedFrame  target
	 * @param iter element index in a collection
	 * @param envelopeWidth available width
	 */
	void render(final TableBorderRequirements b, final FillPagedFrame fillPagedFrame, final int iter, final float envelopeWidth);

	void outputToRTF(final RtfOutput rtfOutput, final OutputData data, final RVAny value, final float colWidth) throws JoriaDataException;

	void outputToHtml(final HtmlOutput2 htmlOutput2, final RunRangeBase rr, final RVAny value);

	float getRunHeight(TableBorderRequirements b, int iter, float remainingHeight, float contentWidth);

}
