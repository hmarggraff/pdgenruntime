// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import java.awt.Graphics2D;
import java.awt.Point;

public interface GridLayouter
{
	void layoutVertical(float y, Graphics2D g);

	void layoutOuter(GridLayouter inner, Graphics2D g);

	void getCellAt(Point p, CellLocation cl);

	boolean isShowGrid();

	boolean isShowingEmptyCells();

	boolean isShowRepeaters();

	float getScale();

	void noteChange();

	void invalidateLayout();

	PageLayouter getTopContainer();

	GridLayouter getContainer();
}
