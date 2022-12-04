// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.style.PredefinedStyles;
import org.pdgen.data.JoriaAccess;
import org.pdgen.model.TemplateModel;

import java.util.Map;

public class CrosstabLabelCell extends DataCellDef implements CrosstabCell
{
    private static final long serialVersionUID = 7L;
    boolean dirIsX;

    public CrosstabLabelCell(CrosstabLabelCell from, TemplateModel parentGrid, Map<Object, Object> copiedReferences)
    {
        super(from, parentGrid, copiedReferences);
        dirIsX = from.dirIsX;

    }

    public CrosstabLabelCell(TemplateModel parentGrid, JoriaAccess vg, boolean isXDir)
    {
        super(parentGrid, vg);
        dirIsX = isXDir;
        if(isXDir)
            myStyle = PredefinedStyles.instance().theCellStyleDefaultCrosstabHorizonalHeaderStyle;
        else
            myStyle = PredefinedStyles.instance().theCellStyleDefaultCrosstabVerticalHeaderStyle;
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object,Object> copiedReferences)
	{
		return new CrosstabLabelCell(this, newContainerGrid, copiedReferences);
	}
}
