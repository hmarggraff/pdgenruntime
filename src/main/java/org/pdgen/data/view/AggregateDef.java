// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaLiteral;
import org.pdgen.data.JoriaPlaceHolderAccess;
import org.pdgen.data.Named;

import org.pdgen.env.Res;

import java.io.Serializable;

/**
 *          make sure AggregateDef is always immutable, otherwise it has to be duplicated when a report/cell is copied
 */
public class AggregateDef implements Named, Serializable
{
	//function enums
	public static final int avg = 0;
	public static final int len = 1;
	public static final int max = 2;
	public static final int min = 3;
	public static final int sum = 4;
	public static final int cimin = 5;	// case insensitive for strings
	public static final int cimax = 6;	// case insensitive for strings
	public static final int first = 7;	// functionally dependent
	public static final  int runningSum = 8; // incremental sum of a value in a collection

	// type enums
	public static final int longType = 2;
	public static final int doubleType = 1;
	public static final int stringType = 0;

	// scope values
	public static final int group = 0;
	public static final int grand = 1;
	public static final int page = 2;
	public static final int running = 3;
    public static final int lastRunning = 4;
    // names of enums
	public static final String[] scopeNames = {
		  Res.str("group"), Res.str("grand"), Res.str("page"), Res.str("running"), Res.str("lastRunning")
	};
	public static final String[] tagStrings = {
		  Res.asis("average"),
		  Res.asis("count"),
		  Res.asis("max"),
		  Res.asis("min"),
		  Res.asis("sum"),
		  Res.asis("cimin"),
		  Res.asis("cimax"),
		  Res.asis("first"),
		  Res.asis("running")
	};
    private static final long serialVersionUID = 7L;

    // members
	protected JoriaAccess myCollection;   // must refer to a JoriaCollection
	protected JoriaAccess myBase;
	protected int function;
	protected String myName;
	protected String shortName;
	protected int resultType;
	protected int sourceType;
	protected boolean[] myScope;
	public transient boolean hollow;
	public transient int index;

	public AggregateDef(int func, boolean[] where, JoriaAccess coll, JoriaAccess base)
	{
		function = func;
		myCollection = coll;
		myBase = base;
		myScope = where;
		myName = tagStrings[function] + "(" + myCollection.getName() + "." + base.getName() + ")";
		shortName = tagStrings[function] + "(" + base.getName() + ")";
		JoriaLiteral jl = (JoriaLiteral) myBase.getType();
		if (jl.isStringLiteral())
		{
			resultType = stringType;
			sourceType = stringType;
		}
		else if (jl.isIntegerLiteral())
		{
			if (function == min || function == max)
			{
				resultType = longType;
				sourceType = longType;
			}
			else
			{
				resultType = doubleType;
				sourceType = longType;
			}
		}
		else
		{
			resultType = doubleType;
			sourceType = doubleType;
		}
	}

	public String getName()
	{
		return myName;
	}

	public JoriaAccess getCollection()
	{
		return myCollection;
	}

	public JoriaAccess getAggregatedAccess()
	{
		return myBase;
	}

	public int getFunction()
	{
		return function;
	}

	public String getParameterString()
	{
		return Res.asis("AggregateDef ") + getName();
	}

	public int getSourceType()
	{
		return sourceType;
	}

	public int getResultType()
	{
		return resultType;
	}

	public boolean[] getScopes()
	{
		return myScope;
	}

	public String getShortName()
	{
		return shortName;
	}

	public boolean fixAccess()
	{
		JoriaAccess fixed = myCollection.getPlaceHolderIfNeeded();
		if (fixed != null)
		{
			myCollection = fixed;
		}
		if (myCollection == null || myCollection instanceof JoriaPlaceHolderAccess)
		{
			return false;
		}
		fixed = myBase.getPlaceHolderIfNeeded();
		if (fixed != null)
		{
			myBase = fixed;
		}
		return !(myBase instanceof JoriaPlaceHolderAccess);
	}

	protected Object readResolve()
    {
        if(myScope.length < 5)
        {
            boolean[] newScope = new boolean[5];
            System.arraycopy(myScope, 0, newScope, 0, myScope.length);
            myScope = newScope;
        }
        return this;
    }
}



