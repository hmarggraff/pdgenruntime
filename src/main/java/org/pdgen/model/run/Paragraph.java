// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;

import java.awt.Graphics2D;

/**
 * User: patrick
 * Date: Aug 14, 2006
 * Time: 12:56:07 PM
 */
public interface Paragraph
{
    void backupSlice();

    int getRemainingSpan();   // TODO dieser müsste angepasst werden.

    float nextSlice(TableBorderRequirements b, float height, float x, float y, float w, Graphics2D g, boolean fixedHeight, FillPagedFrame fpf);

    CellDef getUnSplit();

    boolean more();

    float getContentWidth();

    float getContentX();

    float getContentY();

    float getEnvelopeWidth();

    float getInnerX();

    float getInnerY();

	float roundUp(float smoothColHeight);

	void saveSliceState();
	void restoreSliceState();
}
