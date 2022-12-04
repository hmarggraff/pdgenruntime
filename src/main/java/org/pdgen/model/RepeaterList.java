// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.cells.FreeCellDef;
import org.pdgen.model.cells.NestingCellDef;
import org.pdgen.model.style.TableStyle;
import org.pdgen.env.JoriaUserException;
import org.pdgen.data.AccessVisitor;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.JoriaClass;
import org.pdgen.data.Trace;
import org.pdgen.data.VariableProvider;
import org.pdgen.data.view.GroupValueAccess;
import org.pdgen.data.view.GroupingAccess;
import org.pdgen.data.view.IndirectAccess;
import org.pdgen.data.view.MutableView;
import org.pdgen.data.view.NameableAccess;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Env;
import org.pdgen.env.Res;
import org.pdgen.projection.ComputedField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class RepeaterList implements Serializable
{
    private static final long serialVersionUID = 7L;
    protected Repeater[] spandex;
	TemplateModel myModel;

	public static RepeaterList duplicate(RepeaterList rr, TemplateModel newModel, Repeater newOuter, Map<Object, Object> copiedData)
	{
		if (rr == null)
			return null;
		RepeaterList ret = new RepeaterList(newModel);
		if (rr.spandex == null)
			return ret;
		ret.spandex = new Repeater[rr.spandex.length];
		for (int i = 0; i < rr.spandex.length; i++)
		{
			Repeater repeater = rr.spandex[i];
			ret.spandex[i] = repeater.duplicate(newModel, newOuter, copiedData);
			copiedData.put(repeater, ret.spandex[i]);
		}
		return ret;
	}

	public RepeaterList(TemplateModel model)
	{
		myModel = model;
	}

	/**
	 * adds an Expander into the array of Expanders
	 * keeps the array sorted by the starting column and row
	 * this is important to ensure processing in proper sequence
	 *
	 * @param r the repeater
	 */
	public void addRepeater(Repeater r)
	{
		if (spandex == null)
		{
			spandex = new Repeater[1];
			spandex[0] = r;
			return;
		}
		Repeater[] tmp = new Repeater[spandex.length + 1];
		int six = 0;
		while (six < spandex.length && ((spandex[six].getStartCol() < r.getStartCol()) || (spandex[six].getStartCol() == r.getStartCol() && (spandex[six].getStartRow() == r.getStartRow()))))
		{
			six++;
		}
		if (six > 0)
			System.arraycopy(spandex, 0, tmp, 0, six);
		if (six < spandex.length)
			System.arraycopy(spandex, six, tmp, six + 1, spandex.length - six);
		tmp[six] = r;
		spandex = tmp;
		reindex();
	}

	public void remove(int at)
	{
		Repeater r = spandex[at];
		TemplateModel model = r.getModel();
		for (int row = r.getStartRow() - r.headerRows; row < model.rowCount && row < r.getStartRow(); row++)
		{
			for (int col = r.getStartCol(); col < model.colCount && col <= r.getEndCol(); col++)
			{
				CellDef cd = model.cellAt(row, col);
				if (cd != null && cd.getHeadedRepeater() == r)
					cd.setHeadedRepeater(null);
			}
		}
		Repeater outer = r.getOuterRepeater();
		for (int row = r.getStartRow(); row < model.rowCount && row <= r.getEndRow(); row++)
		{
			for (int col = r.getStartCol(); col < model.colCount && col <= r.getEndCol(); col++)
			{
				CellDef cd = model.cellAt(row, col);
				if (cd != null && cd.getRepeater() == r)
					cd.setRepeater(outer);
			}
		}
		if (spandex.length == 1)
		{
			spandex = null;
			return;
		}
		Repeater[] tmp = new Repeater[spandex.length - 1];
		if (at != 0)
			System.arraycopy(spandex, 0, tmp, 0, at);
		if (at != spandex.length - 1)
			System.arraycopy(spandex, at + 1, tmp, at, spandex.length - at - 1);
		spandex = tmp;
		reindex();
	}

	void adjustDeletedRange(int sr, int er, int sc, int ec)
	{
		if (spandex == null)
			return;
		Repeater[] repeaters = spandex;// hold on to array, because spandex may be reallocated
		for (int j = repeaters.length - 1; j >= 0; j--)
		{
			Repeater sf = repeaters[j];
			if (sc <= sf.getStartCol() && ec >= sf.getEndCol() && sr <= sf.getStartRow() && er >= sf.getEndRow())// the complete repeater: remove it
			{
				Trace.logDebug(Trace.wizards, "Range :" + sf + " deleted");
				remove(j);
			}
			else if (sc <= sf.getStartCol() && ec >= sf.getEndCol() && sr <= sf.getEndRow() && er >= sf.getStartRow())// a whole row of the repeater: contract upwards
			{
				sf.endRow = sf.getEndRow() + er - sr - 1;
				sf.getRepeaterList().adjustDeletedRange(sr, er, sc, ec);
			}
			else if (sr <= sf.getStartRow() && er >= sf.getEndRow() && sc <= sf.getEndCol() && ec >= sf.getStartCol())// a whole column of the repeater: contract from right
			{
				sf.endCol = sf.getEndCol() + sc - ec - 1;
				sf.getRepeaterList().adjustDeletedRange(sr, er, sc, ec);
			}
			else if (er < sf.getStartRow() && sc <= sf.getStartCol() && ec >= sf.getEndCol())// above a complete repeater: shift repeater up
			{
				sf.endRow = sf.getEndRow() + sr - er - 1;
				sf.startRow = sf.getStartRow() + sr - er - 1;
				sf.getRepeaterList().adjustDeletedRange(sr, er, sc, ec);
			}
			else if (ec < sf.getStartCol() && sr <= sf.getStartRow() && er >= sf.getEndRow())// left of a complete repeater: shift repeater left
			{
				sf.startCol = sf.getStartCol() + sc - ec - 1;
				sf.endCol = sf.getEndCol() + sc - ec - 1;
				sf.getRepeaterList().adjustDeletedRange(sr, er, sc, ec);
			}
		}
	}

	protected void adjustCols(int sc, int n)
	{
		adjustColsInner(sc, n);
		ownCells0(null);
	}

	protected void adjustColsInner(int sc, int n)
	{
		// if  n is negative then boxes are contracted
		if (spandex == null)
			return;
		Repeater[] repeaters = spandex;// hold on to original value, because spandex may be reallocated
		for (int j = repeaters.length - 1; j >= 0; j--)
		{
			Repeater sf = repeaters[j];
			if (sc <= sf.getStartCol() && sc > sf.getEndCol() + n)
			{
				Trace.log(Trace.template, "Range :" + sf + " deleted");
				remove(j);
			}
			else
			{
				sf.getRepeaterList().adjustColsInner(sc, n);
				if (sf.getStartCol() > sc)
					sf.startCol = Math.max(sf.getStartCol() + n, sc);
				if (sf.getEndCol() >= sc)
					sf.endCol = Math.max(sf.getEndCol() + n, sc - 1);
			}
		}
	}

	public void adjustRows(int sr, int n)
	{
		adjustRowsInner(sr, n);
		ownCells0(null);
	}

	protected void adjustRowsInner(int sr, int n)
	{
		// if n is negative then boxes are expanded
		if (spandex == null)
			return;
		// loop modifies spandex so we have to iterate from the back
		for (int j = spandex.length - 1; spandex != null && j >= 0; j--)
		{
			Repeater sf = spandex[j];
			if (sr <= sf.getStartRow() && sr - n > sf.getEndRow())// check if box completely disappears
			{
				remove(j);
				Trace.logDebug(Trace.wizards, "Range: " + sf + " deleted");
				if (spandex == null)
					return;
			}
			else
			{
				if (sr < sf.getStartRow())
				{
					if (sr >= sf.getStartRow() - sf.getHeaderRows())
						sf.setHeaderRows(Math.max(sf.getHeaderRows() + n, 0));
					if (sf.getStartRow() + n <= sr)// can happen if n negative because we are shifting up
						sf.startRow = sr;// do not shift up above beginning of deletion
					else
						sf.startRow = sf.getStartRow() + n;
				}
				if (sr <= sf.getEndRow())
				{
					if (sf.getEndRow() + n < sr - 1)// can happen if n negative because we are shifting up
						sf.endRow = sr - 1;// do not shift up above beginning of deletion
					else
						sf.endRow += n;
				}
				// adjust inner repeaters
				sf.getRepeaterList().adjustRowsInner(sr, n);
			}
		}
	}

	public Repeater get(int ix)
	{
		return spandex[ix];
	}

	public Repeater[] get()
	{
		return spandex;
	}

	public int length()
	{
		if (spandex == null)
			return 0;
		return spandex.length;
	}

	public boolean isOverlapping(int sr, int er, int sc, int ec)
	{
		for (Repeater sf : spandex)
		{
			if (sf.isOverlapping(sr, er, sc, ec))
				return true;
			sf.getRepeaterList().isOverlapping(sr, er, sc, ec);
		}
		return false;
	}

	/**
	 * checks whether the range crosses a repeater. I.e it containts both cells from inside a repeater and outside
	 * and the repeater(s) are not completely contained
	 *
	 * @param sr start row
	 * @param er end row
	 * @param sc start column
	 * @param ec end column
	 * @return if a conflict occurs
	 */
	public boolean isCrossing(int sr, int er, int sc, int ec)
	{
		if (spandex == null)
			return false;
		for (Repeater sf : spandex)
		{
			if (sf.isConflicting(sr, er, sc, ec))
				return true;
			sf.getRepeaterList().isCrossing(sr, er, sc, ec);
		}
		return false;
	}

	public void nest(Repeater newSF)
	{
		if (spandex != null)
		{
			for (int i = 0; i < spandex.length; i++)
			{
				Repeater b = spandex[i];
				if (b.isContaining(newSF.getStartRow(), newSF.getEndRow(), newSF.getStartCol(), newSF.getEndCol()))
				{
					b.getRepeaterList().nest(newSF);
					newSF.setOuterRepeater(b);
					return;
				}
				else if (b.isContained(newSF.getStartRow(), newSF.getEndRow(), newSF.getStartCol(), newSF.getEndCol()))
				{
					newSF.getRepeaterList().addRepeater(b);
					remove(i);
					b.setOuterRepeater(newSF);
				}
				else if (b.isOverlapping(newSF.getStartRow(), newSF.getEndRow(), newSF.getStartCol(), newSF.getEndCol()))
				{
					throw new JoriaAssertionError("Forms overlap: [" + newSF.getStartRow() + "," + newSF.getEndRow() + "," + newSF.getStartCol() + "," + newSF.getEndCol() + "] with [" + b.getStartRow() + "," + b.getEndRow() + "," + b.getStartCol() + "," + b.getEndCol() + "]");
				}
			}
		}
		addRepeater(newSF);
	}

	protected void reindex()
	{
		if (spandex == null)
			return;
		for (int i = 0; i < spandex.length; i++)
		{
			spandex[i].setIndex(i);
		}
	}

	protected void ownCells(Repeater owner, Map<Object, Object> copiedAccess)
	{
		for (int i = owner.getStartRow(); i <= owner.getEndRow(); i++)
		{
			for (int j = owner.getStartCol(); j <= owner.getEndCol(); j++)
			{
				CellDef at = myModel.cellAt(i, j);
				if (at == null)
				{
					at = new FreeCellDef(myModel);// empty cells that are part of a repeater must still refer to the repeater
					myModel.setCellAt(at, i, j);
				}
				at.setRepeater(owner);
				if (at instanceof DataCellDef && copiedAccess != null)
				{
					DataCellDef dcd = (DataCellDef) at;
					JoriaAccess oldData = dcd.getAccessor();
					JoriaAccess newData = (JoriaAccess) copiedAccess.get(oldData);
					if (newData != null)
						dcd.setAccessor(newData);
					else
					{
						oldData.getDefiningClass();
						// formula is considered to be a local formula of the template
						// Therefore it must be duplicated
						//TODO distinguish the cases where the formula comes from an anonymous view in a regular view. Then it should not be duplicated
						if (oldData instanceof ComputedField && !((ComputedField)oldData).isDefinedInView())
						{
							dcd.setAccessor(((ComputedField) oldData).dup(oldData.getDefiningClass(), copiedAccess));
						}
					}
				}
				else if (at instanceof NestingCellDef)
				{
					NestingCellDef ncd = (NestingCellDef) at;
					ncd.getInnerBox().getTemplate().getRepeaterList().ownCells(owner, copiedAccess);
				}
			}
		}
		final int headers = owner.getHeaderRows();
		if (headers > 0)
		{
			for (int r = owner.getStartRow() - headers; r < owner.getStartRow(); r++)
			{
				for (int c = owner.getStartCol(); c <= owner.getEndCol(); c++)
				{
					CellDef cd = myModel.cellAt(r, c);
					if (cd == null)// create a free celldef so we can store the headed repeater
					{
						cd = new FreeCellDef(myModel);
						myModel.setCellAt(cd, r, c);
					}
					cd.setHeadedRepeater(owner);
				}
			}
		}
		RepeaterList subs = owner.getRepeaterList();
		for (int i = 0; i < subs.length(); i++)
		{
			ownCells(subs.get(i), copiedAccess);
		}
	}

	protected void ownCells0(Map<Object, Object> copiedAccesses)
	{
		clearRepeaters();
		RepeaterList subs = myModel.getRepeaterList();
		for (int i = 0; i < subs.length(); i++)
		{
			ownCells(subs.get(i), copiedAccesses);
		}
	}

	private void clearRepeaters()
	{
		for (int i = 0; i < myModel.getRowCount(); i++)
		{
			for (int j = 0; j < myModel.getColCount(); j++)
			{
				CellDef at = myModel.cellAt(i, j);
				if (at != null)
				{
					at.setRepeater(null);
					at.setHeadedRepeater(null);
					if (at instanceof NestingCellDef)
					{
						NestingCellDef ncd = (NestingCellDef) at;
						ncd.getInnerBox().getTemplate().getRepeaterList().clearRepeaters();
					}
				}
			}
		}
	}

	void adjustHeadedRepeaters()
	{
		if (spandex == null)
			return;
		for (Repeater owner : spandex)
		{
			final int headers = owner.getHeaderRows();
			if (headers > 0)
			{
				for (int r = owner.getStartRow() - headers; r < owner.getStartRow(); r++)
				{
					for (int c = owner.getStartCol(); c <= owner.getEndCol(); c++)
					{
						CellDef cd = myModel.cellAt(r, c);
						if (cd == null)// create a free celldef so we can store the headed repeater
						{
							cd = new FreeCellDef(myModel);
							myModel.setCellAt(cd, r, c);
						}
						cd.setHeadedRepeater(owner);
					}
				}
			}
		}
	}

	public Repeater find(JoriaAccess axs)
	{
		if (spandex == null)
			return null;
		for (Repeater repeater : spandex)
		{
			JoriaAccess a = repeater.getAccessor();
			while (a instanceof IndirectAccess)
			{
				if (a == axs)
					return repeater;
				IndirectAccess pa = (IndirectAccess) a;
				a = pa.getBaseAccess();
			}
			if (a == axs)
				return repeater;
			else
			{
				Repeater inner = repeater.getRepeaterList().find(axs);
				if (inner != null)
					return inner;
			}
		}
		return null;
	}

	public boolean checkRange(int sr, int er, int sc, int ec)
	{
		if (spandex == null)
			return false;
		for (Repeater sf : spandex)
		{
			if (sf.isOverlapping(sr, er, sc, ec))
			{
				//noinspection SimplifiableIfStatement
				if (sf.isContaining(sr, er, sc, ec))
					return sf.getRepeaterList().checkRange(sr, er, sc, ec);
				else
					return true;// range conflicts with a repeater
			}
		}
		return false;
	}

	public ArrayList<Repeater> findContainedRepeaters(int sr, int er, int sc, int ec) throws JoriaUserException
	{
		if (spandex == null)
			return null;
		ArrayList<Repeater> a = null;
		for (Repeater sf : spandex)
		{
			if (sf.isConflicting(sr, er, sc, ec))
				throw new JoriaUserException(Res.str("Selection_range_conflicts_with_a_repeating_range"));
			if (sf.isContained(sr, er, sc, ec))
			{
				if (a == null)
					a = new ArrayList<Repeater>();
				a.add(sf);
			}
		}
		if (a != null)// found contained repeaters return them and we are done
			return a;
		for (Repeater sf : spandex)
		{
			a = sf.getRepeaterList().findContainedRepeaters(sr, er, sc, ec);
			if (a != null)// found contained repeaters return them and we are done
				return a;
		}
		return null;// no contained repeater found
	}

	/**
	 * find a repeater where the range is contained in, but that is not equal. (proper containment)
	 *
	 * @param sr start row
	 * @param er end row
	 * @param sc start column
	 * @param ec end column
	 * @return a repeater that properly containsName range or null if no such repeater exists
	 */
	public Repeater findContainingRepeater(int sr, int er, int sc, int ec)
	{
		if (spandex == null)
			return null;
		for (Repeater sf : spandex)
		{
			if (sf.isRange(sr, er, sc, ec))
				return null;// if there is a repeater that is identical to the range then no other repeater at this level can contain the range
			if (sf.isContaining(sr, er, sc, ec))
			{
				Repeater ret = sf.getRepeaterList().findContainingRepeater(sr, er, sc, ec);
				if (ret != null)
					return ret;// a nested repeater containsName range
				else
					return sf;
			}
		}
		return null;
	}

	public void namedTableStyleChanged(TableStyle ts)
	{
		if (spandex == null)
			return;
		for (Repeater aSpandex : spandex)
		{
			aSpandex.namedTableStyleChanged(ts);
		}
	}

	public Repeater innerMostRepeaterAt(int row, int col)
	{
		if (spandex == null)
			return null;
		for (Repeater r : spandex)
		{
			if (row < r.getStartRow() || row > r.getEndRow() || col < r.getStartCol() || col > r.getEndCol())
				continue;
			Repeater ir = r.getRepeaterList().innerMostRepeaterAt(row, col);
			if (ir != null)
				return ir;
			else
				return r;
		}
		return null;
	}

	protected void collectFilterVariables(Set<RuntimeParameter> s, Set<Object> seen)
	{
		if (spandex == null)
			return;
		for (Repeater aSpandex : spandex)
		{
			JoriaAccess axs = aSpandex.getAccessor();
			if (axs instanceof VariableProvider)
			{
				((VariableProvider) axs).collectVariables(s, seen);
			}
			aSpandex.getRepeaterList().collectFilterVariables(s, seen);
		}
	}

	/**
	 * shift the repeaters without further adjusments
	 *
	 * @param rowDelta rows to shift
	 * @param colDelta columns to shift
	 */
	protected void shift(int rowDelta, int colDelta)
	{
		if (spandex == null)
			return;
		for (Repeater repeater : spandex)
		{
			repeater.shift(rowDelta, colDelta);
		}
	}

	public void unlinkGroup(Repeater r)
	{
		//TODO was macht das ? Sollten nicht noch die GroupTotalCells entfernt werden?
		// Trace.check(r.getRoot(), GroupingAccess.class); // ensure that a group repeater is unlinked
		for (int i = 0; i < spandex.length; i++)
		{
			Repeater repeater = spandex[i];
			if (repeater == r)
			{
				remove(i);
				RepeaterList rl = r.getRepeaterList();
				if (rl == null || rl.spandex == null)// grouping only: that can happen to create a distinct list
					return;
				Trace.check(rl.spandex.length == 1);// it may only have one inner repeater
				Repeater ir = rl.spandex[0];//
				ir.setOuterRepeater(r.getOuterRepeater());
				GroupingAccess ga = (GroupingAccess) r.getAccessor();
				NameableAccess vax = ga.getValueAccess();
				if (ga.getBaseAccess() instanceof GroupingAccess)
				{
					GroupingAccess bga = (GroupingAccess) ga.getBaseAccess();
					bga.setInnerColView(vax);
				}
				else if (vax instanceof GroupValueAccess)
				{
					ir.myData = ga.getBaseAccess();
				}
				else if (vax instanceof GroupingAccess)
				{
					GroupingAccess iga = (GroupingAccess) vax;
					iga.setBaseAccess(ga.getBaseAccess());
				}
				/*
									GroupValueAccess gva = (GroupValueAccess)ir.getRoot();
									PickAccess npa = new PickAccess(gva.getBaseAccess(), (CollectionView) gva.getType());
									ir.myData = npa;
								}
								else if (ir.getRoot() instanceof GroupingAccess)
								{
									GroupingAccess ga = (GroupingAccess) ir.getRoot();
									ir.myData = ga.getBaseAccess();
								}
								}
								*/
				return;
			}
		}
		/*
				  if (spandex.length > 0) // if we only have a grouping an no group details
					  throw new JoriaAssertionError("Repeater to unlink was not found");
				  */
	}

	public void reloadStyles()
	{
		if (spandex == null)
			return;
		for (Repeater r : spandex)
		{
			TableStyle ts = r.getTableStyle();
			if (ts != null && ts.getName() != null)
                r.setTableStyle(Env.instance().repo().tableStyles.find(ts.getName()));
			r.getRepeaterList().reloadStyles();
		}
	}

	public Repeater getRepeaterHere(int row, int col)
	{
		if (spandex == null)
			return null;
		for (Repeater r : spandex)
		{
			if (r.startRow <= row && r.endRow >= row && r.startCol <= col && r.endCol >= col)
				return r;
		}
		return null;
	}

	public void getRepeaterHeaderStartRows(boolean[] ret)
	{
		if (spandex == null)
			return;
		for (Repeater r : spandex)
		{
			ret[r.getStartRow() - r.getHeaderRows()] = true;
		}
	}

	public void getRepeaterForHeaders(Repeater[][] repeaterForHeader)
	{
		if (spandex == null)
			return;
		for (Repeater rr : spandex)
		{
			if (rr.getModel() != myModel)// repeater is nested
				continue;
			final int headers = rr.getHeaderRows();
			if (headers > 0)
			{
				for (int r = rr.getStartRow() - headers; r < rr.getStartRow(); r++)
				{
					for (int c = rr.getStartCol(); c <= rr.getEndCol(); c++)
					{
						CellDef cd = myModel.cellAt(r, c);
						if (cd == null)// create a free celldef so we can store the headed repeater
						{
							cd = new FreeCellDef(myModel);
							myModel.setCellAt(cd, r, c);
						}
						cd.setHeadedRepeater(rr);
						if (repeaterForHeader != null)
							repeaterForHeader[r][c] = rr;
					}
				}
			}
			rr.getRepeaterList().getRepeaterForHeaders(repeaterForHeader);
		}
	}

	public void makeUnbound()
	{
		if (spandex == null)
			return;
		for (Repeater rr : spandex)
		{
			rr.makeUnbound();
		}
	}

	public void fixModelLink(TemplateModel model)
	{
		if (myModel != model)
			myModel = model;
		if (spandex == null)
			return;
		for (Repeater rr : spandex)
		{
			rr.getRepeaterList().fixModelLink(model);
		}
	}

	public void fixAccess()
	{
		if (spandex == null)
			return;
		for (Repeater rr : spandex)
		{
			rr.fixAccess();
		}
	}

	public void collectViewUsage(Map<MutableView, Set<Object>> viewUsageMap, Set<MutableView> visitedViews)
	{
		if (spandex != null)
			for (Repeater repeater : spandex)
			{
				repeater.collectViewUsage(viewUsageMap, visitedViews);
			}
	}


	public void getUsedAccessors(Set<JoriaAccess> s)
	{
		if (spandex == null)
			return;
		for (Repeater repeater : spandex)
		{
			repeater.getUsedAccessors(s);
		}
	}

	public boolean visit(RepeaterVisitor repVisistor)
	{
		for (int i = 0; spandex != null && i < spandex.length; i++)
		{
			Repeater repeater = spandex[i];
			if (!repeater.visit(repVisistor))
				return false;
		}
		return true;
	}

	public int getIndex(Repeater repeater)
	{
		for (int i = 0; i < spandex.length; i++)
		{
			Repeater repeater1 = spandex[i];
			if (repeater1 == repeater)
				return i;
		}
		return -1;
	}

	public void rebindByName(final JoriaClass newScope)
	{
		if (spandex == null)
			return;
		for (Repeater r : spandex)
		{
			r.rebindByName(newScope);
		}
	}

	public boolean visitAccesses(AccessVisitor visitor, Set<JoriaAccess> seen)
	{
		for (int i = 0; spandex != null && i < spandex.length; i++)
		{
			Repeater repeater = spandex[i];
			if (!repeater.visitAccesses(visitor, seen))
				return false;
		}
		return true;
	}
}
