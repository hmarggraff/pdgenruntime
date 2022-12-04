// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.Trace;
import org.pdgen.data.view.AggregateDef;


public class TotalsAccumulatorString implements TotalsAccumulator
{
	protected String[] val = new String[5];
	protected String tVal;
	protected int function;

	public TotalsAccumulatorString(int function)
	{
		this.function = function;
	}

	public void rowComplete()
	{
		if (tVal != null)
		{
			for (int i = 0; i < 4; i++)
			{
				if (val[i] == null)
					val[i] = tVal;
				else
				{
					switch (function)
					{

						case AggregateDef.min:
							if (val[i].compareTo(tVal) > 0) val[i] = tVal;
							break;

						case AggregateDef.max:
							if (val[i].compareTo(tVal) < 0) val[i] = tVal;
							break;
					}
				}
			}
			tVal = null;
		}
	}


	public void reset(int scope)
	{
        if(scope == AggregateDef.page)
            val[AggregateDef.lastRunning] = val[AggregateDef.running];
        val[scope] = null;
		tVal = null;
	}


	public double getDoubleVal(int scope)
	{
		throw new JoriaAssertionError("Not possible for this subclass");
	}


	public long getLongVal(int scope)
	{
		throw new JoriaAssertionError("Not possible for this subclass");
	}


	public String getStringVal(int scope)
	{
		return val[scope];
	}


	public void add(String nVal)
	{
		Trace.check(tVal == null);
		tVal = nVal;
	}


	public void add(double nVal)
	{
		throw new JoriaAssertionError("Not possible for this subclass");
	}


	public void redoRow()
	{
		tVal = null;
	}
}
