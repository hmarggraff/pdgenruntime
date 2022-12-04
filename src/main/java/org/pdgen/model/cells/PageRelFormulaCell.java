// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.TemplateModel;
import org.pdgen.projection.ComputedField;

import java.util.Map;

public class PageRelFormulaCell extends DataCellDef {
    private static final long serialVersionUID = 7L;

    public PageRelFormulaCell(TemplateModel parentGrid, ComputedField axs)
    {
        super(parentGrid, axs);
    }

    public PageRelFormulaCell(PageRelFormulaCell from, TemplateModel parentGrid, Map<Object,Object> copiedReferences)
    {
        super(from, parentGrid, copiedReferences);
    }

	public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new PageRelFormulaCell(this, newContainerGrid, copiedReferences);
	}

}
