// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.*;
import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.DataCellDef;
import org.pdgen.model.cells.SimpleTextCellDef;
import org.pdgen.model.style.CellStyle;

import org.pdgen.data.view.AggregateDef;
import org.pdgen.env.Settings;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Arrays;

public class RVFloatCol extends RVStringCol
{

	double[] vals;

	public RVFloatCol(double[] vals)
	{
		this.vals = vals;
	}

	public RVFloatCol()
	{

	}

	public RVFloatCol(int size)
	{
		vals = new double[size];
        Arrays.fill(vals, Double.NaN);
	}

	public void add(int at, DBObject o, CellDef rcd, OutputMode env) throws JoriaDataException
	{
		checkBuffer(at);
		try
		{
			JoriaAccess axs = ((DataCellDef)rcd).getAccessor();
			if (axs.isAccessTyped())
			{
                vals[at] = ((JoriaAccessTyped) axs).getFloatValue(o, env.getRunEnv());
			}
			else
			{
				DBData v = axs.getValue(o, axs, env.getRunEnv());
				if (v != null && !v.isNull())
					vals[at] = ((DBReal) v).getRealValue();
				else
					vals[at] = Double.NaN;
			}
		}
		catch (JoriaDataRetrievalExceptionInUserMethod e)
		{
			vals[at] = Double.NaN;
		}
	}

	void checkBuffer(int at)
	{
		if (vals == null || at >= vals.length) // kein Buffer oder zuklein
		{
			int oldSize = (vals == null ? 0 : vals.length);
			int newSize = calculateNewBufferSize(oldSize, at + 1);
			double[] newArray = new double[newSize];
			if (vals != null)
			{
				System.arraycopy(vals, 0, newArray, 0, oldSize);
                Arrays.fill(newArray, oldSize, newSize-1, Double.NaN);
			}
            else
                Arrays.fill(vals, Double.NaN);
			vals = newArray;
		}
	}

	public int getSize()
	{
		return vals.length;
	}

	public void buildFormattedStrings(CellDef cd, Locale loc)
	{
		if (strings != null)
			return;
		CellStyle cs = cd.getCascadedStyle();
		strings = new String[vals.length];
		String lPat = Internationalisation.localize(cs.getFloatPattern(), loc);
		DecimalFormat f = new DecimalFormat(lPat, new DecimalFormatSymbols(loc));
        f.setRoundingMode(Settings.getRoundingMode());
		for (int i = 0; i < vals.length; i++)
		{
			if (Double.isNaN(vals[i]))
				strings[i] = null;
			else
			{
				String s = f.format(vals[i]);
				strings[i] = SimpleTextCellDef.wrapText(s, cs, loc);
			}
		}
	}

	public void accumulate(AggregateCollector collector, ArrayList<AggregateDef> aggregates, int iter)
	{
		collector.accumulate(aggregates, vals[iter]);
	}
}
