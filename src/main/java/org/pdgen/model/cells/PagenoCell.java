// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.*;

import java.util.Stack;
import java.awt.*;
import java.util.Map;

public class PagenoCell extends SimpleTextCellDef implements EnvValueCell
{
    private static final long serialVersionUID = 7L;

    public PagenoCell(TemplateModel parentGrid)
    {
        super(parentGrid, "page()");
    }

    public PagenoCell(PagenoCell from, TemplateModel parentGrid)
    {
        super(from, parentGrid);
    }

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new PagenoCell(this, newContainerGrid);
	}

    public String getFormattedString(DBData from, AggregateCollector into)
    {
        return SimpleTextCellDef.wrapText(into.format(into.getDisplayPageNo() + 1, getCascadedStyle()), getCascadedStyle(), into.getRunEnv().getLocale());
    }

    public String getFormattedString(AggregateCollector into)
    {
        return into.format(into.getDisplayPageNo() + 1, getCascadedStyle());
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
    {
        if (!isVisible(outMode, from))
            return RVSupressHeader.instance;
		String i = outMode.getPageNumber();
        return new RVString(i, getCascadedStyle(), g); // this is just a pseudo value, that acts as a placeholder, so that it is not null
    }

	protected String getGraphElemString(TableBorderRequirements tblReq, int iter, FillPagedFrame out)
	{
		return SimpleTextCellDef.wrapText(getFormattedString(out.getPageRun()), getCascadedStyle(), out.getRunEnv().getLocale());
	}
}
