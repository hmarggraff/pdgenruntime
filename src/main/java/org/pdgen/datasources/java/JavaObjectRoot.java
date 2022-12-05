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

import java.lang.reflect.Method;

public class JavaObjectRoot extends AbstractJoriaRoot
{
    private static final long serialVersionUID = 7L;
    private transient Method method;
	public JavaObjectRoot(Method m, JoriaType t)
	{
		super(m.getName(), t);
		method = m;
	}

	public DBData getValue(DBData dataProvider, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		if (env.getRuntimeParameterValue(this) != null)// for server case because makevalue might trigger the Gui
			return env.getRuntimeParameterValue(this);
		try {
			Object invoke = method.invoke(dataProvider);
			return JavaMember.makeValue(dataProvider, invoke, asView, type, env);
		} catch (Throwable e) {
			throw new JoriaDataException(e.getMessage(), e);
		}
	}

	public void makeName()
	{
		longName = name + ": " + type.getName();
	}

	public void setType(JoriaType newType)
	{
		type = newType;
	}


}
