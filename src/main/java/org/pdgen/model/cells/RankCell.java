// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;
//MARKER The strings in this file shall not be translated

import org.pdgen.model.run.*;
import org.pdgen.model.style.CellStyle;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.RDBase;
import org.pdgen.model.TemplateModel;

import java.util.Locale;
import java.util.Stack;
import java.awt.*;
import java.util.Map;

public class RankCell extends SimpleTextCellDef
{
    private static final long serialVersionUID = 7L;

    public RankCell(TemplateModel parentGrid)
    {
        super(parentGrid, "$rank");
    }

    public RankCell(RankCell from, TemplateModel parentGrid)
    {
        super(from, parentGrid);
    }

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new RankCell(this, newContainerGrid);
	}

    public String getFormattedString(DBData from, AggregateCollector into)
    {
        return SimpleTextCellDef.wrapText("0", getCascadedStyle(), into.getRunEnv().getLocale());
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException
    {
        if (!isVisible(outMode, from))
            return RVSupressHeader.instance;
        return new RVRank();
    }

	protected String getGraphElemString(TableBorderRequirements tblReq, int iter, FillPagedFrame out)
	{
        String str = Integer.toString(iter+1); // start at 1
		return SimpleTextCellDef.wrapText(str, getCascadedStyle(), out.getRunEnv().getLocale());
	}

    public float getMaxWidth(RVAny values, Locale loc, Graphics2D g)
    {
        CellStyle cs = getCascadedStyle();
        return cs.getWidth("888888", g);
    }
}
