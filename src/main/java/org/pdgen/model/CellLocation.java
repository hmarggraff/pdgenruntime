// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.model.cells.CellDef;

import java.util.Objects;

/**
 * holds the indices of the curent selection in the editor.
 */
public class CellLocation
{
	public static final int NOWHERE = 0;
	public static final int ABOVE = NOWHERE + 1;
	public static final int BELOW = ABOVE + 1;
	public static final int LEFT = BELOW + 1;
	public static final int RIGHT = LEFT + 1;
	public static final int CENTER = RIGHT + 1;
	public static final int FARBELOW = CENTER + 1;// Somewhere in frame below this cell
	public static final int FRAME = FARBELOW + 1;// Somewhere in frame
	static final String[] tNames = {"NONE", "ABOVE", "BELOW", "LEFT", "RIGHT", "CENTER", "FAR BELOW", "FRAME"};
	public TemplateModel model;
	public int col;
	public int row;
	public int relative;// which side of the cell

	public CellLocation(TemplateModel t, int r, int c, int rel)
	{
		model = t;
		col = c;
		row = r;
		relative = rel;
	}

	public CellLocation(TemplateModel t, int r, int c)
	{
		model = t;
		col = c;
		row = r;
		relative = CENTER;
	}

	public CellLocation()
	{
		col = -1;
		row = -1;
		relative = NOWHERE;
	}

	public String toString()
	{
		return "CellLocation(" + col + "," + row + "," + tNames[relative] + ")";
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof CellLocation))
			return false;
		CellLocation cp = (CellLocation) o;
		if (relative == NOWHERE && cp.relative == NOWHERE)
			return true;
		return relative == cp.relative && col == cp.col && row == cp.row && model == cp.model;
	}

	@Override
	public int hashCode() {
		return Objects.hash(relative, col, row, model);
	}

	public boolean isCell()
	{
		return relative == CENTER || relative == ABOVE || relative == BELOW || relative == RIGHT || relative == LEFT;
	}

	public boolean isVertical()
	{
		return relative == ABOVE || relative == BELOW || relative == FARBELOW;
	}

	public boolean isHorizontal()
	{
		return relative == LEFT || relative == RIGHT;
	}

	public CellDef getCell()
	{
		if (!isCell())
			return null;
		return model.cellAt(row, col);
	}
}
