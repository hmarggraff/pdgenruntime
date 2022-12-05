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
import org.pdgen.model.run.RunEnv;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.function.Function;

public class JavaAttachedMethod<DeclaredIn, RetType> extends JavaMember
{

    private static final long serialVersionUID = 7L;
    transient Function<DeclaredIn, RetType> function;


	public JavaAttachedMethod(JavaClass a, Function<DeclaredIn, RetType> f, JoriaType type, String trueName)
	{
		super(a, trueName, type);
		function = f;
	}

	public DBData getValue(DBData jv, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		if (jv == null)
			throw new JoriaDataException("getValue from Method with null object");
		if (!(jv instanceof JavaValue))
			throw new JoriaDataException("getValue from Method with wrong object type "
					+ jv.getClass().getName());
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
		return sch.getReflectionDelegate().getAttachedMethodValue(this, (JavaValue) jv, asView, env);
	}

	public String toString()
	{
		return getLongName();
	}


	public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		return (long) eval(from);
	}

	private RetType eval(DBObject from) {
		DeclaredIn source = (DeclaredIn) ((JavaValue) from).getJavaObject();
		return function.apply(source);
	}

	public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		return (double) eval(from);
	}

	public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		return (int) eval(from);
	}

	public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		return (String) eval(from);
	}

	public Date getDateValue(DBObject from, RunEnv env) throws JoriaDataException
	{
		return (Date) eval(from);
	}

    public Object getPictureValue(DBObject from, RunEnv env)
    {
		return eval(from);
	}
}
