// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.Env;

public class DBLiteralStringCollection extends AbstractDBData implements DBLiteralCollection
{
	String[] values;

    public DBLiteralStringCollection(JoriaAccess axs, String[] values)
	{
		super(axs);
        initValues(values);
	}

	@SuppressWarnings("UnusedDeclaration")
    public DBLiteralStringCollection(String[] values)
	{
		this.values = values;
        initValues(values);
	}

    protected void initValues(String [] values)
    {
        int countNonNull = 0;
        for (String value : values)
        {
            if(value != null)
                countNonNull++;
        }
        int targetIndex = 0;
        this.values = new String[countNonNull];
        for (String value : values)
        {
            if(value != null)
            {
                this.values[targetIndex++] = value;
            }
        }
    }
    public int getLength()
	{
		if (values == null)
			return 0;
		return values.length;
	}

	public boolean isNull()
	{
		return values == null;
	}

	public String pick() throws JoriaDataException
	{
		if (values == null || values.length == 0)
			return null;
		return values[0];
	}

	public boolean contains(DBData el)
	{
		if (isNull() || !el.getActualType().isStringLiteral())
			return false;
		if (el.isNull())
		{
            for (String s : values)
            {
                if (s == null)
                    return true;
            }
			return false;

		}
		String es = ((DBString)el).getStringValue();
        for (String s : values)
        {
            if (es.equals(s))
                return true;

        }
		return false;
	}

	public boolean same(DBData theOther)
	{
		if (!(theOther instanceof DBLiteralCollection))
			return false;
		DBLiteralCollection other = (DBLiteralCollection) theOther;
        if(!other.isStrings())
            return false;
		try
		{
			if (other.getLength() != getLength())
				return false;
			for (int i = 0; i < values.length; i++)
			{
				String v1 = values[i];
				String v2 = other.getStringAt(i);
				if (v1 == null)
				{
					if (v2 != null)
						return false;
				}
				else if (!v1.equals(v2))
					return false;
			}
			return true;
		}
		catch (JoriaDataException e)
		{
			Env.instance().handle(e);
			return false;
		}
	}

	public boolean isStrings()
	{
		return true;
	}

	public boolean isInts()
	{
		return false;
	}

	public boolean isFloats()
	{
		return false;
	}

	public boolean isBooleans()
	{
		return false;
	}

	public String getStringAt(int i)
	{
		if (values == null || values.length == 0)
			return null;
		return values[i];
	}

	public long getIntAt(int i)
	{
		throw new JoriaAssertionError("DBLiteralStringCollection returns strings");
	}

	public double getFloatAt(int i)
	{
		throw new JoriaAssertionError("DBLiteralStringCollection returns strings");
	}

	public boolean getBooleanAt(int i)
	{
		throw new JoriaAssertionError("DBLiteralStringCollection returns strings");
	}

    @Override
    public String toString()
    {
        return super.toString();
    }
}
