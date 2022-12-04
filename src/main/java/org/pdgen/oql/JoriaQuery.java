// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;


import java.util.Set;
import java.util.HashSet;

public class JoriaQuery extends UnaryOperatorNode
{
	// fields
	protected String name;
	protected JoriaClass view;
	protected String query;

	public JoriaQuery(JoriaClass p0, NodeInterface p1, String query)
	{
		super(p1);
		this.query = query;
		view = p0;
	}

	public Set<RuntimeParameter> getVariables()
	{
		Set<RuntimeParameter> vars = new HashSet<RuntimeParameter>();
		collectVariables(vars, new HashSet<Object>());
		return vars;
	}

	public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		boolean val = sub.getBooleanValue(env, p0);
		return val;
	}

	public String getTokenString()
	{
		return sub.getTokenString();
	}

	public JoriaType getType()
	{
		if (isBoolean())
			return DefaultBooleanLiteral.instance();
		else if (isInteger())
			return DefaultIntLiteral.instance();
		else if (isReal())
			return DefaultRealLiteral.instance();
		else if (isString())
			return DefaultStringLiteral.instance();
		else if (isCharacter())
			return DefaultCharacterLiteral.instance();
		else if (isDate())
			return JoriaDateTime.instance();
		else if (sub instanceof JoriaTypedNode)
			return ((JoriaTypedNode) sub).getType();
		else
			throw new JoriaAssertionError("Unhandled type when getting query type");
	}

	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		return sub.getWrappedValue(env, p0);
	}

	/**
	 * if the query only represents a simple field access
	 * then this access is returned.
	 *
	 * @return the field that this query represtents or null
	 */
	public JoriaAccess getSimpleAccess()
	{
		if (sub instanceof FieldNode)
			return ((FieldNode) sub).getAccess();
		return null;
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		sub.buildTokenStringWithRenamedAccess(access,newName,collector, -1);
	}

}
