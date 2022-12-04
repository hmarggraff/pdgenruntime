// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.util.Calendar;
import java.util.Date;

public class DBDateTime implements DBObject, java.io.Serializable
{
    private static final long serialVersionUID = 7L;
    JoriaAccess axs;
	private transient Calendar calendar = Calendar.getInstance();

	public DBDateTime(JoriaAccess axs, Date d)
	{
		this.axs = axs;
		if (d == null)
			calendar = null;
		else
			calendar.setTime(d);
	}

	public DBDateTime(JoriaAccess axs, Calendar cal)
	{
		this.axs = axs;
		calendar = cal;
	}

	/*
	 * creates a DBDateTime from a long which represents the milliseconds since 1970 (java date)
	 */
	public DBDateTime(JoriaAccess axs, long d)
	{
		calendar.setTime(new Date(d));
		this.axs = axs;
	}

	public JoriaType getActualType()
	{
		return JoriaDateTime.instance();
	}

	public boolean isAssignableTo(JoriaType t)
	{
		return t == JoriaDateTime.instance();
	}

	public boolean isValid()
	{
		return true;
	}

	public JoriaAccess getAccess()
	{
		return axs;
	}

	public boolean isNull()
	{
		return calendar == null;
	}

	public java.util.Date getDate()
	{
		return calendar.getTime();
	}

	public int getUnixDate()
	{
		return (int) calendar.getTime().getTime() / 1000;
	}

	public Calendar getCalendar()
	{
		return calendar;
	}

	public String toString()
	{
		return calendar.getTime().toString();
	}

	public boolean same(DBData theOther)
	{
		return equals(theOther);
	}

	public int hashCode()
	{
		return (int) calendar.getTimeInMillis();
	}

	public boolean equals(Object theOther)
	{
		if (theOther instanceof DBDateTime)
		{
			long i1 = calendar.getTimeInMillis();
			long i2 = ((DBDateTime) theOther).getDate().getTime();
			return (i1 == i2);
		}
		else
		{
			return false;
		}
	}
}
