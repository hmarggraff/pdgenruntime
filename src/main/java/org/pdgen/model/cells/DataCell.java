// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaClass;

public interface DataCell extends CellDef, CellWithVariables {
    JoriaAccess getAccessor();

    String getDisplayMode();

    void rebindByName(final JoriaClass newScope);

}
