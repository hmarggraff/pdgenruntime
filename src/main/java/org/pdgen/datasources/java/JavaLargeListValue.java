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

import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.env.Env;

import java.util.List;

public class JavaLargeListValue extends JavaListValue
{
	public JavaLargeListValue(Object o, JoriaAccess a) throws JoriaDataException
	{
		super(o, a);
	}

	protected void freeItem(int index)
	{
        final ReflectionDelegate reflectionDelegate = ((JavaSchema) Env.schemaInstance).getReflectionDelegate();
		reflectionDelegate.evictObject(((List<Object>)myValue).get(index));
		((List<Object>)myValue).set(index, null);
	}

    public boolean reset()
    {
        return index == -1;
    }

}
