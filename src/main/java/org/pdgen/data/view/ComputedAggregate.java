// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;
import org.pdgen.model.run.RunEnvImpl;


import java.util.Map;

public class ComputedAggregate extends MutableAccess
{
    private static final long serialVersionUID = 7L;
    protected JoriaAccess aggregateSource;
	protected int function;
	protected boolean nanIsZero;

	public NameableAccess dup(JoriaClass newParent, Map<Object, Object> alreadyCopied)
	{
		final Object duplicate = alreadyCopied.get(this);
		if (duplicate != null)
			return (NameableAccess) duplicate;
		JoriaAccess newBA = (JoriaAccess) alreadyCopied.get(myBaseAccess);
		if (newBA == null)
			newBA = myBaseAccess;
		ComputedAggregate ret = new ComputedAggregate(newParent, newBA, aggregateSource, function, nanIsZero);
		alreadyCopied.put(this, ret);
		
		fillDup(ret, alreadyCopied);
		return ret;
	}

	public ComputedAggregate(JoriaClass parent, JoriaAccess collectionAccess, JoriaAccess member, int which, boolean nanIsZero)
	{
		super(parent, collectionAccess);
		this.nanIsZero = nanIsZero;
		aggregateSource = member;
		function = which;
		switch (function)
		{
			case AggregateDef.min:
			case AggregateDef.max:
			case AggregateDef.cimin:
			case AggregateDef.cimax:
			case AggregateDef.first:
				type = aggregateSource.getType();
				break;
			case AggregateDef.sum:
				if (aggregateSource.getType().isStringLiteral())
					type = DefaultStringLiteral.instance();
				else if (aggregateSource.getType().isIntegerLiteral() || aggregateSource.getType().isRealLiteral())
					type = DefaultRealLiteral.instance();
				else
					throw new JoriaAssertionError("Bad type for sum: " + aggregateSource.getType());
				break;
			case AggregateDef.runningSum:
			case AggregateDef.avg:
				if (aggregateSource.getType().isIntegerLiteral() || aggregateSource.getType().isRealLiteral())
					type = DefaultRealLiteral.instance();
				else
					throw new JoriaAssertionError("Bad type for sum: " + aggregateSource.getType());
				break;
			case AggregateDef.len:
				type = DefaultIntLiteral.instance();
				break;
			default:
				throw new JoriaAssertionError("Unhandled Aggregate function " + function);
		}
		makeName();
		xmlTag = name;
	}

	public JoriaAccess getAggregateSource()
	{
		return aggregateSource;
	}
	public JoriaAccess getAggregateSourceExtended()
	{
		if (aggregateSource == null) // length
			return myBaseAccess;  // the collection
		return aggregateSource;   // aggregated literal
	}

	public void makeName()
	{
		if (aggregateSource != null)
			name = aggregateSource.getName() + "_" + AggregateDef.tagStrings[function];
		else
			name = AggregateDef.tagStrings[function];
		longName = name + ": " + type.getName();
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		if (function == AggregateDef.len)
		{
			DBCollection dc = getColl(from, env);
			if (dc == null)
				return null;
			else
				return new DBIntImpl(this, dc.getLength());
		}
		else if (function == AggregateDef.runningSum)
		{
			final double ret = getRunningSum(env, from, asView);
			return new DBRealImpl(this, ret);
		}
		else
		{
			Object agg = getAggregateValue(from, env);
			if (agg == null)
				return null;
			if (aggregateSource.getType().isIntegerLiteral())
			{
				if (function == AggregateDef.min || function == AggregateDef.max || function == AggregateDef.first)
					return new DBIntImpl(this, getIntAgg(agg));
				else
					return new DBRealImpl(this, getInt2DoubleAgg(agg));
			}
			else if (aggregateSource.getType().isRealLiteral())
			{
				return new DBRealImpl(this, getFloatAggVal(agg));
			}
			else if (aggregateSource.getType().isStringLiteral())
			{
				return new DBStringImpl(this, getStringAgg(agg));
			}
			else if (aggregateSource.getType().isDate())
			{
				return new DBDateTime(this, getDateAggVal(agg));
			}
			else if (aggregateSource.getType().isClass() || aggregateSource.getType().isCollection())
			{
				return (DBData) getAggregateValue(from, env);
			}
			throw new JoriaAssertionError("Unhandled aggregated type " + aggregateSource.getType() + " " + getName());
		}
	}

