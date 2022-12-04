// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;


import java.util.Set;

public class FieldNode extends Node implements JoriaTypedNode
{
	protected JoriaAccess ref;
	boolean isDeferred;

	public FieldNode(JoriaAccess p0)
	{
		ref = p0;
	}

	public boolean getBooleanValue(RunEnv env, DBData b) throws JoriaDataException
	{
		if ((b == null || b.isNull()) && !isDeferred)
			return false;
		if (ref.getType().isBooleanLiteral())
		{
			final DBBoolean dbBoolean = ((DBBoolean) getValue(env, b));
			//noinspection SimplifiableIfStatement
			if (dbBoolean == null || dbBoolean.isNull())
				return false;
			return dbBoolean.getBooleanValue();
		}
		throw new JoriaAssertionError("getBoolean called for non boolean value");
	}

	public char getCharacterValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		if ((p0 == null || p0.isNull()) && !isDeferred)
			return DBInt.CHARNULL;
		if (ref.getType().isCharacterLiteral())
		{
			DBData value = getValue(env, p0);
			if (value == null || value.isNull())
				return DBInt.CHARNULL;
			return (char) ((DBInt) value).getIntValue();
		}
		throw new JoriaAssertionError("getBoolean called for non boolean value");
	}

	public DBCollection getCollection(RunEnv env, DBData p0) throws JoriaDataException
	{
		if (isDeferred)
			return (DBCollection) env.getRuntimeParameterValue(ref);
		else
			return (DBCollection) ref.getValue(p0, ref, env);
	}
	

	public double getFloatValue(RunEnv env, DBData from) throws JoriaDataException
	{
		if ((from == null || from.isNull()) && !isDeferred)
			return Double.NaN;
		if (ref.getType().isRealLiteral())
		{
			if (ref instanceof JoriaAccessTyped && !isDeferred)
				return ((JoriaAccessTyped) ref).getFloatValue((DBObject) from, env);
			DBReal r = (DBReal) getValue(env, from);
			if (r == null)
				return DBReal.NULL;
			return r.getRealValue();
		}
		else if (ref.getType().isIntegerLiteral())
		{
			if (ref instanceof JoriaAccessTyped && !isDeferred)
				return ((JoriaAccessTyped) ref).getIntValue((DBObject) from, env);
			DBInt dbs = (DBInt) getValue(env, from);
			if (dbs == null || dbs.isNull())
				return DBReal.NULL;
			return dbs.getIntValue();
		}
		throw new JoriaAssertionError("getFloat called for non float value");
	}

	public long getIntValue(RunEnv env, DBData from) throws JoriaDataException
	{
		if ((from == null || from.isNull()) && !isDeferred)
			return DBInt.NULL;
		if (ref.getType().isIntegerLiteral())
		{
			if (ref instanceof JoriaAccessTyped && !isDeferred)
				return ((JoriaAccessTyped) ref).getIntValue((DBObject) from, env);
			DBInt dbs = (DBInt) getValue(env, from);
			if (dbs == null || dbs.isNull())
				return DBInt.NULL;
			return dbs.getIntValue();
		}
		throw new JoriaAssertionError("getInt called for non int value");
	}

	public String getStringValue(RunEnv env, DBData from) throws JoriaDataException
	{
		if ((from == null || from.isNull()) && !isDeferred)
			return null;
		if (ref.getType().isStringLiteral())
		{
			if (ref instanceof JoriaAccessTyped && !isDeferred)
				return ((JoriaAccessTyped) ref).getStringValue((DBObject) from, env);
			DBString dbs = (DBString) getValue(env, from);
			if (dbs == null || dbs.isNull())
				return null;
			return dbs.getStringValue();
		}
		throw new JoriaAssertionError("getString called for non string value");
	}

	public String getTokenString()
	{
		return ref.getName();
	}

	public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel)
	{
		if (ref == access)
			collector.append(newName); // here the actual renaming occurs
		else
			collector.append(ref.getName());
	}


	public JoriaType getType()
	{
		return ref.getType();
	}

	public JoriaType getElementType()
	{
		if (ref.getType().isCollection())
			return ((JoriaCollection) ref.getType()).getElementType();
		return null;
	}

	public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException
	{
		if (isDeferred)
			return env.getRuntimeParameterValue(ref);
		else if (p0 == null || p0.isNull())
			return null;
		else
			return ref.getValue(p0, ref, env);
	}

	public boolean isBoolean()
	{
		return ref.getType().isBooleanLiteral();
	}

	public boolean isCharacter()
	{
		return ref.getType().isCharacterLiteral();
	}

	public boolean isCollection()
	{
		return ref.getType().isCollection();
	}

	public boolean isLiteralCollection()
	{
		return ref.getType().isLiteralCollection();
	}

	public boolean isDictionary()
	{
		return ref.getType().isDictionary();
	}

	public boolean isInteger()
	{
		return ref.getType().isIntegerLiteral();
	}

	public boolean isReal()
	{
		return ref.getType().isRealLiteral();
	}

	public boolean isObject()
	{
		return ref.getType().isClass();
	}

	public boolean isString()
	{
		return ref.getType().isStringLiteral();
	}

	public boolean isDate()
	{
		return ref.getType().isDate();
	}

	public boolean isBlob()
	{
		return ref.getType().isBlob();
	}

	public JoriaAccess getAccess()
	{
		return ref;
	}

	public void getUsedAccessors(Set<JoriaAccess> s)
	{
		s.add(ref);
	}

	public boolean hasText(final String text, final boolean searchLabels, final boolean searchData)
	{
		return searchData && ref.getName().toLowerCase().contains(text);
	}

	public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen)
	{
		if (ref instanceof VariableProvider && !seen.contains(ref))
		{
			VariableProvider vp = (VariableProvider) ref;
			seen.add(ref);
			vp.collectVariables(set, seen);
		}
	}
	public void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException
	{
		final DBData value = ref.getValue(from, ref, env);
		env.putRuntimeParameter(ref, value);
		isDeferred = true;
    }

}
