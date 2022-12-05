// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.JoriaAccess;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.projection.ComputedField;

import java.util.Set;

public class FormulaCollector implements CellVisitor {
    Set<JoriaAccess> formulaView;

    public FormulaCollector(Set<JoriaAccess> formulaView) {
        this.formulaView = formulaView;
    }

    public boolean visit(CellDef cd, int r, int c) {
        if (cd == null || cd.getRepeater() != null)
            return true;
        if (!(cd instanceof DataCellDef))
            return true;
        JoriaAccess ax = ((DataCellDef) cd).getAccessor();
        if (!(ax instanceof ComputedField))
            return true;
        formulaView.add(ax);
        return true;
    }
}
