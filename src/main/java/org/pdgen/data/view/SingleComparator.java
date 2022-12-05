// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import java.text.Collator;
import java.util.Calendar;
import java.util.Comparator;

public class SingleComparator implements Comparator<DBData> {
    Collator collator;

    protected SortOrder sortRule;
    private JoriaType t1;

    public SingleComparator(SortOrder sortRule, RunEnv env) {
        this.sortRule = sortRule;
        collator = Collator.getInstance(env.getLocale());
        Trace.check(sortRule, "Empty sort rules not allowed");
    }

    public int compare(DBData o1, DBData o2) {
        DBData d1 = ((DBGroup) o1).getKeyValue();
        DBData d2 = ((DBGroup) o2).getKeyValue();
        int ret = -2;
        if (d1 == null || d1.isNull())
            ret = (d2 == null || d2.isNull()) ? 0 : -1;
        else if (d2 == null || d2.isNull())
            ret = 1;
        if (ret != -2) {
            if (sortRule.isAscending())
                return ret;
            else
                return -ret;
        }
        // now d1 and d2 cannot be null
        if (t1 == null) {
            t1 = d1.getActualType();
            JoriaType t2 = d2.getActualType();
            if (t1 != t2) {
                throw new JoriaAssertionError("Illegal type combination in SingleComparator " + t1 + "=" + t2);
            }
            if (t1.isCollection()) {
                throw new JoriaAssertionError("Try to compare non literal type " + t1);
            }
        }
        //System.out.println(Res.asis("comparing: ") + d1 + " <> " + d2);
        //TEST sorting with null values (e.g. Integer objects)
        if (t1.isIntegerLiteral()) {
            long i1 = ((DBInt) d1).getIntValue();
            long i2 = ((DBInt) d2).getIntValue();
            if (i1 == i2)
                ret = 0;
            else if (i1 < i2)
                ret = -1;
            else
                ret = 1;
        } else if (t1.isStringLiteral()) {
            if (sortRule.isCaseSensitive())
                collator.setStrength(Collator.TERTIARY);
            else
                collator.setStrength(Collator.IDENTICAL);
            ret = collator.compare(((DBString) d1).getStringValue(), ((DBString) d2).getStringValue());
        } else if (t1.isRealLiteral()) {
            double i1 = ((DBReal) d1).getRealValue();
            double i2 = ((DBReal) d2).getRealValue();
            if (i1 == i2)
                ret = 0;
            else if (i1 < i2)
                ret = -1;
            else
                ret = 1;
        } else if (t1.isBooleanLiteral()) {
            //TEST: sorting of boolean attributes
            if (((DBBoolean) d1).getBooleanValue())
                ret = ((DBBoolean) d2).getBooleanValue() ? 0 : 1;
            else
                ret = ((DBBoolean) d2).getBooleanValue() ? -1 : 0;
        } else if (t1.isDate()) {
            final Calendar c1 = ((DBDateTime) d1).getCalendar();
            final Calendar c2 = ((DBDateTime) d2).getCalendar();
            if (c1.before(c2))
                ret = -1;
            else if (c1.after(c2))
                ret = 1;
            else
                ret = 0;
        } else {
            if (sortRule.isCaseSensitive())
                collator.setStrength(Collator.TERTIARY);
            else
                collator.setStrength(Collator.IDENTICAL);
            ret = collator.compare(d1.toString(), d2.toString());
        }
        if (ret == -2)
            throw new JoriaAssertionError("Illegal type combination in ProjectionComparator " + d1 + "=" + d2);
        if (sortRule.isAscending())
            return ret;
        else
            return -ret;
    }
}
