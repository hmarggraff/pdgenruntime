// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.*;

import org.pdgen.data.view.AggregateDef;
import org.pdgen.data.view.PercentageAccess;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.Env;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.style.CellStyle;
import org.pdgen.projection.ComputedField;
import org.pdgen.projection.UnboundMembersClass;
import org.pdgen.oql.JoriaDeferedQuery;
import org.pdgen.oql.OQLParseException;

import java.awt.*;
import java.util.*;

public class DataCellDef extends SimpleTextCellDef implements DataCell, DeferredTotalPagesCell
{
    private static final long serialVersionUID = 7L;
    protected JoriaAccess valueGetter;
	protected ArrayList<SummaryCell> totalsCells;
	protected ArrayList<AggregateDef> aggregates;

	public DataCellDef(TemplateModel parentGrid, JoriaAccess vg)
	{
		super(parentGrid, abbrev(vg.getName()));
		valueGetter = vg;
	}

	public DataCellDef(DataCellDef from, TemplateModel parentGrid, @SuppressWarnings("UnusedParameters") Map<Object, Object> alreadyCopiedViews)
	{
		super(from, parentGrid);
		if ((totalsCells != null && totalsCells.size() > 0) || (aggregates != null && aggregates.size() > 0))
			throw new NotYetImplementedError("copying of cells with totals on them");
		//final JoriaAccess newAxs = (JoriaAccess) alreadyCopiedViews.get(from.valueGetter);
		/*
		  if (newAxs!= null)
			  valueGetter = newAxs;
		  else
  */
		valueGetter = from.valueGetter;
	}

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences)
	{
		return new DataCellDef(this, newContainerGrid, copiedReferences);
	}

    public void refreshName()
	{
		myText = abbrev(valueGetter.getName());
	}

	public JoriaAccess getAccessor()
	{
		return valueGetter;
	}

	public String getDisplayMode()
	{
		return "Text";
	}

	public void setAggregates(ArrayList<AggregateDef> aggs)
	{
		aggregates = aggs;
	}

	public void addAggregate(AggregateDef a)
	{
		if (aggregates == null)
		{
			setAggregates(new ArrayList<AggregateDef>());
		}
		aggregates.add(a);
	}

	public ArrayList<AggregateDef> getAggregates()
	{
		return aggregates;
	}

	public String toString()
	{
		return "DataCellDef(" + valueGetter.getLongName() + ")";
	}

	public String getFormattedString(DBData from, AggregateCollector into) throws JoriaDataException
	{
		DBData cv = valueGetter.getValue(from, valueGetter, into.getRunEnv());
		/*exp hmf 050809 caused duplicate aggregation, because RunRepeater also accumulates
	   if (aggregates != null && !(cv == null || cv.isNull()))
	       into.accumulate(aggregates, cv);
	   */
		if (cv == null)
			return null;
		return wrapText(into.format(cv, getCascadedStyle()), getCascadedStyle(), into.getRunEnv().getLocale());
	}

	public String getValueString(DBData from, AggregateCollector into) throws JoriaDataException
	{
		DBData cv = valueGetter.getValue(from, valueGetter, into.getRunEnv());
		if (cv == null)
			return null;
		return wrapText(into.format(cv, getCascadedStyle()), getCascadedStyle(), into.getRunEnv().getLocale());
	}

	public void addTotalsCell(SummaryCell cell)
	{
		if (totalsCells == null)
			totalsCells = new ArrayList<SummaryCell>(1);
		totalsCells.add(cell);
	}

	public ArrayList<SummaryCell> getTotalsCells()
	{
		return totalsCells;
	}

	public void fixAccess()
	{
		if (valueGetter == null)
			return;
		JoriaAccess fixed = valueGetter.getPlaceHolderIfNeeded();
		if (fixed != null)
			valueGetter = fixed;
		if (fixed instanceof JoriaPlaceHolderAccess || fixed instanceof JoriaModifiedAccess)
			makeModified();
	}

	public void makeModified()
	{
		valueGetter = UnboundMembersClass.getUnboundLiteral();
		myText = "?" + valueGetter.getName() + " " + myText;
		CellStyle ps = new CellStyle();
		ps.setBackground(Color.orange);
		myStyle = ps;
		/*
		if (myStyle != null)
		{
		CellStyle ps = new CellStyle();
		ps.setBaseStyle(myStyle);
		ps.setBackground(ModelStaticStorage.getLoading().theCellStyleDefaultProblemStyle.getBackground()); // hier verreckt er mit null pointer
		myStyle = ps;
		}
		else
		    myStyle = ModelStaticStorage.getLoading().theCellStyleDefaultProblemStyle;
		*/
	}

	public void makeName()
	{
		myText = abbrev(valueGetter.getName());
		myWidth = Float.NaN;
		myHeight = Float.NaN;
		grid.fireChange("DataCellDef displayName changed");
	}

	public String getAccessorName()
	{
		return valueGetter.getLongName();
	}

	public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
	{
		if (from == null || from.isNull())
			return null;
		if (repeater != null)
		{
			DBCollection source = (DBCollection) from;
			JoriaType myType = valueGetter.getType();
			final int length = Math.max(source.getLength(), RVStringCol.startSize);
			RVStringCol col;
			if (myType.isRealLiteral())
			{
				if (valueGetter instanceof PercentageAccess)
					col = new RVPercentCol(length);
				else
					col = new RVFloatCol(length);
			}
			else if (myType.isIntegerLiteral())
			{
				col = new RVIntCol(length);
			}
			else if (myType.isBooleanLiteral())
				col = new RVBooleanCol(length);
			else if (myType.isDate())
				col = new RVDateCol(length);
			else
				col = new RVStringCol(length);
			return col;
		}
		else
		{
			if (!isVisible(outMode, from))
				return RVSupressHeader.instance;
			try
			{
				String v = getValueString(from, outMode.getRunEnv().getPager());
				if (v != null)
					return new RVString(v, getCascadedStyle(), g);
			}
			catch (JoriaDataRetrievalExceptionInUserMethod e)
			{
				return new RVString(JoriaAccess.ACCESSERROR, getCascadedStyle(), g);
			}
		}
		return null;
	}

	protected String getGraphElemString(TableBorderRequirements tblReq, int iter, FillPagedFrame out)
	{
		JoriaDeferedQuery deferredQuery = ComputedField.getIfDeferredQuery(false, valueGetter);
		if (deferredQuery != null)
		{
			try
			{
				DBData cv = deferredQuery.getDeferredValue(out.getRunEnv());
				if (cv == null || cv.isNull())
					return null;
				String into = out.getRunEnv().getPager().format(cv, getCascadedStyle());
				String v = wrapText(into, getCascadedStyle(), out.getRunEnv().getLocale());
				tblReq.value = new RVString(v, 1);
				return v;
			}
			catch (JoriaDataException e)
			{
				Env.instance().handle(e);
				return null;
			}
		}
		String s;
		if (tblReq.value != null && tblReq.value instanceof RValue)// must check if value is an RValue in case the user has put a table into the header of another table
		{
			final RValue rValue = (RValue) tblReq.value;
			s = rValue.get(iter);
			if (s != null)
			{
				if (aggregates != null)
					((RValue) tblReq.value).accumulate(out.getPageRun(), aggregates, iter);
			}
		}
		else
		{
			s = null;
		}
		return s;
	}

	public float getMaxWidth(RVAny values, Locale loc, Graphics2D g)
	{
		if (values == null || values == RVSupressHeader.instance)
			return 0;
		else if (values instanceof RVStringCol)
		{
			((RVStringCol) values).buildFormattedStrings(this, loc);
			String[] strings = ((RVStringCol) values).get();
			float w = 0;
			CellStyle cs = getCascadedStyle();
			for (String string : strings)
			{
				if (string != null)
				{
					w = Math.max(w, cs.getWidth(string, g));
				}
			}
			return w;
		}
		else if (values instanceof RVString)
		{
			return super.getMaxWidth(values, loc, g);
		}
		else
			throw new JoriaAssertionError("Unhandled data value: " + values.getClass());
	}

    /*
	public String formatNumber(long val, Locale loc)
	{
		if (val == Long.MIN_VALUE + 1)
			return null;
		CellStyle cs = getCascadedStyle();
		String conv = cs.getNumberConversion();
		if (conv.equals(NumberAsDate.JAVADATE))
		{
			String lPat = Internationalisation.localize(cs.getDatePattern(), loc);
			SimpleDateFormat f = new SimpleDateFormat(lPat, loc);
			return f.format(new Date(val));
		}
		else if (conv.equals(NumberAsDate.POSIXDATE))
		{
			String lPat = Internationalisation.localize(cs.getDatePattern(), loc);
			SimpleDateFormat f = new SimpleDateFormat(lPat, loc);
			return f.format(new Date(val * 1000));
		}
		else if (conv.charAt(0) == '$')
		{
			RuntimeParameterLiteral var = Repository.instanceRuntime().variables.find(conv.substring(1));
			EnumValidator eva = (EnumValidator) var.getValidator();
			Object[] al = eva.getEntries(null);
			for (Object anAl : al)
			{
				StrNum entry = (StrNum) anAl;
				if (entry.num == val)
					return entry.name;
			}
			return Long.toString(val);
		}
		else
		{
			String lPat = Internationalisation.localize(cs.getIntPattern(), loc);
			DecimalFormat f = new DecimalFormat(lPat);
            f.setRoundingMode(Env.instenv().getRoundingMode());
			return f.format(val);
		}
		//return null;
	}
    */

	public void makeUnboundCell()
	{
		if (totalsCells != null || aggregates != null)
			valueGetter = UnboundMembersClass.unboundFor(valueGetter.getType());
		else
			valueGetter = UnboundMembersClass.getUnboundLiteral();
		myText = valueGetter.getName();
	}

	public void setAccessor(JoriaAccess a)
	{
		valueGetter = a;
		myText = abbrev(a.getName());
		layout = null;
		myWidth = Float.NaN;
		myHeight = Float.NaN;
		grid.fireChange("binding accessor: " + a.getName());
	}

	public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException
	{
		boolean ret = super.makeGraphicElement(tblReq, iter, out);
		JoriaDeferedQuery deferedQuery = ComputedField.getIfDeferredQuery(true, valueGetter);
		if (deferedQuery != null)
		{
			GraphElText from = (GraphElText) tblReq.grel;
			Trace.check(from);
			Trace.check(from.getStyle(), "No style for total pages text: " + from.getText());
			tblReq.grel = new GraphElTotalPages(from, this, from.getBackgroundImage());
			out.addTotalPages((GraphElTotalPages) tblReq.grel);
		}
		return ret;
	}

	public void collectVariables(Set<RuntimeParameter> v, Set<Object> seen)
	{
		if (valueGetter instanceof VariableProvider)
		{
			((VariableProvider) valueGetter).collectVariables(v, seen);
		}
	}

	public boolean visitAccesses(AccessVisitor visitor, Set<JoriaAccess> seen)
	{
		if (!visitor.visit(valueGetter))
			return false;
		if (valueGetter instanceof VisitableAccess)
		{
			return ((VisitableAccess) valueGetter).visitAllAccesses(visitor, seen);
		}
		return true;
	}

	public void getUsedAccessors(Set<JoriaAccess> s) throws OQLParseException
	{
		super.getUsedAccessors(s);
		s.add(valueGetter);
	}

	@Override
	public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc)
	{
		initLayouter();
		//p.setColor(Color.pink);
		//p.draw(new Rectangle2D.Float(x0,y0,w,h));
		p.setFont(cascadedStyle.getStyledFont());
		p.setColor(cascadedStyle.getForeground());
		/*
		if (cascadedStyle.getAlignmentHorizontal().isEnd())
		{
			float width = super.getWidth(loc);
			x0 = x0 + w - width;
		}
		else if (cascadedStyle.getAlignmentHorizontal().isMid())
		{
			float width = super.getWidth(loc);
			x0 = x0 + (w - width) / 2;
		}*/
		layout.paint(p, x0, y0, w, h, loc);
		/* testing
		  final Color pink = Color.pink.brighter();
		  p.setColor(pink);
		  float alpha = .3f;
		  p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		  p.fill(new Rectangle2D.Float(x0,y0,w,h));
  */
	}

	public void resetRunningTotals(AggregateCollector coll)
	{
		if (aggregates != null)
		{
			for (AggregateDef aggregateDef : aggregates)
			{
				coll.resetTotal(aggregateDef, AggregateDef.running);
			}
		}
	}

	public CellStyle getConditionalStyle(DBData val)
	{
		CellStyle cellStyle = super.getConditionalStyle(val);
		cellStyle.setBackground(Color.cyan);
		return cellStyle;
	}

	public void rebindByName(final JoriaClass newScope)
	{
		final String pName = valueGetter.getName();
		final JoriaAccess newAccess = newScope.findMemberIncludingSuperclass(pName);
		if (totalsCells != null || aggregates != null)
		{
			final JoriaType oriType = valueGetter.getType();
			if (JoriaClassHelper.isAssignableFrom(oriType, newAccess.getType()))
				valueGetter = newAccess;
			else
				valueGetter = UnboundMembersClass.unboundFor(oriType);
		}
		else
			valueGetter = newAccess;
		myText = valueGetter.getName();
	}

	public boolean hasText(final String text)
	{
		if (super.hasText(text))
			return true;
		if (valueGetter.getName().toLowerCase().contains(text))
			return true;
		if (valueGetter instanceof ComputedField)
		{
			final String filter = ((ComputedField) valueGetter).getFilter();
			return filter.toLowerCase().contains(text);
		}
		return false;
	}


	public boolean hasTotalPages()
	{
		if (valueGetter instanceof ComputedField)
		{
			ComputedField cf = (ComputedField) valueGetter;
			return cf.isNeedTotalPages();
		}
		return false;
	}

	public void postProcess(final GrahElPostprocess grel, final RunEnvImpl env, final int page, final int totalPages) throws JoriaDataException
	{
		JoriaDeferedQuery deferredQuery = ComputedField.getIfDeferredQuery(true, valueGetter);
		Trace.check(deferredQuery);
		try
		{
			if (deferredQuery.isNeedsAllPages())
			{
				//DBData cv = valueGetter.getValue(from, valueGetter, out.getRunEnv());
				DBData cv = deferredQuery.getDeferredValue(env);
				if (cv == null || cv.isNull())
					grel.setText("", env.getGraphics2D());
				String into = env.getPager().format(cv, getCascadedStyle());
				String v = wrapText(into, getCascadedStyle(), env.getLocale());
				grel.setText(v, env.getGraphics2D());
			}
		}
		catch (JoriaDataException e)
		{
			Env.instance().handle(e);
		}
	}
}
