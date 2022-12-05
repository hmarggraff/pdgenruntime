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

import org.pdgen.data.AbstractJoriaLiteral;

import java.io.ObjectStreamException;

public class JavaClassAsLiteral extends AbstractJoriaLiteral
{

    private static final long serialVersionUID = 7L;
    protected String myName = "Class";
	protected static JavaClassAsLiteral theInstance = new JavaClassAsLiteral();

	private JavaClassAsLiteral()
	{
	}

	public static JavaClassAsLiteral instance()
	{
		return theInstance;
	}

	/* ---------------------------------------------------------------------- readResolve */
	protected Object readResolve() throws ObjectStreamException
	{
        return theInstance;
	}

	/* ---------------------------------------------------------------------- isLiteral */
	public boolean isLiteral()
	{
		return true;
	}

	public String getName()
	{
		return myName;
	}

	/* ---------------------------------------------------------------------- isStringLiteral */
	public boolean isStringLiteral()
	{
		return true;
	}

	public String getParamString()
	{
		return "JavaClassAsLiteral";
	}
}
