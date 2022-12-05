// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.GrahElPostprocess;
import org.pdgen.model.run.RunEnvImpl;

public interface DeferredTotalPagesCell extends CellDef {
    boolean hasTotalPages();

    void postProcess(GrahElPostprocess grel, RunEnvImpl env, final int page, final int totalPages) throws JoriaDataException;
}
