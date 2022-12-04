// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.NotYetImplementedError;
import org.pdgen.data.view.RuntimeParameter;

import java.util.Set;

public class MethodNode extends FieldNode
{
	// fields
	protected Node[] args;

	public MethodNode(JoriaAccess p0, Node[] p1)
	{
		super(p0);
		if (p1 != null)
			throw new NotYetImplementedError("Methods with args not implemented");
		args = p1;
	}

	public String getTokenString()
	{
		StringBuffer b = new StringBuffer();
		buildTokenString(b);
		return b.toString();
	}

	private void buildTokenString(final StringBuffer b)
	{
		b.append(ref.getName()).append('(');
		for (Node arg : args)
		{
			b.append(arg.getTokenString());
		}
		b.append(')');
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		collector.append(ref.getName()).append('(');
		for (NodeInterface arg : args)
		{
			arg.buildTokenStringWithRenamedAccess(access, newName, collector, 0);
		}
		collector.append(')');
	}

	public void getUsedAccessors(Set<JoriaAccess> s)
	{
		s.add(ref);
		for (Node arg : args)
		{
			arg.getUsedAccessors(s);
		}
	}

	public boolean hasText(final String text, final boolean searchLabels, final boolean searchData)
	{
		for (Node arg : args)
		{
			if (arg.hasText(text, searchLabels, searchData))
				return true;
		}
		return false;
	}

	public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen)
	{
		for (Node arg : args)
		{
			arg.collectVariables(set,seen);
		}
	}
}
