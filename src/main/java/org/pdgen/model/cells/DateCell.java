// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.DBData;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.AggregateCollector;

import java.util.Date;
import java.util.Map;

public class DateCell extends SimpleTextCellDef implements EnvValueCell
{
    private static final long serialVersionUID = 7L;

    public DateCell(TemplateModel parentGrid)
	{
		super(parentGrid, "$date");
	}

	public DateCell(DateCell from, TemplateModel parentGrid)
	{
		super(from, parentGrid);
	}

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new DateCell(this, newContainerGrid);
	}

	public String getFormattedString(DBData from, AggregateCollector into)
	{
		return SimpleTextCellDef.wrapText(into.format(new Date(), getCascadedStyle()), getCascadedStyle(), into.getRunEnv().getLocale());
	}
	public String getFormattedString(AggregateCollector into)
	{
		return into.format(new Date(), getCascadedStyle());
	}
}
