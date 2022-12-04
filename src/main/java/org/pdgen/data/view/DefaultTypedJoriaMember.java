// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


import java.util.Date;

public abstract class DefaultTypedJoriaMember extends AbstractMember implements JoriaAccessTyped
{
    private static final long serialVersionUID = 7L;

    protected DefaultTypedJoriaMember(JoriaClass definingClass)
	{
		super(definingClass);
	}

	protected DefaultTypedJoriaMember(JoriaClass definingClass, String name)
	{
		super(definingClass, name);
	}

	protected DefaultTypedJoriaMember()
	{
	}

	public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

	public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

	public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

	public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

	public Date getDateValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
	}

    public Object getPictureValue(DBObject from, RunEnv env) throws JoriaDataException
    {
        throw new JoriaAssertionError("Can only get a String from a JoriaPlaceHolderAccess");
    }
}
