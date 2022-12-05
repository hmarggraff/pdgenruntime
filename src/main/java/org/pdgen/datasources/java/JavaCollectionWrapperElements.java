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
import org.pdgen.model.run.RunEnv;

/**
 * Provides access to the elements of a top level collection (A collection valued root)
 */
public class JavaCollectionWrapperElements extends AbstractTypedJoriaMember
{

    private static final long serialVersionUID = 7L;

    public JavaCollectionWrapperElements(JoriaClass definingClass, JoriaCollection typ)
	{
		super(definingClass, "elements", typ);
		makeLongName();
	}

	public void setType(JoriaType newType)
	{
		type = newType;
		makeLongName();
	}

	public String getBaseName()
	{
		return null;
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		CollectionWrapperValue f = (CollectionWrapperValue) from;
		if (f == null)
			return null;
		DBCollection d = f.getCachedCollectionValue(asView); 
		if (d == null)
		{
            Object rd = f.getValue();
			if (rd == null)
				return null;
			d = JavaMember.makeCollectionValue(rd, asView, asView.getSourceCollection(), env);
			f.addCollectionToCache(d, asView);
			return d;
		}
		else
			return d;
	}

	protected Object readResolve()
	{
		if (definingClass instanceof JoriaUnknownType)
		{
			return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.classNotFound, this, null);
		}
		JoriaAccess mem = definingClass.findMember(name);
		if (mem == null)
		{
			return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.memberNotFound, this, null);
		}
		if (type instanceof JoriaUnknownType || type != mem.getType())
		{
			return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, definingClass, JoriaModifiedAccess.typeChanged, this, mem);
		}
		return mem;
	}
}
