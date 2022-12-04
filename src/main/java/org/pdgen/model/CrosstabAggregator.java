// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAssertionError;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.view.AggregateDef;


import java.util.HashMap;

public class CrosstabAggregator implements Comparable<CrosstabAggregator>
{
	private final CrosstabAggregate[] aggs;
	final double[] totals;
	HashMap<DBData, CrosstabAggregator> elems;  // die inneren Gruppen
	public int index;
	int count;

	public CrosstabAggregator(CrosstabAggregate[] aggs, boolean subElems, int index)
	{
		this.aggs = aggs;
		this.index = index;
		if (subElems)
			elems  = new HashMap<DBData, CrosstabAggregator>();
		totals = new double[aggs.length];
		initAggs(aggs);
	}

	private void initAggs(CrosstabAggregate[] aggs)
	{
		for (int i = 0; i < aggs.length; i++)
		{
			CrosstabAggregate agg = aggs[i];
			switch (agg.getFunction())
			{
				case AggregateDef.min:
					{
						totals[i] = Double.MAX_VALUE;
                        break;
                    }
				case AggregateDef.max:
					{
						totals[i] = -Double.MAX_VALUE;
                        break;
                    }
			}
		}
	}

	public CrosstabAggregator(CrosstabAggregate[] aggs, int index)
	{
		this.aggs = aggs;
		this.index = index;
		totals = new double[aggs.length];
		initAggs(aggs);
	}

	public void accumulate(double[] vals) throws JoriaDataException
	{
		count++;
		for (int i = 0; i < aggs.length; i++)
		{
			switch (aggs[i].getFunction())
			{
				case AggregateDef.sum:
				case AggregateDef.avg:
					{
						totals[i] += vals[i];
						break;
					}
				case AggregateDef.min:
					{
						totals[i] = Math.min(vals[i], totals[i]);
						break;
					}
				case AggregateDef.max:
					{
						totals[i] = Math.max(vals[i], totals[i]);
						break;
					}
				case AggregateDef.len:
                    break;
                default:
					throw new JoriaDataException("Bad aggregate function for crosstab: " + aggs[i].toString());
			}
		}
	}

	public HashMap<DBData, CrosstabAggregator> getElems()
	{
		return elems;
	}

	public double get(int ix)
	{
		CrosstabAggregate agg = aggs[ix];
		switch (agg.getFunction())
		{
			case AggregateDef.sum:
			case AggregateDef.min:
			case AggregateDef.max:
				return totals[ix];
			case AggregateDef.avg:
				return totals[ix]/count;
			case AggregateDef.len:
				return count;
			default:
				throw new JoriaAssertionError("Unhandled aggregate: " + agg.getFunction());
		}
	}

	public int compareTo(CrosstabAggregator o)
	{
		return o.index - index;
	}

	public int getCount()
	{
		return count;
	}
}
