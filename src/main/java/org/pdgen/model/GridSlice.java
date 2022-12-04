// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCell;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.env.JoriaUserException;
import org.pdgen.data.JoriaClassHelper;
import org.pdgen.data.JoriaType;
import org.pdgen.env.Res;

import java.util.ArrayList;
import java.util.HashMap;

public class GridSlice
{
	CellDef[] cells;
	int cols;
	ArrayList<Repeater> repeaters;
	JoriaType scope;
	Repeater containingRepeater;

	public GridSlice(TemplateModel sourceModel, int sr, int er, int sc, int ec, boolean cut) throws JoriaUserException
	{
		cols = ec - sc + 1;
		int h = er - sr + 1;
		repeaters = sourceModel.myRepeaters.findContainedRepeaters(sr, er, sc, ec);
		containingRepeater = sourceModel.myRepeaters.findContainingRepeater(sr, er, sc, ec);
		cells = new CellDef[h * cols];
		if (repeaters != null)
		{
			for (Repeater rr : repeaters)
			{
				if (sr > rr.getStartRow() - rr.getHeaderRows())
					rr.setHeaderRows(0);
				rr.shift(-sr, -sc);
				if (containingRepeater != null)
				{
					containingRepeater.getRepeaterList().remove(rr.myIndex);
				}
				else
					sourceModel.getRepeaterList().remove(rr.myIndex);
			}
		}
		for (int i = sr; i <= er; i++)
		{
			int co = (i - sr) * cols;
			for (int j = 0; j < cols; j++)
			{
				CellDef cd = sourceModel.cellAt(i, sc + j);
				cells[co + j] = cd;
				if (cd != null)
				{
					if (cut)
					{
						sourceModel.setCellAtInternal(null, i, sc + j);
					}
					else if (cd instanceof DataCellDef)// copying does not copy the aggregates
					{
						DataCellDef def = (DataCellDef) cd;
						def.setAggregates(null);
					}
					cd.setGrid(null);
				}
			}
		}
	}

	/*
	 * does a proper copy
	 */
	public GridSlice(TemplateModel sourceModel, int sr, int er, int sc, int ec) throws JoriaUserException
	{
		cols = ec - sc + 1;
		int h = er - sr + 1;
		ArrayList<Repeater> oldInnerRepeaters = sourceModel.myRepeaters.findContainedRepeaters(sr, er, sc, ec);
		HashMap<Object, Object> copiedReferences = new HashMap<Object, Object>();
		containingRepeater = sourceModel.myRepeaters.findContainingRepeater(sr, er, sc, ec);
		cells = new CellDef[h * cols];
		scope = sourceModel.getAccessor().getType();
		if (oldInnerRepeaters != null)
		{
			repeaters = new ArrayList<Repeater>(oldInnerRepeaters.size());
			for (Repeater or : oldInnerRepeaters)
			{
				Repeater nr = or.duplicate(null, or.getOuterRepeater(), copiedReferences);
				repeaters.add(nr);
				copiedReferences.put(or, nr);
				if (sr > nr.getStartRow() - nr.getHeaderRows())
					nr.setHeaderRows(0);
				nr.shift(-sr, -sc);
			}
		}
		for (int i = sr; i <= er; i++)
		{
			int co = (i - sr) * cols;
			for (int j = 0; j < cols; j++)
			{
				CellDef cd = sourceModel.cellAt(i, sc + j);
				if (cd != null)
				{
					CellDef ncd = cd.duplicate(sourceModel, copiedReferences);
					copiedReferences.put(cd, ncd);
					if (ncd instanceof DataCellDef)// copying does not copy the aggregates
					{
						DataCellDef def = (DataCellDef) ncd;
						def.setAggregates(null);
					}
					ncd.setGrid(null);
					if (cd.getRepeater() != null)
					{
						Repeater nr = (Repeater) copiedReferences.get(cd.getRepeater());
						ncd.setRepeater(nr);
					}
					cells[co + j] = ncd;
				}
			}
		}
	}

