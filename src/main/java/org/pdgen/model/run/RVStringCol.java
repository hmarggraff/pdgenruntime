// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;

import org.pdgen.data.view.AggregateDef;

import java.util.ArrayList;
import java.util.Locale;

public class RVStringCol implements RValue, RVCol
{
	String[] strings;
	public static final int startSize = 4;
	protected static final int endExpSize = 0x10000;

	public RVStringCol(String[] strings)
	{
		this.strings = strings;
	}

	public RVStringCol()
	{
	}

	public RVStringCol(int size)
	{
		strings = new String[size];
	}

	void checkBuffer(int at)
	{
		if (strings == null || at >= strings.length) // kein Buffer oder zuklein
		{
			int oldSize = (strings == null ? 0 : strings.length);
			int newSize = calculateNewBufferSize(oldSize, at + 1);
			String[] newArray = new String[newSize];
			if (strings != null)
			{
				System.arraycopy(strings, 0, newArray, 0, oldSize);
			}
			strings = newArray;
		}
	}

	protected int calculateNewBufferSize(int oldSize, int needed)
	{
		if (needed <= startSize)
		{
			return startSize;
		}
		else if (needed > endExpSize)
		{
			return ((needed / endExpSize) + 1) * endExpSize;
		}
		else
		{
			int size = oldSize;
			do
			{
				size *= 2;
			}
			while (size < needed);
			return size;
		}
	}

	public void add(int at, DBObject o, CellDef cd, OutputMode env) throws JoriaDataException
	{
		checkBuffer(at);
		try
		{
            boolean useDefaultAccess=true;
            if (cd instanceof DataCellDef)
            {
                DataCellDef rcd = (DataCellDef) cd;
                JoriaAccess axs = rcd.getAccessor();
                if (axs.isAccessTyped())
                {
                    strings[at] = ((JoriaAccessTyped) axs).getStringValue(o, env.getRunEnv());
                    useDefaultAccess = false;
                }
            }
			if (useDefaultAccess)
			{
				strings[at] = cd.getFormattedString(o, env.getRunEnv().getPager());
			}
		}
		catch (JoriaDataRetrievalExceptionInUserMethod e)
		{

			strings[at] = JoriaAccess.ACCESSERROR;
		}
	}

	public int getSize()
	{
		return strings.length;
	}

	public String get(int at)
	{
		Trace.check(strings);
		if (strings.length > 0)
			return strings[at];
		else
			return null;
	}

	public String[] get()
	{
		return strings;
	}

	public void buildFormattedStrings(CellDef formatter, Locale loc)
	{
	}

	public void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter)
	{
		collector.accumulate(aggregates, get(iter));
	}
}
