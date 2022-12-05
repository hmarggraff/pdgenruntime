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

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.env.Env;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Map;

public class JavaClass extends AbstractJoriaClass implements JoriaPhysicalClass
{
	private static final long serialVersionUID = 7L;
	protected String myName;
	protected boolean internal;
	protected transient Class<?> myClass;

	public JavaClass(Class<?> c)
	{
		myClass = c;
		myName = c.getName();
	}

	public JavaClass(Class<?> c, String name)
	{
		myClass = c;
		myName = name;
	}

	public String getName()
	{
		return myName;
	}

	public String getParamString()
	{
		return "JavaClass[" + myClass.getName() + "]";
	}

	public boolean isInternal()
	{
		return internal;
	}

	public boolean isUnknown()
	{
		return Object.class.equals(myClass);
	}

	public void setBaseClasses(JoriaClass[] bases)
	{
		baseClasses = bases;
	}

	public void setInternal(boolean internal)
	{
		this.internal = internal;
	}

	public void setName(String name)
	{
		myName = name;
	}

	public void setMembers(JoriaAccess[] members)
	{
		this.members = members;
		flat_members = null;
	}

	public Class<?> theClass()
	{
		return myClass;
	}

	protected Object readResolve() throws ObjectStreamException
	{
		JoriaClass cls = Env.schemaInstance.findClass(myName);
		if (cls != null)
		{
			return cls;
		}
		else
		{
			return JoriaUnknownType.createJoriaUnknownType(myName);
		}
	}

	public String getPhysicalClassName()
	{
		return myClass.getName();
	}

}

