// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import org.pdgen.env.JoriaExternalErrorWrapped;
import org.pdgen.env.Res;

import java.text.Collator;
import java.util.Calendar;
import java.util.Comparator;

public class ProjectionComparator implements Comparator<DBData>
{
	static Collator collator;

	protected SortOrder[] sortRules;
	RunEnv env;

	public ProjectionComparator(SortOrder[] sortRules, RunEnv env)
	{
		this.sortRules = sortRules;
		this.env = env;
		collator = Collator.getInstance(env.getLocale());
		Trace.check(sortRules.length > 0, "Empty sort rules array should be null");
	}

	public int compare(DBData o1, DBData o2)
	{
		DBData d1 = o1;
		DBData d2 = o2;
		JoriaType t1 = d1.getActualType();
		JoriaType t2 = d2.getActualType();
		if ((t1.isLiteral() && t2.isLiteral()) || (t1.isDate() && t2.isDate()))
		{	//TEST  sorting of literal collections
			return compare1(d1, d2, sortRules[0]);
		}
		else
		{
            for (SortOrder sortRule : sortRules)
            {
                JoriaAccess a = sortRule.getBaseAccess();
                try
                {
                    DBData m1 = a.getValue(d1, a, env);
                    DBData m2 = a.getValue(d2, a, env);
                    int ret = compare1(m1, m2, sortRule);
                    if (ret != 0)
                        return ret;
                }
                catch (JoriaDataException ex)
                {
                    Trace.log(ex);
                    //ex.printStackTrace();
                    throw new JoriaExternalErrorWrapped(Res.asis("JoriaDataException caught in sorter when accessing ") + a.getLongName() + " " + ex, ex);
                }
            }
			return 0;
		}
	}

	public int compare1(DBData d1, DBData d2, SortOrder sortRule)
	{
		int ret = -2;
		if (d1 == null || d1.isNull())
			ret = (d2 == null || d2.isNull()) ? 0 : -1;
		else if (d2 == null || d2.isNull())
			ret = 1;
		if (ret != -2)
		{
			if (sortRule.isAscending())
				return ret;
			else
				return -ret;
		}
		// now d1 and d2 cannot be null
		JoriaType t1 = d1.getActualType();
		JoriaType t2 = d2.getActualType();
		if (t1 != t2)
		{
			throw new JoriaAssertionError("Illegal type combination in ProjectionComparator " + t1 + "=" + t2);
		}
		if (!(t1.isLiteral() || t1.isDate()))
		{
			throw new JoriaAssertionError("Try to compare non literal type " + t1);
		}

		//System.out.println(Res.asis("comparing: ") + d1 + " <> " + d2);
		//TEST sorting with null values (e.g. Integer objects)
		if (t1.isIntegerLiteral())
		{
			long i1 = ((DBInt) d1).getIntValue();
			long i2 = ((DBInt) d2).getIntValue();
			if (i1 == i2)
				ret = 0;
			else if (i1 < i2)
				ret = -1;
			else
				ret = 1;
		}
		else if (t1.isStringLiteral())
		{
			if (sortRule.isCaseSensitive())
				collator.setStrength(Collator.TERTIARY);
			else
				collator.setStrength(Collator.IDENTICAL);
			ret = collator.compare(((DBString) d1).getStringValue(), ((DBString) d2).getStringValue());
		}
		else if (t1.isRealLiteral())
		{
			double i1 = ((DBReal) d1).getRealValue();
			double i2 = ((DBReal) d2).getRealValue();
			if (Double.isNaN(i1))
			{
				if (Double.isNaN(i2))
					return 0;
				else
					return -1;
			}
			else if (Double.isNaN(i2))
				return 1;
			if (i1 == i2)
				ret = 0;
			else if (i1 < i2)
				ret = -1;
			else
				ret = 1;
		}
		else if (t1.isBooleanLiteral())
		{
			//TEST: sorting of boolean attributes
			if (((DBBoolean) d1).getBooleanValue())
				ret = ((DBBoolean) d2).getBooleanValue() ? 0 : 1;
			else
				ret = ((DBBoolean) d2).getBooleanValue() ? -1 : 0;
		}
		else if (t1.isDate())
		{
			final Calendar c1 = ((DBDateTime) d1).getCalendar();
			final Calendar c2 = ((DBDateTime) d2).getCalendar();
			if (c1.before(c2))
				ret = -1;
			else if (c1.after(c2))
				ret = 1;
			else
				ret = 0;
		}
		if (ret == -2)
			throw new JoriaAssertionError("Illegal type combination in ProjectionComparator " + d1 + "=" + d2);
		if (sortRule.isAscending())
			return ret;
		else
			return -ret;
	}
}
