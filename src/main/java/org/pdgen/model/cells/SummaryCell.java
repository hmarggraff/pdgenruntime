// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.env.Env;
import org.pdgen.model.RDBase;
import org.pdgen.env.Res;
import org.pdgen.model.PageLevelBox;
import org.pdgen.model.TemplateModel;

import java.awt.Graphics2D;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

public class SummaryCell extends SimpleTextCellDef implements RelatedCell
{
    private static final long serialVersionUID = 7L;
    int where;
	AggregateDef aggDef;
	DataCellDef summaryFor;

	public SummaryCell(TemplateModel parentGrid, AggregateDef aggDef, int where, DataCellDef dataCell)
	{
		super(parentGrid, SimpleTextCellDef.abbrev(makeName(aggDef, where)));
		this.aggDef = aggDef;
		this.where = where;
		summaryFor = dataCell;
	}

	public SummaryCell(SummaryCell from, TemplateModel parentGrid)
	{
		super(from, parentGrid);
		where = from.where;
		aggDef = from.aggDef;// this assumes that aggDef is immutable and does not need to be duplicated
	}

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences)
	{
		return new SummaryCell(this, newContainerGrid);
	}

	String getValString(AggregateCollector into)
	{
		String val;
		int typ = aggDef.getResultType();
		if (typ == AggregateDef.stringType)
		{
			val = into.getAggregateString(aggDef, where);
		}
		else
		{
			if (typ == AggregateDef.doubleType)
			{
				double d = into.getAggregateDouble(aggDef, where);
				val = into.format(d, getCascadedStyle());
			}
			else
			{
				long d = into.getAggregateLong(aggDef, where);
				val = into.format(d, getCascadedStyle());
			}
		}
		return val;
	}

	protected static String makeName(AggregateDef aggDef, int where)
	{
		return AggregateDef.scopeNames[where] + "(" + aggDef.getName() + ")";
	}

	public AggregateDef getAggregateDef()
	{
		return aggDef;
	}

	public String getFormattedString(DBData from, AggregateCollector into)
	{
		return SimpleTextCellDef.wrapText(getValString(into), getCascadedStyle(), into.getRunEnv().getLocale());
	}

	public CellDef getDest()
	{
		return summaryFor;
	}

	public void removed()
	{
		boolean[] scopes = aggDef.getScopes();
		scopes[where] = false;
		boolean anyScope = false;
		for (boolean scope : scopes)
		{
			anyScope |= scope;
		}
		if (!anyScope)
		{
			if (summaryFor != null)
			{
				if (summaryFor.getAggregates() == null)
					Env.instance().tell(Res.str("Summary_field_is_broken_can_be_fixed"), getText() + "\n" + summaryFor.getText());
				else
				{
					summaryFor.getAggregates().remove(aggDef);
					if (summaryFor.getAggregates().size() == 0)
					{
						summaryFor.setAggregates(null);
					}
				}
			}
			PageLevelBox tb = getGrid().getFrame().getPageLevelParent();
			// this may throw a null pointer exception if the summaryx cell is in a named frame
			tb.getPage().getAggregates().remove(aggDef);
		}
	}

	public void setDest(CellDef newDest)
	{
		summaryFor = (DataCellDef) newDest;
	}

	public boolean isPageSummary()
	{
		return where == AggregateDef.page || where == AggregateDef.running;
	}

	public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
	{
		if (!isVisible(outMode, from))
			return RVSupressHeader.instance;
		return new RVString(myText, getCascadedStyle(), g);// paceholder to prevent a null value
	}

	protected String getGraphElemString(TableBorderRequirements tblReq, int iter, FillPagedFrame out)
	{
		//todo aggregate when processing aggregated cells
		return SimpleTextCellDef.wrapText(getValString(out.getPageRun()), getCascadedStyle(), out.getRunEnv().getLocale());
	}

	public float getMaxWidth(RVAny values, Locale loc, Graphics2D g)
	{
		// todo set width from the totalled cell where required and possible (Min/Max)
		int resultType = aggDef.getResultType();
		CellStyle cs = getCascadedStyle();
		if (resultType == AggregateDef.doubleType)
			return cs.getWidth(SimpleTextCellDef.wrapText("9999999999.99", cs, loc), g);
		else if (resultType == AggregateDef.longType)
			return cs.getWidth(SimpleTextCellDef.wrapText("9999999999", cs, loc), g);
		else
			return 0;// this must min max of string which is sized properly by the strings themselves
	}

	public int getWhere()
	{
		return where;
	}

	public boolean fixAccess()
	{
		return aggDef.fixAccess();
	}
}
