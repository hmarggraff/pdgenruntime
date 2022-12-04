// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.Trace;
import org.pdgen.data.Internationalisation;
import org.pdgen.env.Settings;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;

import java.util.Stack;
import java.awt.*;
import java.util.Map;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class PagestotalCell extends SimpleTextCellDef implements EnvValueCell, DeferredTotalPagesCell
{
    private static final long serialVersionUID = 7L;
    String prefix;
    String midtext;
    String suffix;
    boolean showPageno;
    boolean showPagecount = true;

    public PagestotalCell(TemplateModel parentGrid, String prefix, String midtext, String suffix,
                          boolean showPageno, boolean showPagecount)
    {
        super(parentGrid, "$pageXofY");
        this.prefix = prefix;
        this.midtext = midtext;
        this.suffix = suffix;
        this.showPageno = showPageno;
        this.showPagecount = showPagecount;
        myText = getOutString("x", "y");
    }

    public PagestotalCell(TemplateModel parentGrid)
    {
        super(parentGrid, "$pageXofY");
    }

    public PagestotalCell(PagestotalCell from, TemplateModel parentGrid)
    {
        super(from, parentGrid);
        prefix = from.prefix;
        midtext = from.midtext;
        suffix = from.suffix;
        showPageno = from.showPageno;
        showPagecount = from.showPagecount;
    }

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new PagestotalCell(this, newContainerGrid);
	}

    public String getFormattedString(DBData from, AggregateCollector into)
    {
        return SimpleTextCellDef.wrapText(into.format(into.getDisplayPageNo() + 1, getCascadedStyle()), getCascadedStyle(), into.getRunEnv().getLocale());
    }

    public String getFormattedString(AggregateCollector into)
    {
        final String intPattern = getCascadedStyle().getIntPattern();
        Locale locale = into.getRunEnv().getLocale();
        String lPat = Internationalisation.localize(intPattern, locale);
        DecimalFormat f = new DecimalFormat(lPat, new DecimalFormatSymbols(locale));
        f.setRoundingMode(Settings.getRoundingMode());
        String p = f.format(into.getDisplayPageNo() + 1);
        return getOutString(p,"99999");
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
    {
        if (!isVisible(outMode, from))
            return RVSupressHeader.instance;
		String i = outMode.getPageNumber();
        return new RVString(getOutString(i, outMode.getTotalPageNumberPlaceHolder()), getCascadedStyle(), g); // this is just a pseudo value, that acts as a placeholder, so that it is not null
    }

	protected String getGraphElemString(TableBorderRequirements tblReq, int iter, FillPagedFrame out)
	{
        return SimpleTextCellDef.wrapText(getFormattedString(out.getPageRun()), getCascadedStyle(), out.getRunEnv().getLocale());
	}

    public boolean makeGraphicElement(TableBorderRequirements tblReq, int iter, FillPagedFrame out) throws JoriaDataException
    {
        if(!showPagecount)
        {
            return super.makeGraphicElement(tblReq, iter, out);
        }
        final boolean endOfPage = super.makeGraphicElement(tblReq, iter, out);
        if (endOfPage)
            return true;
        GraphElText from = (GraphElText) tblReq.grel;
        Trace.check(from);
        Trace.check(from.getStyle(), "No style for total pages text: " + from.getText());
        tblReq.grel = new GraphElTotalPages(from, this, from.getBackgroundImage());
        out.addTotalPages((GraphElTotalPages) tblReq.grel);
        return endOfPage;
    }

    public String getOutString(String pageno, String pagecount)
    {
        StringBuffer sb;
		if(prefix != null)
			sb = new StringBuffer(prefix);
		else
			sb = new StringBuffer();
		if (showPageno)
			sb.append(pageno);
        sb.append(midtext);
        if (showPagecount)
            sb.append(pagecount);
		if(suffix != null)
			sb.append(suffix);
        return sb.toString();
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public String getMidtext()
    {
        return midtext;
    }

    public void setMidtext(String midtext)
    {
        this.midtext = midtext;
    }

    public String getSuffix()
    {
        return suffix;
    }

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    public boolean isShowPageno()
    {
        return showPageno;
    }

    public void setShowPageno(boolean showPageno)
    {
        this.showPageno = showPageno;
    }

    public boolean hasTotalPages()
    {
        return showPagecount;
    }

	public void postProcess(final GrahElPostprocess grel, final RunEnvImpl env, final int page, final int totalPages)
	{
		final CellStyle cs = getCascadedStyle();

		final String intPattern = cs.getIntPattern();
		final Locale loc = env.getLocale();
		String lPat = Internationalisation.localize(intPattern, loc);
		DecimalFormat f = new DecimalFormat(lPat, new DecimalFormatSymbols(loc));
        f.setRoundingMode(Settings.getRoundingMode());
		String totalPagesString = f.format(totalPages);
		String currpageString = f.format(page);
		String res = getOutString(currpageString, totalPagesString);// build real text
		res = SimpleTextCellDef.wrapText(res, cs, loc);
		//float width = cs.getWidth(res, env.getGraphics2D());
		grel.setText(res, env.getGraphics2D());
	}

	public void setShowPagecount(boolean showPagecount)
    {
        this.showPagecount = showPagecount;
    }
}
