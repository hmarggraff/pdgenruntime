// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class SubtractNode extends BinaryOperatorNode
{
	public SubtractNode(int p0, NodeInterface p1, NodeInterface p2)
	{
		super(p0, p1, p2);
	}

	/**
	 * ----------------------------------------------------------------------- getFloatValue
	 */
	public double getFloatValue(RunEnv env, DBData p) throws JoriaDataException
	{
		double floatValueLeft = left.getFloatValue(env, p);
		double floatValueRight = right.getFloatValue(env, p);
		if (Double.isNaN(floatValueLeft) || Double.isNaN(floatValueRight))
			return DBReal.NULL;
        return floatValueLeft - floatValueRight;
	}

	/**
	 * ----------------------------------------------------------------------- getIntValue
	 */
	public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		long intValueLeft = left.getIntValue(env, p0);
		long intValueRight = right.getIntValue(env, p0);
		if (intValueLeft == DBInt.NULL || intValueRight == DBInt.NULL)
			return DBInt.NULL;
		return intValueLeft - intValueRight;
	}

	public String getTokenString()
	{
		return left.getTokenString() + '-' + right.getTokenString();
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 5;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append(" - ");
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		optBrace(bindingLevel, newLevel, collector, ')');
	}

	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
    {
        DBData leftValue = left.getValue(env, p0);
        DBData rightValue = right.getValue(env, p0);
        if(leftValue == null || leftValue.isNull() || rightValue == null || rightValue.isNull())
            return null;
        if(left.isInteger() && right.isInteger())
        {
            return new DBIntImpl(null, getIntValue(env, p0));
        }
        else
        {
            return new DBRealImpl(null, getFloatValue(env, p0));
        }
    }
}
