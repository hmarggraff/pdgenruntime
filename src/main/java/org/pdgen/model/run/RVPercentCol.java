// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.data.view.PercentageAccess;
import org.pdgen.env.Settings;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.cells.SimpleTextCellDef;
import org.pdgen.model.style.CellStyle;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class RVPercentCol extends RVFloatCol {
    public RVPercentCol(double[] vals) {
        super(vals);
    }

    public RVPercentCol() {
    }

    public void add(int at, DBObject o, CellDef rcd, OutputMode env) throws JoriaDataException {
        JoriaAccess axs = ((PercentageAccess) ((DataCellDef) rcd).getAccessor()).getBaseAccess();
        if (axs.getType().isIntegerLiteral()) {
            checkBuffer(at);
            try {
                if (axs.isAccessTyped()) {
                    vals[at] = ((JoriaAccessTyped) axs).getIntValue(o, env.getRunEnv());
                } else {
                    DBData v = axs.getValue(o, axs, env.getRunEnv());
                    if (v != null && !v.isNull())
                        vals[at] = ((DBInt) v).getIntValue();
                    else
                        vals[at] = Double.NaN;
                }
            } catch (JoriaDataRetrievalExceptionInUserMethod e) {
                vals[at] = Double.NaN;
            }
        } else
            super.add(at, o, rcd, env);
    }

    public RVPercentCol(int size) {
        super(size);
    }


    public void buildFormattedStrings(CellDef cd, Locale loc) {
        if (strings != null)
            return;
        double sum = 0;
        for (double val : vals) {
            if (!Double.isNaN(val))
                sum += val;
        }
        CellStyle cs = cd.getCascadedStyle();
        strings = new String[vals.length];
        String lPat = Internationalisation.localize(cs.getFloatPattern(), loc);
        DecimalFormat f = new DecimalFormat(lPat, new DecimalFormatSymbols(loc));
        f.setRoundingMode(Settings.getRoundingMode());
        for (int i = 0; i < vals.length; i++) {
            if (Double.isNaN(vals[i]) || sum == 0)
                strings[i] = null;
            else {
                double number = vals[i] / sum * 100;
                String s = f.format(number);
                strings[i] = SimpleTextCellDef.wrapText(s, cs, loc);
            }
        }
    }
}
