// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


import java.util.Calendar;

public class LessOrEqualNode extends BooleanOperatorNode
{

    public LessOrEqualNode(int p0, NodeInterface p1, NodeInterface p2)
    {
        super(p0, p1, p2);
    }

	/**
	 * ----------------------------------------------------------------------- getBooleanValue
	 */
	@SuppressWarnings("SimplifiableIfStatement")
    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		if (commonOperandType == intType)
		{
			long l = left.getIntValue(env, p0);
			long r = right.getIntValue(env, p0);
			if (l == DBInt.NULL || r == DBInt.NULL)
				return false;
			return l <= r;
		}
		else if (commonOperandType == stringType)
			return compareStrings(left.getStringValue(env, p0), right.getStringValue(env, p0), env) <= 0;
		else if (commonOperandType == realType)
		{
			double l = left.getFloatValue(env, p0);
			double r = right.getFloatValue(env, p0);
			if (Double.isNaN(l) || Double.isNaN(r))
				return false;
			return l <= r;
		}
		else if (commonOperandType == charType)
		{
			char l = left.getCharacterValue(env, p0);
			char r = right.getCharacterValue(env, p0);
			if (l == DBInt.CHARNULL || r == DBInt.CHARNULL)
				return false;
			return l <= r;
		}
		else if (commonOperandType == dateType)
		{
			final DBDateTime ld = (DBDateTime) left.getValue(env, p0);
			final DBDateTime rd = (DBDateTime) right.getValue(env, p0);
			if (ld == null || ld.isNull())
				return rd != null && !rd.isNull();
			if (rd == null || rd.isNull())
				return false;
			//Calendar t0 = ld.getCalendar();
			//String chk0 = t0.get(Calendar.YEAR) + "." + (t0.get(Calendar.MONTH) + 1) + "." + t0.get(Calendar.DAY_OF_MONTH) + " " + t0.get(Calendar.HOUR_OF_DAY);
			final Calendar t = rd.getCalendar();
			//String chk = t.get(Calendar.YEAR) + "." + t.get(Calendar.MONTH) + "." + t.get(Calendar.DAY_OF_MONTH) + " " + t.get(Calendar.HOUR_OF_DAY);
			return !ld.getCalendar().after(t);
		}
		else
			throw new JoriaAssertionError("Type mismatch detected when too late");
	}

	public String getTokenString()
	{
		return left.getTokenString() + " <= " + right.getTokenString();
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 4;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append(" <= ");
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		optBrace(bindingLevel, newLevel, collector, ')');
	}

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
    {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }
}
