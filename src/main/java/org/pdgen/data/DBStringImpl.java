// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.io.Serializable;

public class DBStringImpl extends AbstractDBData implements DBString, Serializable
{

    private static final long serialVersionUID = 7L;
    String myString;

	public DBStringImpl(JoriaAccess axs, String value)
	{
		super(axs);
		myString = value;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof DBString)
		{
			String stringValue = ((DBString) obj).getStringValue();
			if (stringValue != null)
				return stringValue.equals(myString);
		}
		return false;
	}

	/**
	 * ----------------------------------------------------------------------- getStringValue
	 */
	public String getStringValue()
	{
		return myString;
	}

	/**
	 * ----------------------------------------------------------------------- isNull
	 */
	public boolean isNull()
	{
		return myString == null;
	}

	/**
	 * ----------------------------------------------------------------------- toString
	 */
	public String toString()
	{
		return myString;
	}

	public int hashCode()
	{
		if (myString != null)
			return myString.hashCode();
		else
			return 0;
	}

	public boolean same(DBData theOther)
	{
		if (theOther instanceof DBStringImpl)
		{
			return ((DBStringImpl) theOther).myString.equals(myString);
		}
		else
		{
			return false;
		}
	}

	public int compareTo(DBString o)
	{
		if (myString == null)
		{
			if (o == null)
				return 0;
			else
				return 1;
		}
		return myString.compareTo(o.getStringValue());
	}
}
