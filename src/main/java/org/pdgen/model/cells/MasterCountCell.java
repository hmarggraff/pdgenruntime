// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.DBData;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.AggregateCollector;

import java.util.Map;

public class MasterCountCell extends SimpleTextCellDef implements EnvValueCell
{
    private static final long serialVersionUID = 7L;

    public MasterCountCell(TemplateModel parentGrid)
	{
		super(parentGrid, "$mcount");
	}

	public MasterCountCell(MasterCountCell from, TemplateModel parentGrid)
	{
		super(from, parentGrid);
	}

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new MasterCountCell(this,newContainerGrid);
	}

    public String getFormattedString(DBData from, AggregateCollector into)
	{
		return SimpleTextCellDef.wrapText(into.format(into.getMasterIndex(), getCascadedStyle()), getCascadedStyle(), into.getRunEnv().getLocale());
	}
	public String getFormattedString(AggregateCollector into)
	{
		return into.format(into.getMasterIndex(), getCascadedStyle());
	}
}
