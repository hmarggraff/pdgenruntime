// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaClass;
import org.pdgen.model.Repeater;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.style.CellStyle;
import org.pdgen.model.style.PredefinedStyles;

import java.util.Map;

/**
 * This cell is created for the cells that are outside of a repeater, but belong to it.
 * the repeater may be a pseudo repeater (i.e. one without data fields)
 */
public class GroupGrandTotalCell extends DataCellDef {
    private static final long serialVersionUID = 7L;
    Repeater owningRepeater;

    public GroupGrandTotalCell(TemplateModel parentGrid, JoriaAccess vg, Repeater owningRepeater) {
        super(parentGrid, vg);
        this.owningRepeater = owningRepeater;
        CellStyle style;
        PredefinedStyles runtime = PredefinedStyles.instance();
        if (vg.getType().isStringLiteral())
            style = runtime.theCellStyleDefaultTotalStyle;
        else {
            style = runtime.theCellStyleDefaultTotalNumberStyle;
            if (style == null)
                style = runtime.theCellStyleDefaultNumberStyle;
        }
        setStyle(style);
        if (owningRepeater != null) {
            owningRepeater.addGroupGrandTotalCell(this);
        }
    }

    public GroupGrandTotalCell(GroupGrandTotalCell from, TemplateModel parentGrid, Map<Object, Object> copiedReferences) {
        super(from, parentGrid, copiedReferences);
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences) {
        GroupGrandTotalCell copy = new GroupGrandTotalCell(this, newContainerGrid, copiedReferences);
        copy.owningRepeater = (Repeater) copiedReferences.get(owningRepeater);
        if (copy.owningRepeater == null) {
            copy.owningRepeater = owningRepeater.duplicate(newContainerGrid, null, copiedReferences);
            copy.owningRepeater.addGroupGrandTotalCell(copy);
        }
        return copy;
    }

    public void setOwningRepeater(Repeater owningRepeater) {
        if (this.owningRepeater != null)
            this.owningRepeater.removeGroupGrandTotalCell(this);
        this.owningRepeater = owningRepeater;
        if (owningRepeater != null)
            owningRepeater.addGroupGrandTotalCell(this);
    }

    public Repeater getOwningRepeater() {
        return owningRepeater;
    }

    public void removed() {
        super.removed();
        if (owningRepeater != null)
            owningRepeater.removeGroupGrandTotalCell(this);
    }

    public void rebindByName(final JoriaClass newScope) {
        super.rebindByName(newScope);
        repeater.rebindByName(newScope);
    }
}
