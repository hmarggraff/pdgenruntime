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

public class JavaMethod extends JavaMember {

    private static final long serialVersionUID = 7L;
    transient Method myMethod;

    public JavaMethod(JavaClass a, Method f, JoriaType type) {
        super(a, f.getName(), type);
        myMethod = f;
    }

    public JavaMethod(JavaClass a, Method f, JoriaType type, String name) {
        super(a, name, type);
        myMethod = f;
    }

    public DBData getValue(DBData jv, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        if (jv == null)
            throw new JoriaDataException("getValue from Method with null object");
        if (!(jv instanceof JavaValue))
            throw new JoriaDataException("getValue with java method not possible from " + jv.getClass().getName() + " " + myMethod.getName());
        if (getType().isCollection()) {
            DBCollection data = ((JavaValue) jv).getCachedCollectionValue(asView);
            if (data != null)
                return data;
        }
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        DBData data = sch.getReflectionDelegate().getMethodValue(this, (JavaValue) jv, asView, env);
        if (data instanceof DBCollection) {
            //((JavaValue)jv).addCollectionToCache((DBCollection) data, asView);
        }
        return data;
    }

    public Method getMethod() {
        return myMethod;
    }

    public String toString() {
        return getLongName();
    }

    public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getIntMethod(this, (JavaValue) from, env);
    }

    public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getFloatMethod(this, (JavaValue) from, env);
    }

    public Date getDateValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getDateMethod(this, (JavaValue) from, env);
    }

    public Object getPictureValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getObjectMethod(this, (JavaValue) from, env);
    }

    public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getBooleanMethod(this, (JavaValue) from, env);
    }

    public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getStringMethod(this, (JavaValue) from, env);
    }
}
