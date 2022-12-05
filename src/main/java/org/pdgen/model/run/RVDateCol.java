// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.cells.SimpleTextCellDef;
import org.pdgen.model.style.CellStyle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RVDateCol extends RVStringCol {

    Date[] vals;

    public RVDateCol(Date[] vals) {
        this.vals = vals;
    }

    public RVDateCol() {

    }

    public RVDateCol(int size) {
        vals = new Date[size];
    }

    public void add(int at, DBObject o, CellDef rcd, OutputMode env) throws JoriaDataException {
        checkBuffer(at);
        try {
            JoriaAccess axs = ((DataCellDef) rcd).getAccessor();
            if (axs.isAccessTyped()) {
                vals[at] = ((JoriaAccessTyped) axs).getDateValue(o, env.getRunEnv());
            } else {
                DBData v = axs.getValue(o, axs, env.getRunEnv());
                if (v != null && !v.isNull())
                    vals[at] = ((DBDateTime) v).getDate();
            }
        } catch (JoriaDataRetrievalExceptionInUserMethod e) {
            vals[at] = null;
        }
    }

    void checkBuffer(int at) {
        if (vals == null || at >= vals.length) // kein Buffer oder zuklein
        {
            int oldSize = (vals == null ? 0 : vals.length);
            int newSize = calculateNewBufferSize(oldSize, at + 1);
            Date[] newArray = new Date[newSize];
            if (strings != null) {
                System.arraycopy(vals, 0, newArray, 0, oldSize);
            }
            vals = newArray;
        }
    }

    public int getSize() {
        return vals.length;
    }

    public void buildFormattedStrings(CellDef cd, Locale loc) {
        if (strings != null)
            return;
        CellStyle cs = cd.getCascadedStyle();
        strings = new String[vals.length];
        String lPat = Internationalisation.localize(cs.getDatePattern(), loc);
        SimpleDateFormat f = new SimpleDateFormat(lPat, loc);
        for (int i = 0; i < vals.length; i++) {
            if (vals[i] == null)
                strings[i] = null;
            else {
                String s = f.format(vals[i]);
                strings[i] = SimpleTextCellDef.wrapText(s, cs, loc);
            }
        }
    }
}
