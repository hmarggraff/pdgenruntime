// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.JoriaDataException;
import org.pdgen.data.Trace;
import org.pdgen.model.RDBase;
import org.pdgen.model.Repeater;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.OutputMode.LastRowState;

public class RunTemplate extends RunRangeBase
{
	int lastRowTocUpdated = -1;

	/**
	 * Creates new RunRange for a single object
	 *
	 * @param out    the output object
	 * @param values values to be displayed
	 * @param defs   cells and repeater to be displayed
	 * @param level  level of frame in frame
	 */
	public RunTemplate(OutputMode out, RVTemplate values, RDRangeBase defs, int level)
	{
		super(0, out.getTemplate().getColCount() - 1, out.getTemplate().getRowCount(), out.getTemplate(), out, level, defs, values);
		if (template.getRepeaterList().length() > 0)
		{
			listerCounts = new int[nRow];
			listers = new RunRepeater[template.getRepeaterList().length()];// the index of each Repeater in explist matches the index in this array. (Hopefully)
		}
	}

	public RunTemplate(OutputMode out, RVTemplate values, RDRangeBase defs, int level, TemplateModel template)
	{
		super(0, template.getColCount() - 1, template.getRowCount(), template, out, level, defs, values);
		if (template.getRepeaterList().length() > 0)
		{
			listerCounts = new int[nRow];
			listers = new RunRepeater[template.getRepeaterList().length()];// the index of each Repeater in explist matches the index in this array. (Hopefully)
		}
	}

	protected LastRowState hasInitialData() throws JoriaDataException
	{
		inRow = 0;
		return LastRowState.proceed;
	}

	public Repeater findRepeaterStart(int r, int c)
	{
		return template.getRepeaterList().getRepeaterHere(r, c);
	}

	/**
	 * makes a lister (RunRepeater) from the top level
	 */
	protected RunRepeater makeLister(int row, int col) throws JoriaDataException
	{
		if (values == null)
			return null;
		RunRepeater lister;
		final RDBase rdBase = dataDef.getFields()[row][col];
		Trace.logDebug(Trace.fill, "make Lister at " + row + "," + col);
		RDRepeater listDef = (RDRepeater) rdBase;
		Repeater tate = (Repeater) listDef.getModel();
		RVTemplate listVals = (RVTemplate) values.get(row, col);
		//exp hmf 030408: always build a lister so that borders and backgrounds get painted at least once
		//exp hmf 050715: do not build if empty table shall be compressed
		if ((listVals == null || listVals.getElementCount() == 0) && tate.getCascadedTableStyle().getSuppressEmpty())
			return null;
		lister = new RunRepeater(tate, listDef, listVals, out, nestingLevel + 1, null);
		lister.startBodyAt(out.getOutRow());
		listers[tate.getTopLevelIndex()] = lister;
		listerCounts[tate.getEndRow()]++;
		return lister;
	}

	public void pageBreak(OutputMode.LastRowState endState)
	{
		Trace.logDebug(Trace.fill, ind + "RunRange pageBreak " + endState);
		if (listers != null)
		{
			for (RunRangeBase activeLister : listers)
			{
				if (activeLister != null)// null --> completed
					activeLister.pageBreak(endState);
			}
		}
	}

	public int getIteration()
	{
		return 0;
	}
}
