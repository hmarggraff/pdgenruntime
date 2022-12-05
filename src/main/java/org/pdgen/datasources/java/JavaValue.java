/*
 * Copyright (c) reportsanywhere.com.  All rights reserved.  http://www.reportsanywhere.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * The software in this package is published under the terms of the GPL v2.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE-GPL.txt file.
 */
package org.pdgen.datasources.java;

import org.pdgen.data.*;
import org.pdgen.env.Env;

public class JavaValue extends AbstractDBObject
{

	protected Object myValue;

	public JavaValue(Object o, JoriaAccess a)
	{
		super(a);
		myValue = o;
	}

	public JoriaType getActualType()
	{
		Class<?> c = myValue.getClass();
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
		JoriaType t = sch.findClassOrType(c);
		return t;
	}

    public boolean isAssignableTo(JoriaType t)
    {
        if(!(t instanceof JavaClass))
            return false;
        Class<?> tc = ((JavaClass)t).myClass;
        return tc.isAssignableFrom(myValue.getClass());
    }

    public Object getJavaObject()
	{
		return myValue;
	}

	public boolean isNull()
	{
		return myValue == null;
	}

	public String toString()
	{
		return myValue.toString();
	}

	public boolean equals(Object o)
	{
		if (o instanceof JavaValue)
		{
			JavaValue v = (JavaValue) o;
			if (myValue == null)
				return v.myValue == null;
			else
				return myValue.equals(v.myValue);
		}
		else
			return false;
	}

	public int hashCode()
	{
		return myValue.hashCode();
	}

	public boolean same(DBData theOther)
	{
		if (theOther instanceof JavaValue)
		{
			JavaValue v = (JavaValue) theOther;
			if (myValue == null)
				return v.myValue == null;
			else
				return myValue.equals(v.myValue);
		}
		else
			return false;
	}
}
