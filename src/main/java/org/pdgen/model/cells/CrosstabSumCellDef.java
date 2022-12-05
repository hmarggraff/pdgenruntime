// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.JoriaClass;
import org.pdgen.model.CrosstabAggregate;
import org.pdgen.model.TemplateModel;
import org.pdgen.model.style.PredefinedStyles;

import java.util.Map;

/**
 * User: patrick
 * Date: Oct 26, 2006
 * Time: 11:23:42 AM
 */
public class CrosstabSumCellDef extends DataCellDef implements CrosstabCell {
    private static final long serialVersionUID = 7L;
    CrosstabAggregate value;
    CrosstabLabelCell label;

    public CrosstabSumCellDef(CrosstabSumCellDef from, TemplateModel parentGrid, Map<Object, Object> copiedReferences) {
        super(from, parentGrid, copiedReferences);
        value = from.value;
        label = from.label;
    }

    public CrosstabSumCellDef(TemplateModel parentGrid, CrosstabAggregate value, CrosstabLabelCell label) {
        super(parentGrid, label.valueGetter);
        this.value = value;
        this.label = label;
        myText = value.toString() + "->" + label.myText;
        myStyle = PredefinedStyles.instance().theCellStyleDefaultNumberStyle;
    }

    public CellDef duplicate(TemplateModel newContainerGrid, Map<Object, Object> copiedReferences) {
        return new CrosstabSumCellDef(this, newContainerGrid, copiedReferences);
    }

    public void rebindByName(final JoriaClass newScope) {
        makeUnboundCell();
    }
}
