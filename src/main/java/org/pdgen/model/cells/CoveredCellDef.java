// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.model.TemplateModel;

import java.io.ObjectStreamException;

public class CoveredCellDef extends FreeCellDef
{
    private static final long serialVersionUID = 7L;

    protected CoveredCellDef(TemplateModel containerGrid)
    {
        super(containerGrid);
    }

    protected Object readResolve() throws ObjectStreamException
    {
        if(getRepeater() != null)
        {
            return new FreeCellDef((TemplateModel) getModel(), getRepeater());
        }
        else
            return null;
    }
}
