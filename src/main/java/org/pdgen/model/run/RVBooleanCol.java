// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.cells.SimpleTextCellDef;


public class RVBooleanCol extends RVStringCol {
    public static final String FALSE = "false";
    public static final String TRUE = "true";

    public RVBooleanCol() {
    }

    public RVBooleanCol(int size) {
        super(size);
    }

    public void add(int at, DBObject o, CellDef rcd, OutputMode env) throws JoriaDataException {
        checkBuffer(at);
        try {
            JoriaAccess axs = ((DataCellDef) rcd).getAccessor();
            if (axs.isAccessTyped()) {
                int v = ((JoriaAccessTyped) axs).getBooleanValue(o, env.getRunEnv());
                if (v == JoriaAccessTyped.ValForTrue)
                    strings[at] = SimpleTextCellDef.wrapText(TRUE, rcd.getCascadedStyle(), null);
                else if (v == JoriaAccessTyped.ValForFalse)
                    strings[at] = SimpleTextCellDef.wrapText(FALSE, rcd.getCascadedStyle(), null);
                // else: leave as null

            } else {
                DBData v = axs.getValue(o, axs, env.getRunEnv());
                if (v != null && !v.isNull()) {
                    if (((DBBoolean) v).getBooleanValue())
                        strings[at] = SimpleTextCellDef.wrapText(TRUE, rcd.getCascadedStyle(), null);
                    else
                        strings[at] = SimpleTextCellDef.wrapText(FALSE, rcd.getCascadedStyle(), null);
                }
            }
        } catch (JoriaDataRetrievalExceptionInUserMethod e) {
            strings[at] = JoriaAccess.ACCESSERROR;
        }
    }
}
