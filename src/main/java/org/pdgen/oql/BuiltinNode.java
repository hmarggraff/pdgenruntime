// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;
import org.pdgen.projection.PseudoAccess;

import org.pdgen.data.Internationalisation2;
import org.pdgen.data.I18nKeyHolderFailure;

import java.util.Set;
import java.util.HashMap;
import java.util.List;

public class BuiltinNode extends Node implements JoriaTypedNode
{

	// fields
	protected NodeInterface[] args;
	Exec exec;
    JoriaClass p2;

    public BuiltinNode(NodeInterface[] p0, Exec p1, JoriaClass p2)
	{
        this.p2 = p2;
        args = p0;
		exec = p1;
	}

	protected DBData[] getArgValues(RunEnv env, DBData p0) throws JoriaDataException
	{
		if (args != null)
		{
			DBData[] vals = new DBData[args.length];
			for (int i = 0; i < args.length; i++)
			{
				vals[i] = args[i].getWrappedValue(env, p0);
			}
			return vals;
		}
		return null;
	}

	/* ----------------------------------------------------------------------- getBooleanValue */
	public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		return ((Exec.ExecBoolean) exec).execute(env, p0, getArgValues(env, p0));
	}

	/* ----------------------------------------------------------------------- getCharacterValue */
	public char getCharacterValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		return ((Exec.ExecChar) exec).execute(env, p0, getArgValues(env, p0));
	}

	/* ----------------------------------------------------------------------- getFloatValue */
	public double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException
	{
        if(exec instanceof Exec.ExecReal)
            return ((Exec.ExecReal) exec).execute(env, p0, getArgValues(env, p0));
        else if(exec instanceof Exec.ExecInteger)
            return ((Exec.ExecInteger) exec).execute(env, p0, getArgValues(env, p0));
        else
            throw new JoriaAssertionError("try to get float from unexpected type "+exec.getClass());
    }

	public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		return ((Exec.ExecInteger) exec).execute(env, p0, getArgValues(env, p0));
	}

	public String getStringValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		return ((Exec.ExecString) exec).execute(env, p0, getArgValues(env, p0));
	}

	public String getTokenString()
	{
		StringBuffer b = new StringBuffer();
		buildTokenString(b);
		return b.toString();
	}

	private void buildTokenString(final StringBuffer b)
	{
		b.append(exec.getName()).append('(');
		for (NodeInterface arg : args)
		{
		    b.append(arg.getTokenString());
		}
		b.append(')');
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		collector.append(exec.getName()).append('(');
		for (NodeInterface arg : args)
		{
			arg.buildTokenStringWithRenamedAccess(access, newName, collector, 0);
		}
		collector.append(')');
	}


	public JoriaType getType()
	{
		return exec.getType(args);
	}

	public JoriaType getElementType()
	{
		return null;
	}

	/* ----------------------------------------------------------------------- getValue */
	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		if (exec instanceof Exec.ExecDBData)
			return ((Exec.ExecDBData) exec).execute(env, p0, getArgValues(env, p0));
		else if (exec instanceof Exec.ExecString)
			return new DBStringImpl(new PseudoAccess(DefaultStringLiteral.instance()), ((Exec.ExecString) exec).execute(env, p0, getArgValues(env, p0)));
		else if (exec instanceof Exec.ExecInteger)
			return new DBIntImpl(new PseudoAccess(DefaultIntLiteral.instance()), ((Exec.ExecInteger) exec).execute(env, p0, getArgValues(env, p0)));
		else
			throw new NotYetImplementedError("Generic Builtin for " + exec.getClass());

	}

	public boolean isCharacter()
	{
		return exec instanceof Exec.ExecChar;
	}

	public boolean isCollection()
	{
		if (exec instanceof Exec.ExecDBData)
		{
			Exec.ExecDBData edbd = (Exec.ExecDBData) exec;
			return edbd.getType(args).isCollection();
		}
		return false;
	}

	public boolean isLiteralCollection()
	{
		if (exec instanceof Exec.ExecDBData)
		{
			Exec.ExecDBData edbd = (Exec.ExecDBData) exec;
			return edbd.getType(args).isLiteralCollection();
		}
		return false;
	}

	public boolean isDictionary()
	{
		if (exec instanceof Exec.ExecDBData)
		{
			Exec.ExecDBData edbd = (Exec.ExecDBData) exec;
			return edbd.getType(args).isDictionary();
		}
		return false;
	}

	/* ----------------------------------------------------------------------- isInteger */
	public boolean isInteger()
	{
		return exec instanceof Exec.ExecInteger;
	}

	/* ----------------------------------------------------------------------- isNumber */
	public boolean isReal()
	{
		return exec instanceof Exec.ExecInteger || exec instanceof Exec.ExecReal;
	}

	/* ----------------------------------------------------------------------- isObject */
	public boolean isObject()
	{
		return exec instanceof Exec.ExecDBData;
	}

	/* ----------------------------------------------------------------------- isString */
	public boolean isString()
	{
		return exec instanceof Exec.ExecString;
	}

	public boolean isDate()
	{
		return exec instanceof Exec.ExecDate;
	}

	public boolean isBoolean()
	{
		return exec instanceof Exec.ExecBoolean;
	}

	public boolean hasMofifiedAccess()
	{
		if (args == null)
			return false;
        for (NodeInterface arg : args)
        {
            if (arg.hasMofifiedAccess())
                return true;
        }
		return false;
	}

	public boolean isPageRelative()
	{
		return exec.isPageRelative();
	}

    public boolean isNeedsAllPages()
    {
        return exec.isNeedsAllPages();
    }

    public void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException
    {
		if (args == null)
			return;
		for (NodeInterface arg : args) {
			arg.cacheDeferredFields(env, from);
		}
	}

	public void i18nKeys(HashMap<String, List<I18nKeyHolder>> collect)
	{
		if (exec == BuiltIns.biLocal)
		{
			if (args[0] instanceof StringNode)
			{
				Internationalisation2.collectI18nKeys(((StringNode) args[0]).value, collect, I18nKeyHolderFailure.instance);
			}
		}
	}

	public void getUsedAccessors(Set<JoriaAccess> s)
	{
        for (NodeInterface arg : args)
        {
            arg.getUsedAccessors(s);
        }
	}

	@Override
	public boolean hasText(final String text, final boolean searchLabels, final boolean searchData)
	{
		final boolean nameHasText = exec.getName().contains(text);
		if (nameHasText)
			return true;
		for (NodeInterface arg : args)
		 {
			 final boolean argHasText = arg.hasText(text, searchLabels, searchData);
			 if (argHasText)
				 return true;
		 }
		return false;
	}

	public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen)
	{
		for (NodeInterface arg : args)
		{
		    arg.collectVariables(set,seen);
		}

	}
}
