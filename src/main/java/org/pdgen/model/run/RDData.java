// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.env.Settings;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.style.CellStyle;
import org.pdgen.projection.NumberAsDate;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RDData {

    CellDef cd;

    public RDData(CellDef cd) {
        this.cd = cd;
    }

    public JoriaAccess getAccessor() {
        if (cd instanceof DataCellDef)
            return ((DataCellDef) cd).getAccessor();
        return null;
    }

    protected DecimalFormat getDecimalFormat(String pattern, Locale loc) {
        String lPat = Internationalisation.localize(pattern, loc);
        return new DecimalFormat(lPat);
    }

    public String[] format(double[] vals, Locale loc) {
        CellStyle cs = cd.getCascadedStyle();
        String[] strings = new String[vals.length];
        String lPat = Internationalisation.localize(cs.getFloatPattern(), loc);
        DecimalFormat f = new DecimalFormat(lPat);
        f.setRoundingMode(Settings.getRoundingMode());
        for (int i = 0; i < vals.length; i++) {
            if (Double.isNaN(vals[i]))
                strings[i] = null;
            else {
                strings[i] = f.format(vals[i]);
            }
        }
        return strings;
    }


    public String format(long val, Locale loc) {
        if (val == Long.MIN_VALUE + 1)
            return null;
        CellStyle cs = cd.getCascadedStyle();
        String conv = cs.getNumberConversion();
        if (conv.equals(NumberAsDate.JAVADATE)) {
            String lPat = Internationalisation.localize(cs.getDatePattern(), loc);
            SimpleDateFormat f = new SimpleDateFormat(lPat, loc);
            return f.format(new Date(val));
        } else if (conv.equals(NumberAsDate.POSIXDATE)) {
            String lPat = Internationalisation.localize(cs.getDatePattern(), loc);
            SimpleDateFormat f = new SimpleDateFormat(lPat, loc);
            return f.format(new Date(val * 1000));
        } else {
            String lPat = Internationalisation.localize(cs.getIntPattern(), loc);
            DecimalFormat f = new DecimalFormat(lPat);
            f.setRoundingMode(Settings.getRoundingMode());
            return f.format(val);
        }
        //return null;
    }

    public float getMaxWidth(RVAny v, Graphics2D g) {
        if (v instanceof RVStringCol) {
            //((RVStringCol) v).format(this);
            String[] strings = ((RVStringCol) v).get();
            float w = 0;
            CellStyle cs = cd.getCascadedStyle();
            for (String string : strings) {
                if (string != null) {
                    float rw = cs.getWidth(string, g);
                    w = Math.max(w, rw);
                }
            }
            String pf = cs.getPrefix();
            if (pf != null)
                w += cs.getWidth(pf, g);
            String sf = cs.getSuffix();
            if (sf != null)
                w += cs.getWidth(sf, g);
            return w;
        } else
            throw new JoriaAssertionError("RDData cant get width for a " + v.getClass().getName());
    }

    public String format(DBData val, Locale loc) {
        CellStyle cs = cd.getCascadedStyle();
        JoriaType t = val.getActualType();
        if (t.isIntegerLiteral()) {
            long v = ((DBInt) val).getIntValue();
            return format(v, loc);
        } else if (t.isStringLiteral()) {
            return ((DBString) val).getStringValue();
        } else if (t.isRealLiteral()) {
            double dval = ((DBReal) val).getRealValue();
            if (Double.isNaN(dval))
                return null;
            DecimalFormat f = getDecimalFormat(cs.getFloatPattern(), loc);
            return f.format(dval);
        } else if (t.isDate()) {
            SimpleDateFormat f = new SimpleDateFormat(cs.getDatePattern(), loc);
            return f.format(((DBDateTime) val).getDate());
        } else
            return val.toString();
    }

    public boolean isData() {
        return true;
    }

    public boolean isLabel() {
        return false;
    }
}