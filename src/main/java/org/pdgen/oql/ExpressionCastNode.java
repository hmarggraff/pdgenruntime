// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.ClassProjection;
import org.pdgen.model.run.RunEnv;


public class ExpressionCastNode extends BinaryOperatorNode implements JoriaTypedNode
{
	public ExpressionCastNode(ClassNameNode p0, NodeInterface p1)
	{
		super(NodeInterface.objectType, p0, p1);
	}

	public String getTokenString()
	{
		return left.getTokenString() + right.getTokenString();
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		final int newLevel = 9;
		optBrace(bindingLevel, newLevel, collector, '(');
		left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
		optBrace(bindingLevel, newLevel, collector, ')');
	}

	public JoriaType getType()
	{
		return ((JoriaTypedNode) left).getType();
	}

	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		final DBData d = right.getValue(env, p0);
		JoriaType type = getType();
		while (type.isView())
		{
			type = ((ClassProjection) type).getBase();
		}
		if (d != null && JoriaClassHelper.isAssignableFrom(d, type))
			return d;
		else
			return null;
	}

	public JoriaType getElementType()
	{
		final JoriaType joriaType = ((ClassNameNode) left).getElementType();
		if (joriaType.isCollection())
			return joriaType;
		return null;
	}
}