	protected double getRunningSum(final RunEnv env, final DBData from, final JoriaAccess asView) throws JoriaDataException
	{
		DBCollectionCache firstCache = (DBCollectionCache) from;
		AggregateKey k = new AggregateKey(getBaseAccess(), this);
		Double existingAgg = (Double) firstCache.getCachedAggregate(k);
		if (existingAgg != null)
		{
			return existingAgg;
		}
		//DBObject parentVal = (DBObject) ((RunEnvImpl) env).topOfObjectPath();
		DBCollectionCache cc = (DBCollectionCache) ((RunEnvImpl) env).topOfObjectPath();
		DoubleHolder agg = (DoubleHolder) cc.getCachedAggregate(k);
		if (agg == null)
		{
			agg = new DoubleHolder();
			cc.addAggregate(k, agg);
		}
		DBData d = aggregateSource.getValue(from, asView, env);
		if (d instanceof DBReal)
			agg.val += ((DBReal) d).getRealValue();
		else if (d instanceof DBInt)
			agg.val += ((DBInt) d).getIntValue();
		final double ret = agg.val;
		firstCache.addAggregate(k, ret);
		return ret;
	}

	protected Object getAggregateValue(DBData from, RunEnv env) throws JoriaDataException
	{
		DBCollectionCache f = (DBCollectionCache) from;
		AggregateKey ak = new AggregateKey(myBaseAccess, aggregateSource);
		Object agg = f.getCachedAggregate(ak);
		if (agg == null)
		{
			agg = buildAggCache(f, env);
		}
		return agg;
	}

	private long getDateAggVal(Object agg)
	{
		AggregateCacheDate aci = (AggregateCacheDate) agg;
		if (function == AggregateDef.min)
			return aci.min;
		else if (function == AggregateDef.max)
			return aci.max;
		else if (function == AggregateDef.first)
			return aci.first;
		throw new JoriaAssertionError("Invalid aggregation function " + function);
	}

	private String getStringAgg(Object agg)
	{
		AggregateCacheString aci = (AggregateCacheString) agg;
		if (function == AggregateDef.cimin)
			return aci.cimin;
		else if (function == AggregateDef.cimax)
			return aci.cimax;
		else if (function == AggregateDef.min)
			return aci.min;
		else if (function == AggregateDef.max)
			return aci.max;
		else if (function == AggregateDef.first)
			return aci.first;
		else if (function == AggregateDef.sum)
		{
			if (aci.sum != null)
				return aci.sum.toString();
			else
				return null;
		}
		throw new JoriaAssertionError("Invalid aggregation function " + function);
	}

	double getFloatAggVal(Object agg)
	{
		AggregateCacheReal aci = (AggregateCacheReal) agg;
		if (function == AggregateDef.sum)
			return aci.sum;
		else if (function == AggregateDef.min)
			return aci.min;
		else if (function == AggregateDef.max)
			return aci.max;
		else if (function == AggregateDef.avg)
			return aci.avg;
		else if (function == AggregateDef.first)
			return aci.first;
		throw new JoriaAssertionError("Invalid aggregation function " + function);
	}

	protected long getIntAgg(Object agg)
	{
		AggregateCacheInt aci = (AggregateCacheInt) agg;
		if (function == AggregateDef.min)
			return aci.min;
		else if (function == AggregateDef.max)
			return aci.max;
		else if (function == AggregateDef.first)
			return aci.first;
		throw new JoriaAssertionError("Invalid aggregation function " + function);
	}

	protected double getInt2DoubleAgg(Object agg)
	{
		AggregateCacheInt aci = (AggregateCacheInt) agg;
		if (function == AggregateDef.sum)
			return aci.sum;
		else if (function == AggregateDef.avg)
			return aci.avg;
		throw new JoriaAssertionError("Invalid aggregation function " + function);
	}

	public boolean isAccessTyped()
	{
		//try hmf changed 030225 return true;
		return false;
	}

