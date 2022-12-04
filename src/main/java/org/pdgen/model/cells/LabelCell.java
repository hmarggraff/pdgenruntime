// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.TemplateModel;

import java.util.Map;

public class LabelCell extends TextCellDef
{
    private static final long serialVersionUID = 7L;

    public LabelCell(TemplateModel parentGrid)
	{
		super(parentGrid);
	}
	public LabelCell(TemplateModel parentGrid, String s)
	{
		super(parentGrid, s);
	}
	public LabelCell(TextCellDef from, TemplateModel parentGrid)
	{
		super(from, parentGrid);
	}

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new LabelCell(this, newContainerGrid);
	}
}
