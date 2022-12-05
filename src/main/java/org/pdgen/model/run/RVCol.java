// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.DBObject;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.cells.CellDef;

public interface RVCol extends RVAny {
    void add(int at, DBObject o, CellDef rcd, OutputMode env) throws JoriaDataException;
}