	public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		if (function == AggregateDef.runningSum)
		{
			return getRunningSum(env, from, this);
		}
		final Object agg = getAggregateValue(from, env);
		if (agg == null)
			return Float.NaN;
		else if (agg instanceof AggregateCacheReal)
		{
			AggregateCacheReal aci = (AggregateCacheReal) agg;
			if (function == AggregateDef.sum)
				return aci.sum;
			else if (function == AggregateDef.min)
				return aci.min;
			else if (function == AggregateDef.max)
				return aci.max;
			else if (function == AggregateDef.avg)
				return aci.avg;
			else if (function == AggregateDef.first)
				return aci.first;
		}
		else if (agg instanceof AggregateCacheInt)
		{
			AggregateCacheInt aci = (AggregateCacheInt) agg;
			if (function == AggregateDef.sum)
				return aci.sum;
			else if (function == AggregateDef.avg)
				return aci.avg;
		}
		throw new JoriaAssertionError("Invalid aggregation function " + function);
	}

	public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		if (function != AggregateDef.len)
		{
			final Object agg = getAggregateValue(from, env);
			AggregateCacheInt aci = (AggregateCacheInt) agg;
			if (function == AggregateDef.min)
				return aci.min;
			else if (function == AggregateDef.max)
				return aci.max;
			else if (function == AggregateDef.first)
				return aci.first;
			throw new JoriaAssertionError("Invalid aggregation function " + function);
		}
		else
		{
			DBCollection dc = getColl(from, env);
			if (dc == null)
				return -1;
			else
				return dc.getLength();
		}
	}

	public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		throw new JoriaAssertionError("getBooleanValue not possible for ComputedAggregate");
	}

	public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		final Object av = getAggregateValue(from, env);
		return getStringAgg(av);
	}

	DBCollection getColl(DBData from, RunEnv env) throws JoriaDataException
	{
		DBCollectionCache f = (DBCollectionCache) from;
		DBCollection dc = f.getCachedCollectionValue(myBaseAccess);
		try
		{
			if (dc == null)
			{
				if (myBaseAccess instanceof ComputedAggregate || myBaseAccess instanceof GroupingAccess || !(from instanceof DBGroup))
					dc = (DBCollection) myBaseAccess.getValue(from, myBaseAccess, env);
				else
				{
					DBGroup dbg = (DBGroup) from;
					dc = dbg.getGroupValues();
				}
				//d = JavaMember.makeCollectionValue(f.getRootDef().getValue(), asView, asView.getSourceCollection());
				f.addCollectionToCache(dc, myBaseAccess);
			}
			return dc;
		}
		catch (JoriaDataException ex)
		{
			Trace.log(ex);
			throw ex;
		}
	}

	Object buildAggCache(DBCollectionCache f, RunEnv env) throws JoriaDataException
	{
		DBCollection dc = getColl(f, env);
		if (dc == null)
			return null;
		dc.reset();
		AggregateKey ak = new AggregateKey(myBaseAccess, aggregateSource);
		if (aggregateSource.getType().isIntegerLiteral())
		{
			AggregateCacheInt aci = new AggregateCacheInt();
			int len = 0;
			while (dc.next())
			{
				DBObject o = dc.current();
				len++;
				DBInt v = (DBInt) aggregateSource.getValue(o, aggregateSource, env);
				if (v != null)
				{
					long iv = v.getIntValue();
					aci.min = Math.min(iv, aci.min);
					aci.max = Math.max(iv, aci.max);
					aci.sum = aci.sum + iv;
					if (len == 1)
						aci.first = iv;
				}
				else if (!nanIsZero)
				{
					aci.sum = Double.NaN;
					aci.max = Long.MAX_VALUE;
					aci.min = Long.MIN_VALUE;
					if (len == 1)
						aci.first = Long.MIN_VALUE;
				}
			}
			dc.reset();
			if (len > 0)
				aci.avg = aci.sum / len;
			else
				aci.avg = DBReal.NULL;
			f.addAggregate(ak, aci);
			return aci;
		}
		else if (aggregateSource.getType().isRealLiteral())
		{
			AggregateCacheReal acr = new AggregateCacheReal();
			int len = 0;
			while (dc.next())
			{
				len++;
				DBObject o = dc.current();
				double vr = Double.NaN;
				if (aggregateSource.isAccessTyped())
				{
					vr = ((JoriaAccessTyped) aggregateSource).getFloatValue(o, env);
				}
				else
				{
					DBReal v = (DBReal) aggregateSource.getValue(o, aggregateSource, env);
					if (v != null)
						vr = v.getRealValue();
				}
				if (Double.isNaN(vr))
					if (nanIsZero)
						vr = 0;
				acr.min = Math.min(vr, acr.min);
				acr.max = Math.max(vr, acr.max);
				acr.sum = acr.sum + vr;
				if (len == 1)
					acr.first = vr;
			}
			dc.reset();
			if (len > 0)
				acr.avg = acr.sum / len;
			else
				acr.avg = DBReal.NULL;
			f.addAggregate(ak, acr);
			return acr;
		}
		else if (aggregateSource.getType().isStringLiteral())
		{
			boolean first = true;
			AggregateCacheString acs = new AggregateCacheString();
			while (dc.next())
			{
				DBObject o = dc.current();
				DBString v = (DBString) aggregateSource.getValue(o, aggregateSource, env);
				if (v != null && !v.isNull())
				{
					String sv = v.getStringValue();
					if (acs.min == null)
					{
						acs.min = sv;
						acs.max = sv;
						acs.cimin = sv;
						acs.cimax = sv;
						if (function == AggregateDef.sum && sv != null && sv.trim().length() > 0)
							acs.sum = new StringBuffer(sv.trim());
						if (first)
						{
							acs.first = sv;
							first = false;
						}
						continue;
					}
					if (function == AggregateDef.sum)
						acs.sum.append(", ").append(sv);
					if (acs.min.compareToIgnoreCase(sv) > 0)
						acs.cimin = sv;
					if (acs.max.compareToIgnoreCase(sv) < 0)
						acs.cimax = sv;
					if (acs.min.compareTo(sv) > 0)
						acs.min = sv;
					if (acs.max.compareTo(sv) < 0)
						acs.max = sv;
				}
			}
			dc.reset();
			f.addAggregate(ak, acs);
			return acs;
		}
		else if (aggregateSource.getType().isDate())
		{
			AggregateCacheDate acs = new AggregateCacheDate();
			boolean first = true;
			while (dc.next())
			{
				DBObject o = dc.current();
				DBDateTime v = (DBDateTime) aggregateSource.getValue(o, aggregateSource, env);
				if (v != null && !v.isNull())
				{
					long sv = v.getDate().getTime();
					if (acs.min > sv)
						acs.min = sv;
					if (acs.max < sv)
						acs.max = sv;
					if (first)
					{
						acs.first = sv;
						first = false;
					}
				}
			}
			dc.reset();
			f.addAggregate(ak, acs);
			return acs;
		}
		else if (function == AggregateDef.first)
		{
			if (dc.next())
			{
				DBObject o = dc.current();
				DBData v = aggregateSource.getValue(o, aggregateSource, env);
				dc.reset();
				if (v != null && !v.isNull())
					f.addAggregate(ak, v);
				return v;
			}
			return null;
		}
		else
			throw new JoriaAssertionError("Unhandled type in aggregation " + aggregateSource.getType().toString());
	}

	public JoriaType getSourceTypeForChildren()
	{
		return type;
	}

	protected Object readResolve()
	{
		if (!nanIsZero)
			nanIsZero = true;
		return this;
	}

	private ComputedAggregate(JoriaClass newParent, String name)
	{
		super(newParent, name);
	}

	public String getFunction()
	{
		return AggregateDef.tagStrings[function];
	}

	public int getFunctionIndex()
	{
		return function;
	}

	public void unbind()
	{
		super.unbind();
		if (aggregateSource != null)
			aggregateSource = new UnboundAccessSentinel(aggregateSource);
		else if (function != AggregateDef.len)
			Trace.breakHere();
	}

	public void rebind(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		Trace.check(bindableTo(newBinding, newParentBinding));
		myBaseAccess = newParentBinding;
		aggregateSource = newBinding;
	}

	public boolean bindableTo(JoriaAccess newBinding, JoriaAccess newParentBinding)
	{
		UnboundAccessSentinel unboundAccessBase = (UnboundAccessSentinel) myBaseAccess;
		if (aggregateSource != null)
		{
			return unboundAccessBase.isBindable(newParentBinding) && ((UnboundAccessSentinel) aggregateSource).isBindable(newBinding);
		}
		else
			return unboundAccessBase.isBindable(newBinding);
	}

	public void setDefiningClass(ClassView grp)
	{
		definingClass = grp;
	}
}

class AggregateCacheInt
{
	long min = Long.MAX_VALUE;
	long max = Long.MIN_VALUE;
	double sum;
	double avg;
	long first;
}

class AggregateCacheReal
{
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;
	double sum;
	double avg;
	double first;
}

class AggregateCacheString
{
	String min;
	String max;
	String cimin;
	String cimax;
	String first;
	StringBuffer sum;
}

class AggregateCacheDate
{
	long min = Long.MAX_VALUE;
	long max = Long.MIN_VALUE;
	long first;
}
