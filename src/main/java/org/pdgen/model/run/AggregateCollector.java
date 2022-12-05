// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.data.view.AggregateDef;
import org.pdgen.env.Settings;
import org.pdgen.model.Template;
import org.pdgen.model.style.CellStyle;
import org.pdgen.projection.NumberAsDate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

public class AggregateCollector {
    Template template;
    //Hashtable totals = new Hashtable();
    TotalsAccumulator[] accumulators;
    Hashtable<String, Format> formats = new Hashtable<String, Format>();
    RunEnvImpl env;

    public AggregateCollector(RunEnvImpl env) {
        template = env.startTemplate;
        this.env = env;
        env.template = env.startTemplate;
        env.setPager(this);
        final ArrayList<AggregateDef> aggregates = template.getPage().getAggregates();
        accumulators = new TotalsAccumulator[template.getPage().getAggregates().size()];
        for (int i = 0; i < accumulators.length; i++) {
            AggregateDef a = aggregates.get(i);
            a.index = i;
            if (a.getSourceType() == AggregateDef.stringType)
                accumulators[i] = new TotalsAccumulatorString(a.getFunction());
            else
                accumulators[i] = new TotalsAccumulatorNumber(a.getFunction());
        }
    }

    public double getAggregateDouble(AggregateDef a, int scope) {
        TotalsAccumulator ta = accumulators[a.index];
        return ta.getDoubleVal(scope);
    }

    public String getAggregateString(AggregateDef a, int scope) {
        TotalsAccumulator ta = accumulators[a.index];
        return ta.getStringVal(scope);
    }

    public long getAggregateLong(AggregateDef a, int scope) {
        TotalsAccumulator ta = accumulators[a.index];
        return ta.getLongVal(scope);
    }

    public void resetTotals(int scope) {
        for (TotalsAccumulator accumulator : accumulators) {
            accumulator.reset(scope);
        }
    }

    public void accumulate(ArrayList<AggregateDef> a, DBData d) {
        AggregateDef a0 = a.get(0);
        int type = a0.getSourceType();
        if (type == AggregateDef.stringType) {
            String s = ((DBString) d).getStringValue();
            if (s != null)
                accumulate(a, s);
        } else if (type == AggregateDef.longType) {
            long s = ((DBInt) d).getIntValue();
            accumulate(a, s);
        } else {
            double s = ((DBReal) d).getRealValue();
            if (!Double.isNaN(s))
                accumulate(a, s);
        }
    }

    public void accumulate(ArrayList<AggregateDef> a, double s) {
        if (Double.isNaN(s))
            return;
        for (AggregateDef aa : a) {
            accumulators[aa.index].add(s);
        }
    }

    public void accumulate(ArrayList<AggregateDef> a, long s) {
        if (s == DBInt.NULL) // null or error
            return;
        for (AggregateDef anA : a) {
            accumulators[anA.index].add(s);
        }
    }

    public void accumulate(ArrayList<AggregateDef> a, String s) {
        for (AggregateDef anA : a) {
            accumulators[anA.index].add(s);
        }
    }

    public void completeRowAccus() {
        for (TotalsAccumulator accumulator : accumulators) {
            accumulator.rowComplete();
        }
    }

    public void backOutRowAccus() {
        for (TotalsAccumulator accumulator : accumulators) {
            accumulator.redoRow();
        }
    }

    protected DecimalFormat getDecimalFormat(String pattern, Locale loc) {
        DecimalFormat f = (DecimalFormat) formats.get(pattern);
        if (f == null) {
            String lPat = Internationalisation.localize(pattern, loc);
            f = new DecimalFormat(lPat, new DecimalFormatSymbols(loc));
            f.setRoundingMode(Settings.getRoundingMode());
            formats.put(pattern, f);
        }
        return f;
    }

    protected SimpleDateFormat getDateFormat(String pattern, Locale loc) {
        SimpleDateFormat f = (SimpleDateFormat) formats.get(pattern);
        if (f == null) {
            String lPat = Internationalisation.localize(pattern, loc);
            f = new SimpleDateFormat(lPat, loc);
            formats.put(pattern, f);
        }
        return f;
    }

    public String format(DBData val, CellStyle cs) {
        JoriaType t = val.getActualType();
        if (t.isIntegerLiteral()) {
            long v = ((DBInt) val).getIntValue();
            if (v == Long.MIN_VALUE)
                return null;
            String conv = cs.getNumberConversion();
            if (conv.equals(NumberAsDate.JAVADATE))
                return format(new Date(v), cs);
            else if (conv.equals(NumberAsDate.POSIXDATE))
                return format(new Date(v * 1000), cs);
            else
                return format(v, cs);
        } else if (t.isStringLiteral()) {
            return ((DBString) val).getStringValue();
            //exp hmf 020705 this method is only used for data cells, which do not get lokalized
            //return Internationalisation.localize(((DBString)val).getStringValue());
        } else if (t.isRealLiteral()) {
            double dval = ((DBReal) val).getRealValue();
            if (Double.isNaN(dval))
                return null;
            DecimalFormat f = getDecimalFormat(cs.getFloatPattern(), getRunEnv().getLocale());
            return f.format(dval);
        } else if (t.isDate()) {
            SimpleDateFormat f = getDateFormat(cs.getDatePattern(), getRunEnv().getLocale());
            return f.format(((DBDateTime) val).getDate());
        } else
            return val.toString();
    }

    public String format(long val, CellStyle cs) {
        if (val == Long.MIN_VALUE)
            return null;
        DecimalFormat f = getDecimalFormat(cs.getIntPattern(), getRunEnv().getLocale());
        return f.format(val);
    }

    public String format(double val, CellStyle cs) {
        if (Double.isNaN(val))
            return null;
        DecimalFormat f = getDecimalFormat(cs.getFloatPattern(), getRunEnv().getLocale());
        return f.format(val);
    }

    public String format(Date val, CellStyle cs) {
        SimpleDateFormat f = getDateFormat(cs.getDatePattern(), getRunEnv().getLocale());
        return f.format(val);
    }

    public int getMasterIndex() // the default is not to use master/detail logic so we are always the first master
    {
        return 0;
    }

    public int getDisplayPageNo() // the default is not to count pages
    {
        return 0;
    }

    public RunEnvImpl getRunEnv() {
        return env;
    }

    public DBData getRootVal() {
        return env.getRootVal();
    }

    public Template getStartingReport() {
        return env.startTemplate;
    }

    public void resetTotal(AggregateDef aggregateDef, int scope) {
        accumulators[aggregateDef.index].reset(scope);
    }

    public int getTotalPagesNumber() {
        return 0;
    }
}

