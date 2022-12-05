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

public class JavaList extends AbstractJoriaCollection
{
    private static final long serialVersionUID = 7L;
    protected transient Class<?> javaClass;
	boolean large;

	public JavaList(Class<?> aJavaClass, JoriaClass eltype)
	{
		super(eltype);
		if (aJavaClass == null)
			throw new Error("Cannot make JavaList when collection class is null");
		javaClass = aJavaClass;
		name = makeCollectionName(javaClass, elementType);
	}

	public Class<?> getJavaClass()
	{
		return javaClass;
	}

	public static String makeCollectionName(Class<?> javaClass, JoriaClass elementType) {
		String elementTypeName = elementType.getName();
		if (elementTypeName.startsWith("java.lang."))
		{
			elementTypeName = elementTypeName.substring("java.lang.".length());
		}

		if (javaClass.isArray())
		{
			return "Array<" + elementTypeName + ">";
		}
		else
		{
			String collectionClassName = javaClass.getName();
			if (collectionClassName.startsWith("java.util."))
			{
				collectionClassName = collectionClassName.substring("java.util.".length());
			}
			else if (collectionClassName.startsWith("java.lang."))
			{
				collectionClassName = collectionClassName.substring("java.lang.".length());
			}
			return collectionClassName + "<" + elementTypeName + ">";
		}
	}

	protected Object readResolve() throws ObjectStreamException
	{
        JoriaSchema sch = Env.schemaInstance;
		JoriaType cls = sch.findInternalType(name);
		if (cls != null)
			return cls;
		return JoriaUnknownType.createJoriaUnknownType(name);
	}

	public void setElementType(JoriaClass val)
	{
		if (val == null)
			throw new Error("JavaList.setElementType(null) not allowed.");
		elementType = val;
		name = makeCollectionName(javaClass, elementType);
	}

	public boolean isLarge()
	{
		return large;
	}

	public void setLarge(boolean large)
	{
		this.large = large;
	}
}
