// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

public class DBIntImpl extends AbstractDBData implements DBInt
{
    private static final long serialVersionUID = 7L;
    private final long myInt;

	/**
	 * ----------------------------------------------------------------------- DBIntImpl
	 */
	public DBIntImpl(JoriaAccess axs, long value)
	{
		super(axs);
		myInt = value;
	}

	/**
	 * ----------------------------------------------------------------------- equals
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof DBInt)
			return ((DBInt) obj).getIntValue() == myInt;
		return obj instanceof DBReal && ((DBReal) obj).getRealValue() == myInt;
	}

	/**
	 * ----------------------------------------------------------------------- getIntValue
	 */
	public long getIntValue()
	{
		return myInt;
	}

	/**
	 * ----------------------------------------------------------------------- isNull
	 */
	public boolean isNull()
	{
		return myInt == Long.MIN_VALUE + 1;
	}

	/**
	 * ----------------------------------------------------------------------- toString
	 */
	public String toString()
	{
		return String.valueOf(myInt);
	}

	public int hashCode()
	{
		return (int) (myInt % Integer.MAX_VALUE);
	}

	public boolean same(DBData theOther)
	{
		return theOther instanceof DBIntImpl && ((DBIntImpl) theOther).myInt == myInt;
	}
}
