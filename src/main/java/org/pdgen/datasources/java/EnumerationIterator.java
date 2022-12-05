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

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator implements Iterator
{

	Enumeration<?> e;

	public EnumerationIterator(Enumeration<?> e)
	{
		this.e = e;
	}

	public void remove()
	{
	}

	public Object next()
	{
		return e.nextElement();
	}

	public boolean hasNext()
	{
		return e.hasMoreElements();
	}
}
