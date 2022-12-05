// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.env.Settings;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.cells.SimpleTextCellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.projection.NumberAsDate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class RVIntCol extends RVStringCol {
    long[] vals;

    public RVIntCol(long[] vals) {
        this.vals = vals;
    }

    public RVIntCol() {
    }

    public RVIntCol(int size) {
        vals = new long[size];
        Arrays.fill(vals, DBInt.NULL);
    }

    public void add(int at, DBObject o, CellDef rcd, OutputMode env) throws JoriaDataException {
        checkBuffer(at);
        try {
            JoriaAccess axs = ((DataCellDef) rcd).getAccessor();
            if (axs.isAccessTyped()) {
                vals[at] = ((JoriaAccessTyped) axs).getIntValue(o, env.getRunEnv());
            } else {
                DBData v = axs.getValue(o, axs, env.getRunEnv());
                if (v != null && !v.isNull()) {
                    vals[at] = ((DBInt) v).getIntValue();
                } else
                    vals[at] = DBInt.NULL;
            }
        } catch (JoriaDataRetrievalExceptionInUserMethod e) {
            vals[at] = DBInt.NULL;
        }
    }

    void checkBuffer(int at) {
        if (vals == null || at >= vals.length) // kein Buffer oder zuklein
        {
            int oldSize = (vals == null ? 0 : vals.length);
            int newSize = calculateNewBufferSize(oldSize, at + 1);
            long[] newArray = new long[newSize];
            if (vals != null) {
                System.arraycopy(vals, 0, newArray, 0, oldSize);
                Arrays.fill(vals, oldSize, newSize - 1, DBInt.NULL);
            } else
                Arrays.fill(vals, DBInt.NULL);
            vals = newArray;
        }
    }

    public int getSize() {
        return vals.length;
    }

    public void buildFormattedStrings(CellDef formatter, Locale loc) {
        if (strings != null)
            return;
        CellStyle cs = formatter.getCascadedStyle();
        strings = new String[vals.length];
        String conv = cs.getNumberConversion();
        if (conv.equals(NumberAsDate.JAVADATE)) {
            String lPat = Internationalisation.localize(cs.getDatePattern(), loc);
            SimpleDateFormat f = new SimpleDateFormat(lPat, loc);
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] == Long.MIN_VALUE + 1)
                    strings[i] = null;
                else if (vals[i] == Long.MIN_VALUE + 2)
                    strings[i] = JoriaAccess.ACCESSERROR;
                else {
                    String s = f.format(new Date(vals[i]));
                    strings[i] = SimpleTextCellDef.wrapText(s, cs, loc);
                }
            }
        } else if (conv.equals(NumberAsDate.POSIXDATE)) {
            String lPat = Internationalisation.localize(cs.getDatePattern(), loc);
            SimpleDateFormat f = new SimpleDateFormat(lPat, loc);
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] == Long.MIN_VALUE + 1)
                    strings[i] = null;
                else if (vals[i] == Long.MIN_VALUE + 2)
                    strings[i] = JoriaAccess.ACCESSERROR;
                else {
                    String s = f.format(new Date(vals[i] * 1000)); // java date is in milliseconds; posix in seconds
                    strings[i] = SimpleTextCellDef.wrapText(s, cs, loc);
                }
            }
        } else {
            String lPat = Internationalisation.localize(cs.getIntPattern(), loc);
            DecimalFormat f = new DecimalFormat(lPat, new DecimalFormatSymbols(loc));
            f.setRoundingMode(Settings.getRoundingMode());
            for (int i = 0; i < vals.length; i++) {
                if (vals[i] == Long.MIN_VALUE + 1)
                    strings[i] = null;
                else if (vals[i] == Long.MIN_VALUE + 2)
                    strings[i] = JoriaAccess.ACCESSERROR;
                else {
                    String s = f.format(vals[i]);
                    strings[i] = SimpleTextCellDef.wrapText(s, cs, loc);
                }
            }
        }
    }

    public void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter) {
        collector.accumulate(aggregates, vals[iter]);
    }
}