	public void paste(TemplateModel m, int sr, int sc) throws JoriaUserException
	{
		int rows = cells.length / cols;
		int extraColsNeeded = cols + sc - m.getColCount();
		if (extraColsNeeded > 0)
		{
			m.addCols(m.getEndCol(), extraColsNeeded, 1);
		}
		int extraRowsNeeded = rows + sr - m.getRowCount();
		if (extraRowsNeeded > 0)
		{
			m.addRows(m.getEndRow(), extraRowsNeeded, 1);
		}
		for (int i = 0; i < rows; i++)
		{
			ArrayList<CellDef> tr = m.myRows.get(sr + i);
			int ro = i * cols;
			for (int j = 0; j < cols; j++)
			{
				CellDef cd = cells[ro + j];
				if (cd != null)
				{
					tr.set(sc + j, cd);
					cd.setGrid(m);
					cd.clearCachedStyle();
				}
			}
		}
		Repeater dest = m.getRepeaterList().findContainingRepeater(sr, sr + rows - 1, sc, sc + cols - 1);
		JoriaType scope = null;
		if (dest != null)
			scope = dest.getAccessor().getType();
		if (repeaters != null)
		{
			for (Repeater rr : repeaters)
			{
				rr.shift(sr, sc);
				rr.setModel(m);
				Repeater ctx = rr.getOuterRepeater();
				if (ctx != null)
				{
					if (dest == null)// TODO diese Exception sind viel zu spaet !!!!!!!!!!!!!!!!!!!!!
						throw new JoriaUserException(Res.str("Cannot_paste_a_nested_repeater_in_the_top_level_scope"));
					if (ctx != dest && ctx.getAccessor().getType() != scope)// paste in a different repeater
						throw new JoriaUserException(Res.str("Cannot_paste_a_nested_repeater_into_a_scope_with_a_different_type"));
					if (ctx == dest)
						dest.getRepeaterList().addRepeater(rr);
				}
				else
				{
					m.getRepeaterList().addRepeater(rr);
				}
				rr.setOuterRepeater(dest);
				//dest.getRepeaterList().addRepeater(rr);
			}
		}
	}

	public void pasteCopy(TemplateModel m, int sr, int sc, boolean neutralize) throws JoriaUserException
	{
		int rows = cells.length / cols;
		Repeater dest = m.getRepeaterList().findContainingRepeater(sr, sr + rows - 1, sc, sc + cols - 1);
		//TODO deal with occupied cells
		if (m.getRowCount() < sr + rows)
		{
			m.addRowsUnadjusted(sr + rows - m.getRowCount(), m.getRowCount() - 1, 1, false);
		}
		if (sc + cols > m.getColCount())
		{
			m.addColsUnadjusted(sc + cols - m.getColCount(), m.getColCount() - 1, 1, false);
		}
		if (dest != null)
		{
			JoriaType type = null;
			if (containingRepeater == null)
			{
				//throw new JoriaUserException(Res.str("Cannot_paste_in_this_context_Top_level_fields_can_not_be_pasted_into_a_table"));
				findDataType:
				for (int i = 0; i < rows; i++)
				{
					//ArrayList<CellDef> tr = m.myRows.get(sr + i);
					int ro = i * cols;
					for (int j = 0; j < cols; j++)
					{
						CellDef cd = cells[ro + j];
						if (cd instanceof DataCell)
						{
							DataCell dc = (DataCell) cd;
							type = dc.getAccessor().getDefiningClass();
							break findDataType;
						}
					}
				}
				JoriaType newScope = dest.getAccessor().getCollectionTypeAsserted().getElementType();
				neutralize |= !JoriaClassHelper.isAssignableFrom(newScope, type);
			}
			else
			{
				type = containingRepeater.getAccessor().getType();
				JoriaType newScope = dest.getAccessor().getType();
				neutralize |= newScope != type;
			}
			if (sr + rows - 1 > dest.getEndRow())
			{
				dest.setEndRow(sr + rows - 1);
			}
			if (sc + cols - 1 > dest.getEndCol())
				dest.setEndCol(sc + cols - 1);
		}
		else
		{
			if (containingRepeater != null)
			{
				throw new JoriaUserException(Res.str("Cannot_paste_in_this_context_Table_fields_can_not_be_pasted_on_top_level"));
			}
			JoriaType newScope = m.getFrame().getPageLevelParent().getRoot().getType();
			neutralize |= !JoriaClassHelper.isAssignableFrom(newScope, scope);
		}
		HashMap<Object, Object> copiedReferences = new HashMap<Object, Object>();
		if (repeaters != null)
		{
			for (Repeater or : repeaters)
			{
				Repeater nr = or.duplicate(m, dest, copiedReferences);
				nr.shift(sr, sc);
				Repeater ctx = or.getOuterRepeater();
				if (ctx != null)
				{
					if (dest != null)
						dest.getRepeaterList().addRepeater(nr);
				}
				else
				{
					m.getRepeaterList().addRepeater(nr);
				}
			}
			m.fireChange("paste cells wih table");//trdone
		}
		else
			m.fireChange("paste cells");//trdone
		for (int i = 0; i < rows; i++)
		{
			ArrayList<CellDef> tr = m.myRows.get(sr + i);
			int ro = i * cols;
			for (int j = 0; j < cols; j++)
			{
				CellDef cd = cells[ro + j];
				if (cd != null)
				{
					CellDef ncd = cd.duplicate(m, copiedReferences);
					ncd.clearCachedStyle();
					if (neutralize)
						ncd.makeUnboundCell();
					tr.set(sc + j, ncd);
				}
			}
		}
		m.getRepeaterList().ownCells0(null);
	}
}
