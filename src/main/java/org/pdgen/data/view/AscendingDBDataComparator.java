// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import java.text.Collator;
import java.util.Calendar;
import java.util.Comparator;

public class AscendingDBDataComparator implements Comparator<DBData> {
    Collator collator;

    private JoriaType t1;

    public AscendingDBDataComparator(RunEnv env) {
        collator = Collator.getInstance(env.getLocale());
    }

    public int compare(DBData o1, DBData o2) {
        int ret = -2;
        if (o1 == null || o1.isNull())
            ret = (o2 == null || o2.isNull()) ? 0 : -1;
        else if (o2 == null || o2.isNull())
            ret = 1;
        if (ret != -2) {
            return ret;
        }
        // now d1 and d2 cannot be null
        if (t1 == null) {
            t1 = o1.getActualType();
            JoriaType t2 = o2.getActualType();
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
            long i1 = ((DBInt) o1).getIntValue();
            long i2 = ((DBInt) o2).getIntValue();
            if (i1 == i2)
                ret = 0;
            else if (i1 < i2)
                ret = -1;
            else
                ret = 1;
        } else if (t1.isStringLiteral()) {
            collator.setStrength(Collator.TERTIARY);
            ret = collator.compare(((DBString) o1).getStringValue(), ((DBString) o2).getStringValue());
        } else if (t1.isRealLiteral()) {
            double i1 = ((DBReal) o1).getRealValue();
            double i2 = ((DBReal) o2).getRealValue();
            if (i1 == i2)
                ret = 0;
            else if (i1 < i2)
                ret = -1;
            else
                ret = 1;
        } else if (t1.isBooleanLiteral()) {
            //TEST: sorting of boolean attributes
            if (((DBBoolean) o1).getBooleanValue())
                ret = ((DBBoolean) o2).getBooleanValue() ? 0 : 1;
            else
                ret = ((DBBoolean) o2).getBooleanValue() ? -1 : 0;
        } else if (t1.isDate()) {
            final Calendar c1 = ((DBDateTime) o1).getCalendar();
            final Calendar c2 = ((DBDateTime) o2).getCalendar();
            if (c1.before(c2))
                ret = -1;
            else if (c1.after(c2))
                ret = 1;
            else
                ret = 0;
        } else {
            collator.setStrength(Collator.IDENTICAL);
            ret = collator.compare(o1.toString(), o2.toString());
        }
        if (ret == -2)
            throw new JoriaAssertionError("Illegal type combination in ProjectionComparator " + o1 + "=" + o2);
        return ret;
    }
}