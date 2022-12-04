// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.*;
import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.styledtext.model.FloatDim;
import org.pdgen.styledtext.model.StyledParagraph;
import org.pdgen.styledtext.model.StyledParagraphLayouterList;
import org.pdgen.styledtext.model.StyledParagraphList;

import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.RDBase;
import org.pdgen.util.StringUtils;
import org.pdgen.env.Res;
import org.pdgen.model.TemplateModel;
import org.pdgen.oql.JoriaQuery;
import org.pdgen.oql.OQLParseException;

import java.awt.Graphics2D;
import java.util.*;

public class StyledTextCellDef extends FreeCellDef implements CellWithVariables
{
    private static final long serialVersionUID = 7L;
    protected StyledParagraphList myText;
	protected transient StyledParagraphLayouterList layout;
	//Set<RuntimeParameter> variables;
	//Set<JoriaAccess> usedAccessors;

	public StyledTextCellDef(StyledTextCellDef from, TemplateModel parentGrid)
	{
		super(from, parentGrid);
		myText = from.myText;
	}

	public StyledTextCellDef(TemplateModel parentGrid, String text)
	{
		super(parentGrid);
		myText = new StyledParagraphList(text);
	}

	public StyledTextCellDef(TemplateModel model, CellStyle newCs, CellStyle cascaded)
	{
		super(model);
		myStyle = newCs;
		myText = new StyledParagraphList(new StyledParagraph(" "));
		cascadedStyle = cascaded;
		float size = cascaded.getSize().getValInPoints();
		myText.setInitialAttributes(cascaded.getFont(), size, cascaded.getBold(), cascaded.getItalic(), cascaded.getUnderlined());
	}

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences)
	{
		return new StyledTextCellDef(this, newContainerGrid);
	}

	public static String wrapText(String txt, CellStyle cs, Locale loc)
	{
		String prefix = Internationalisation.localize(cs.getPrefix(), loc);
		String appendix = Internationalisation.localize(cs.getSuffix(), loc);
		if (prefix == null)
		{
			if (appendix == null)
				return txt;
			else if (txt == null)
				return appendix;
			else
				return txt + appendix;
		}
		else if (appendix == null)
		{
			if (txt == null)
				return prefix;
			else
				return prefix + txt;
		}
		else if (txt == null)
			return prefix + appendix;
		else
			return prefix + txt + appendix;
	}

	protected void adjustSize(Locale loc, Graphics2D g)
	{
		super.adjustSize(loc, g);
		if (myText == null || myText.length() == 0)
			return;
		FloatDim dim = myText.unbrokenWidth(g.getFontRenderContext());
		myWidth += dim.getWidth();
		myHeight += dim.getHeight();
	}

	public StyledParagraphList getParagraphs()
	{
		return myText;
	}

	public String getFormattedString(DBData from, AggregateCollector into) throws JoriaDataException
	{
		return wrapText(myText.getAsString(), getCascadedStyle(), into.getRunEnv().getLocale());
	}

	public void paint(Graphics2D p, float x0, float y0, float w, float h, Locale loc)
	{
		initLayouter(p);
		p.setFont(cascadedStyle.getStyledFont());
		p.setColor(cascadedStyle.getForeground());
		layout.paint(p, x0, y0, w, h);
	}

	private void initLayouter(Graphics2D p)
	{
		float innerWidth = myWidth - getCascadedStyle().getLeftRightPaddingValue();
		if (layout == null || innerWidth != layout.getTargetWidth())
		{
			layout = new StyledParagraphLayouterList(myText, p.getFontRenderContext(), innerWidth);
		}
		layout.recalc();
	}

	public void clearCachedStyle()
	{
		super.clearCachedStyle();//To change body of overridden methods use File | Settings | File Templates.
		layout = null;// throw away layouter in case text type has changed
	}

	public void setStyle(CellStyle sty)
	{
		super.setStyle(sty);
		layout = null;// throw away layouter in case text type has changed
	}

	// this is really only useful when a style requests reflowing of contents
	// the layouter is required to do the right thing. I.e use the width and set the height
	public void reFlow(float width, Locale loc, Graphics2D g)
	{
		myWidth = width;
		initLayouter(g);
		myHeight = layout.getHeight() + getCascadedStyle().getTopBotPaddingValue();
	}

	public float heightForWidth(float width, Locale loc, Graphics2D g)
	{
		reFlow(width, loc, g);
		return myHeight;
	}

	public boolean isReflowing()
	{
		return true;
	}

	public static String abbrev(String name)
	{
		return name;
	}

	public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
	{
		if (repeater == null)
		{
			if (!isVisible(outMode, from))
				return RVSupressHeader.instance;
			else
				return new RVStyledText(getStyledText((DBObject) from, outMode));
		}
		else
		{
			DBCollection source = (DBCollection) from;
			final int length = Math.max(source.getLength(), RVStringCol.startSize);
			RVStyledTextCol col = new RVStyledTextCol(length);
			return col;
		}
	}

	public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException
	{
		StyledParagraphList val = null;
		if (tblReq.value == RVSupressHeader.instance)
		{
			return out.makeNull(tblReq);
		}
		else if (tblReq.value instanceof RVStyledText)
		{
			val = ((RVStyledText) tblReq.value).getText();
		}
		else if (tblReq.value instanceof RVStyledTextCol)
		{
			val = ((RVStyledTextCol) tblReq.value).getText(iter);
		}
		if (val == null || val.length() == 0)
			return out.makeEmptyGrel(tblReq);
		else
			return out.makeStyledTextGraphel(tblReq, val);
	}

	public float getMaxWidth(RVAny values, Locale loc, Graphics2D g)
	{
		if (values == null)
			return 0;
		else if (values instanceof RVStyledTextCol)
		{
			float w = 0;
			RVStyledTextCol val = (RVStyledTextCol) values;
			for (int i = 0; i < val.getSize(); i++)
			{
				StyledParagraphList text = val.getText(i);
				if (text != null)
				{
					FloatDim dim = text.unbrokenWidth(g.getFontRenderContext());
					w = Math.max(w, dim.getWidth());
				}
			}
			return w;
		}
		else if (values instanceof RVStyledText)
		{
			StyledParagraphList text = ((RVStyledText) values).getText();
			FloatDim dim = text.unbrokenWidth(g.getFontRenderContext());
			return dim.getWidth();
		}
		else if (values instanceof RVSupressHeader)
		{
			return 0;
		}
		else
			throw new JoriaAssertionError("Unhandled data value: " + values.getClass());
	}

	private synchronized HashSet<JoriaAccess> determineUsedAccessors(final boolean stopOnError)
	{
		HashSet<JoriaAccess> usedAccessors = new HashSet<JoriaAccess>();
		for (int i = 0; i < myText.length(); i++)
		{
			StyledParagraph paragraph = myText.get(i);
			getUsedAccessors(paragraph.toString(), getScope(), usedAccessors, stopOnError);
		}
		return usedAccessors;
	}

	public StyledParagraphList getStyledText(DBObject o, OutputMode env) throws JoriaDataException
	{
		//if (myText.getAsString().indexOf('{') < 0)
		//	return myText;
		ArrayList<StyledParagraph> newList = new ArrayList<>(myText.length());
		for (int i = 0; i < myText.length(); i++)
		{
			StyledParagraph sourceParagraph = myText.get(i);
			int at = sourceParagraph.indexOf('{', 0); // no replacements use paragraph as is
			if (at < 0)
			{
				newList.add(new StyledParagraph(sourceParagraph, true));
				continue;
			}
			StyledParagraph resultParagraph = new StyledParagraph(sourceParagraph, true);
			newList.add(resultParagraph);

			int lastAt = 0;
			while (at >= 0)
			{
				at++;
				if (at == 1 || sourceParagraph.charAt(at - 2) != '\\')
				{
					int end = getExpression(sourceParagraph, at);
					final String expr = sourceParagraph.get(at, end - at);
					final DBData dbData = computeSubexpression(o, env.getRunEnv(), expr, getScope());
					String replace;
					if (dbData != null && !dbData.isNull())
					{
						replace = env.getRunEnv().getPager().format(dbData, getCascadedStyle());
					}
					else
					{
						replace = "";
					}
					if (replace.indexOf('\n') != -1)
					{
						ArrayList<String> strings = new ArrayList<String>();
						int nlat;
						int lastlnat = 0;
						int lastLength = 0;
						while ((nlat = replace.indexOf('\n', lastlnat)) != -1)
						{
							String str = replace.substring(lastlnat, nlat);
							lastLength = str.length();
							strings.add(str);
							lastlnat = nlat + 1;
						}
						strings.add(replace.substring(lastlnat));
						lastAt = lastLength;
						resultParagraph.replace(at - 1, end + 1, strings, newList);
					}
					else
					{
						resultParagraph.replaceInStyle(at - 1, end + 1, replace);
						lastAt = -1 + replace.length() + at;
					}
				}
				at = sourceParagraph.indexOf('{', lastAt);
			}
		}
		StyledParagraph[] newValues = new StyledParagraph[newList.size()];
		return new StyledParagraphList(newList.toArray(newValues));
	}

	public void setStyledText(StyledParagraphList text)
	{
		myText = text;
		layout = null;
		myHeight = Float.NaN;
		myWidth = Float.NaN;
		grid.fireChange();
	}

	public void collectVariables(Set<RuntimeParameter> v, Set<Object> seen)
	{
		getUsedVariables(v);
	}

	public void getUsedVariables(final Set<RuntimeParameter> v)
	{
		for (int i = 0; i < myText.length(); i++)
		{
			String filter = StringUtils.trimNull(myText.get(i).toString());
			if (filter != null)
			{
				try
				{
					check(filter, v);
				}
				catch (OQLParseException ex)
				{
					break;
				}
			}
		}
	}

	public boolean visitAccesses(AccessVisitor visitor, Set<JoriaAccess> seen) // seen can be ignored, because styled text only references accesses, but does not define any
	{
		final HashSet<JoriaAccess> usedAccessors = determineUsedAccessors(visitor.stopAccessSearchOnError());
		for (JoriaAccess joriaAccess : usedAccessors)
		{
			if (!visitor.visit(joriaAccess))
				return false;
			if (joriaAccess instanceof VisitableAccess)
			{
				if (!((VisitableAccess) joriaAccess).visitAllAccesses(visitor, seen))
					return false;
			}
		}
		return true;
	}

	public boolean hasText(final String text)
	{
		if (super.hasText(text))
			return true;
		boolean ret = myText.hasText(text);
		return ret;
	}

	public void highlightText(final String s)
	{
		layout.highlightText(s);
	}

	/**
	 * searches may highlight text in the cell. This must be removed before other work is done. At the latest before saving.
	 */
	public void clearHighlites()
	{
		layout.clearHighlites();
	}

	private void check(String text, Set<RuntimeParameter> vars) throws OQLParseException
	{
		int at = text.indexOf("{");
		if (at < 0)
		{
			return;
		}
		while (at >= 0)
		{
			int lastAt;
			if (at == 0 || text.charAt(at - 1) != '\\')
			{
				at++;
				int end = getExpression(text, at);
				if (end < 0)
					throw new OQLParseException(Res.str("Incomplete_expression_Close_expression_with_or_write_as"), text, at);
				final String expr = text.substring(at, end);
				try
				{
					final JoriaQuery q = parseSubExpression(expr, getScope());
					if (q != null)
					{
						Set<RuntimeParameter> tVariables = q.getVariables();
						vars.addAll(tVariables);
					}
				}
				catch (OQLParseException ex)
				{
					ex.pos += at;
					throw ex;
				}
				lastAt = end + 1;
			}
			else
			{
				lastAt = at + 2;
			}
			at = text.indexOf("{", lastAt);
		}
	}
}
