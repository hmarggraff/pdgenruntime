// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class EqualsNode extends BooleanOperatorNode
{
	public EqualsNode(int commonTypeKey, NodeInterface p1, NodeInterface p2)
	{
		super(commonTypeKey, p1, p2);
	}

	@SuppressWarnings("SimplifiableIfStatement")
    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		if (commonOperandType == intType)
		{
			long intValueLeft = left.getIntValue(env, p0);
			long intValueRight = right.getIntValue(env, p0);
            if (intValueLeft == DBInt.NULL || intValueRight == DBInt.NULL)
				return false;
			return intValueLeft == intValueRight;
		}
		else if (commonOperandType == stringType)
			return compareStrings(left.getStringValue(env, p0), right.getStringValue(env, p0), env) == 0;
		else if (commonOperandType == realType)
		{
			double floatValueLeft = left.getFloatValue(env, p0);
			double floatValueRight = right.getFloatValue(env, p0);
			if (Double.isNaN(floatValueLeft) || Double.isNaN(floatValueRight))
				return false;
			return floatValueLeft == floatValueRight;
		}
		else if (commonOperandType == charType)
		{
			char characterValueLeft = left.getCharacterValue(env, p0);
			char characterValueRight = right.getCharacterValue(env, p0);
			if (characterValueLeft == DBInt.CHARNULL || characterValueRight == DBInt.CHARNULL)
				return false;
			return characterValueLeft == characterValueRight;
		}
		else if (commonOperandType == dateType)
		{
			DBData valueLeft = left.getValue(env, p0);
			DBData valueRight = right.getValue(env, p0);
			if (valueLeft == null || valueLeft.isNull() || valueRight == null || valueRight.isNull())
				return false;
			return ((DBDateTime) valueLeft).getDate().compareTo(((DBDateTime) valueRight).getDate()) == 0;
		}
		else if (commonOperandType == objectType)
		{
			DBObject o1 = (DBObject) left.getValue(env, p0);
			DBObject o2 = (DBObject) right.getValue(env, p0);
			boolean b1 = o1 == null || o1.isNull();
			boolean b2 = o2 == null || o2.isNull();
			if (b1)
				return b2;
			else if (b2)
				return false;
			else
				return o1.same(o2);
		}
		else if (commonOperandType == collectionType)
		{
			DBCollection o1 = (DBCollection) left.getValue(env, p0);
			DBCollection o2 = (DBCollection) right.getValue(env, p0);
			boolean b1 = o1 == null || o1.isNull();
			boolean b2 = o2 == null || o2.isNull();
			if (b1)
				return b2;
			else if (b2)
				return false;
			else
				return o1.same(o2);
		}
		else if (commonOperandType == dictionaryType)
		{
			DBDictionary o1 = (DBDictionary) left.getValue(env, p0);
			DBDictionary o2 = (DBDictionary) right.getValue(env, p0);
			boolean b1 = o1 == null || o1.isNull();
			boolean b2 = o2 == null || o2.isNull();
			if (b1)
				return b2;
			else if (b2)
				return false;
			else
				return o1.same(o2);
		}
		else if (commonOperandType == literalCollectionType)
		{
			DBLiteralCollection o1 = (DBLiteralCollection) left.getValue(env, p0);
			DBLiteralCollection o2 = (DBLiteralCollection) right.getValue(env, p0);
			boolean b1 = o1 == null || o1.isNull();
			boolean b2 = o2 == null || o2.isNull();
			if (b1)
				return b2;
			else if (b2)
				return false;
			else
				return o1.same(o2);
		}
		else if (commonOperandType == booleanType)
        {
            boolean lValue = left.getBooleanValue(env, p0);
            boolean rValue = right.getBooleanValue(env, p0);
            return lValue == rValue;
        }
		else
			throw new JoriaAssertionError("Type mismatch detected when too late");
	}

	public String getTokenString()
	{
		return left.getTokenString() + " = " + right.getTokenString();
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 3;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		collector.append(" = ");
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		optBrace(bindingLevel, newLevel, collector, ')');
	}


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
    {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }
}
