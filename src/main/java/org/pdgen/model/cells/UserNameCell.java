// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.DBData;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.run.AggregateCollector;

import java.util.Map;

public class UserNameCell extends SimpleTextCellDef implements EnvValueCell
{
    private static final long serialVersionUID = 7L;

    public UserNameCell(TemplateModel parentGrid)
	{
		super(parentGrid, "$user");
	}

	public UserNameCell(UserNameCell from, TemplateModel parentGrid)
	{
		super(from, parentGrid);
	}

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new UserNameCell(this, newContainerGrid);
	}

	public String getFormattedString(DBData from, AggregateCollector into)
	{
		String userName = System.getProperty("user.name");
		return SimpleTextCellDef.wrapText(userName, getCascadedStyle(), into.getRunEnv().getLocale());
	}
	public String getFormattedString(AggregateCollector into)
	{
        return System.getProperty("user.name");
	}
}
