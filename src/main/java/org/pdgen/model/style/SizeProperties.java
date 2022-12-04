// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;
import org.pdgen.model.cells.CellDef;

import java.io.Serializable;


public class SizeProperties implements Serializable
{
    private static final long serialVersionUID = 7L;
    protected FlexSize row;
	protected FlexSize col;
	CellDef cellDef;
    int rowIndex;
    int colIndex;


    public SizeProperties()
	{
	}


	public void setCol(FlexSize newWidth)
	{
		col = newWidth;
	}


	public FlexSize getCol()
	{
		return col;
	}


	public float getColWidth()
	{
		return col.getVal();
	}


	public float getRowHeight()
	{
		return row.getVal();
	}


	public void setRow(FlexSize newRowHeight)
	{
		row = newRowHeight;
	}


	public FlexSize getRow()
	{
		return row;
	}

	public CellDef getCellDef()
	{
		return cellDef;
	}

	public void setCellDef(CellDef cellDef)
	{
		this.cellDef = cellDef;
	}

    public int getColIndex()
    {
        return colIndex;
    }

    public void setColIndex(int colIndex)
    {
        this.colIndex = colIndex;
    }

    public int getRowIndex()
    {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex)
    {
        this.rowIndex = rowIndex;
    }
}
