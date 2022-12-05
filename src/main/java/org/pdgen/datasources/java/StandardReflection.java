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

import org.joda.time.DateTime;
import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * This maps access to reflection to javas standard reflection
 */
public class StandardReflection implements ReflectionDelegate {
    protected boolean filterMethods;
    protected boolean filterInterfaces;
    protected boolean filterSuperClass;
    //final URLClassLoader contentClassloader;

    public StandardReflection() {
    }

    protected boolean realField(Class<?> c, Field f) {
        return true;
    }

    protected boolean realMethod(Class<?> c, Method m) {
        return true;
    }

    protected boolean realInterface(Class<?> c) {
        return true;
    }

    protected boolean realSuperclass(Class<?> c) {
        return true;
    }

    public Class<?>[] getInterfaces(Class<?> c) {
        Class<?>[] interfaces = c.getInterfaces();
        if (filterInterfaces) {
            ArrayList<Class<?>> is = new ArrayList<Class<?>>();
            for (Class<?> anInterface : interfaces) {
                if (realInterface(anInterface)) {
                    is.add(anInterface);
                }
            }
            interfaces = new Class[is.size()];
            is.toArray(interfaces);
        }
        return interfaces;
    }

    public Field getField(Class<?> c, String n) {
        try {
            return c.getDeclaredField(n);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (SecurityException e) {
            return null;
        }
    }

    public Method getMethod(Class<?> c, String n) {
        try {
            if (c.isInterface())
                return c.getMethod(n);
            else
                return c.getDeclaredMethod(n);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (SecurityException e) {
            return null;
        }
    }

    public Method getMethod(Class<?> c, Class<?> e, String n) {
        Class<?>[] parameters = new Class[]{c};
        try {
            return e.getDeclaredMethod(n, parameters);
        } catch (NoSuchMethodException e1) {
            return null;
        } catch (SecurityException e1) {
            return null;
        }
    }

    public String getRealMethodName(Method method) {
        return method.getName();
    }

    public Field[] getDeclaredFields(Class<?> c) {
        return c.getDeclaredFields();
    }

    public ArrayList<Field> getFields(Class<?> c) {
        ArrayList<Field> fs = new ArrayList<Field>();
        collectFields(c, fs);
        return fs;
    }

    protected void collectFields(Class<?> c, ArrayList<Field> fs) {
        Field[] fields = null;
        while (fields == null) {
            try {
                fields = getDeclaredFields(c);
            } catch (NoClassDefFoundError ex) {
                String clsName = ex.getMessage();
                clsName = clsName.replace('/', '.');
                System.out.println("looking for " + clsName);//trdone
                Class<?> cls = loadMangledClass(clsName);
                if (cls == null) {
                    throw ex;
                }
            }
        }
        if (c != Object.class)
            Trace.logDebug(Trace.schema, "Class " + c.getName() + " with fields ");//trdone
        for (Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers()))
            	Trace.logDebug(Trace.schema, "  " + field.getName() + ": " + field.getGenericType());
        }
        for (Field field1 : fields) {
            if (realField(c, field1) && !Modifier.isStatic(getModifiers(field1))) {
                fs.add(field1);
            }
        }
        Class<?> bc = getSuperclass(c);
        if (bc != null)
            collectFields(bc, fs);
    }

    public Method[] getMethods(Class<?> c) {
        Method[] methods = null;
        while (methods == null) {
            try {
                methods = c.getMethods();
            } catch (NoClassDefFoundError ex) {
                String clsName = ex.getMessage();
                clsName = clsName.replace('/', '.');
                Trace.log(Trace.schema, "looking for " + clsName);//trdone
                Class<?> cls = loadMangledClass(clsName);
                if (cls == null) {
                    throw ex;
                }
            }
        }
        if (Trace.getTraceLevel() >= 7 && (Trace.getModules() & Trace.schema) != 0) {
            Trace.logDebug(Trace.schema, "Class " + c.getName() + " with methods:");//trdone
            for (Method m : methods) {
                Trace.logDebug(Trace.schema, " " + m.getName());
            }
        }
        if (filterMethods) {
            ArrayList<Method> ms = new ArrayList<Method>();
            for (Method method : methods) {
                if (realMethod(c, method) && !Modifier.isStatic(getModifiers(method))) {
                    ms.add(method);
                }
            }
            methods = new Method[ms.size()];
            ms.toArray(methods);
        }
        return methods;
    }

    public int getModifiers(Member f) {
        return f.getModifiers();
    }

    public Class<?> getSuperclass(Class<?> c) {
        if (filterSuperClass) {
            Class<?> sc = c.getSuperclass();
            if (sc == null)
                return null;
            else if (realSuperclass(sc)) {
                return sc;
            } else {
                return java.lang.Object.class;
            }
        } else {
            return c.getSuperclass();
        }
    }

    public void loadObject(Object o) throws JoriaDataException {
        // to be used by oodbms reflection classes to pull object into memory
    }

    public boolean isInternal(Class<?> c) {
        return false;
    }

    public boolean isInternalName(String clsname) {
        return false;
    }

    public DBData getFieldValue(JavaField f, JavaValue jv, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        Field myField = f.getField();
        Object o = jv.getJavaObject();
        loadObject(o);
        try {
            // System.out.println(o.toString());
            if (f.getType().isLiteral()) {
                Class<?> c = myField.getType();
                if (c == int.class)
                    return new DBIntImpl(asView, myField.getInt(o));
                else if (c == Integer.class)
                    return new DBIntImpl(asView, (Integer) myField.get(o));
                else if (c == String.class)
                    return new DBStringImpl(asView, (String) myField.get(o));
                else if (c == boolean.class || c == Boolean.class)
                    return new DBBooleanImpl(asView, myField.getBoolean(o));
                else if (c == float.class || c == Float.class)
                    return new DBRealImpl(asView, myField.getFloat(o));
                else if (c == double.class || c == Double.class)
                    return new DBRealImpl(asView, myField.getDouble(o));
                else if (c == long.class)
                    return new DBIntImpl(asView, myField.getLong(o));
                else if (c == Long.class)
                    return new DBIntImpl(asView, (Long) myField.get(o));
                else if (c == byte.class || c == Byte.class)
                    return new DBIntImpl(asView, myField.getByte(o));
                else if (c == short.class || c == Short.class)
                    return new DBIntImpl(asView, myField.getShort(o));
                else if (c == char.class || c == Character.class)
                    return new DBIntImpl(asView, myField.getChar(o));
                else if (c == Class.class)
                    return new DBStringImpl(asView, ((Class<?>) myField.get(o)).getName());
                else
                    throw new JoriaAssertionError("Primitive type not handled " + c);
            } else {
                Object fo = myField.get(o);
                return JavaMember.makeValue(jv, fo, asView, f.getType(), env);
            }
        } catch (IllegalArgumentException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with wrong object " + o.getClass() + " -> " + f.getLongName() + " in " + f.getDefiningClass() + " from " + myField.getDeclaringClass().getName());//trdone
        } catch (NullPointerException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with null object " + f.getLongName());
        } catch (IllegalAccessException e) {
            System.err.println("IllegalAccessException in accessing a Java Field " + this + " " + myField);//trdone
            throw new JoriaAssertionError("Access Error " + o + " -> " + f.getLongName());
        }
    }

    public DBData getMethodValue(JavaMethod m, JavaValue jv, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        Object ret = getMethodValueInner(m, jv);
        return JavaMember.makeValue(jv, ret, asView, m.getType(), env);
    }

    protected Object getMethodValueInner(JavaMethod m, JavaValue jv) throws JoriaDataException {
        Method myMethod = m.getMethod();
        Object o = jv.getJavaObject();
        loadObject(o);
        Object ret;
        try {
            ret = myMethod.invoke(o);
        } catch (InvocationTargetException e) {
            try {
                Trace.logError("InvocationTargetException in getValue from method " + myMethod + " on " + o + "of " + o.getClass());
                Trace.log(e.getTargetException());
                //e.printStackTrace();
            } catch (Throwable errorInHandler) {
                Trace.log(e);
                Trace.logError("InvocationTargetException in getValueFromMethod with error in Handler: " + errorInHandler.getMessage());
                Trace.log(errorInHandler);
                //e.printStackTrace();
            }
            throw new JoriaDataRetrievalExceptionInUserMethod("Error in customer supplied code.", e);//trdone
        } catch (IllegalAccessException e) {
            //System.err.println("IllegalAccessException in getValueFromMethod " + myMethod + " on " + o.toString()); //trdone
            Trace.log(e);
            //e.printStackTrace();
            throw new JoriaDataRetrievalExceptionInUserMethod("Access Error on user method", e);//trdone
        } catch (IllegalArgumentException e) {
            //System.err.println("IllegalArgumentException in getValueFromMethod " + myMethod + " on " + o.toString() + " " + o.getClass()); //trdone
            Trace.log(e);
            //e.printStackTrace();
            throw new JoriaAssertionError("Argument Error method: " + myMethod + "\n of expected class: " + myMethod.getDeclaringClass().getName() + "\n found object of: " + o.getClass() + "\n object: " + o + "\n Message: " + e.getMessage());//trdone
        }
        return ret;
    }

    public DBData getAttachedMethodValue(JavaAttachedMethod m, JavaValue jv, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        Object ret = getAttachedMethodValueInner(m, jv);
        return JavaMember.makeValue(jv, ret, asView, m.getType(), env);
    }

    protected Object getAttachedMethodValueInner(JavaAttachedMethod m, JavaValue jv){
        throw new Error("deprecated");
    }

    public long getIntField(JavaField f, JavaValue jv, RunEnv env) throws JoriaDataException {
        Trace.check(f.getType().isIntegerLiteral());
        Field myField = f.getField();
        Object o = jv.getJavaObject();
        loadObject(o);
        try {
            return myField.getLong(o);
        } catch (IllegalArgumentException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with wrong object " + o.getClass() + " -> " + f.getLongName() + " in " + f.getDefiningClass() + " from " + myField.getDeclaringClass().getName());//trdone
        } catch (NullPointerException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with null object " + f.getLongName());
        } catch (IllegalAccessException e) {
            Trace.log(e);
            //System.out.println("IllegalAccessException in accessing a Java Field " + this + " " + myField); //trdone
            throw new JoriaAssertionError("Access Error " + o + " -> " + f.getLongName());
        }
    }

    public double getFloatField(JavaField f, JavaValue jv, RunEnv env) throws JoriaDataException {
        Trace.check(f.getType().isRealLiteral() || f.getType().isIntegerLiteral());
        Object o = jv.getJavaObject();
        loadObject(o);
        Field myField = f.getField();
        try {
            final Class<?> type = myField.getType();
            if (type.isPrimitive())
                return myField.getDouble(o);
            else if (type == Double.class) {
                final Double fieldObj = (Double) myField.get(o);
                return fieldObj.doubleValue();
            } else if (type == Float.class) {
                final Float fieldObj = (Float) myField.get(o);
                return fieldObj.doubleValue();
            }
            throw new JoriaAssertionError("getFloatField with wrong member type " + f.getDefiningClass() + "." + f.getLongName() + " from " + myField.getDeclaringClass().getName() + " found field type " + myField.getClass() + " and object " + o.getClass());//trdone
        } catch (IllegalArgumentException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getFloatField with wrong object type " + f.getDefiningClass() + "." + f.getLongName() + " from " + myField.getDeclaringClass().getName() + " found field type " + myField.getClass() + " and object " + o.getClass());//trdone
        } catch (NullPointerException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getFloatField with null object " + f.getLongName());
        } catch (IllegalAccessException e) {
            Trace.log(e);
            //System.out.println("IllegalAccessException in accessing a Java Field " + this + " " + myField); //trdone
            throw new JoriaAssertionError("Access Error " + o + " -> " + f.getLongName());
        }
    }

    public int getBooleanField(JavaField f, JavaValue jv, RunEnv env) throws JoriaDataException {
        Trace.check(f.getType().isBooleanLiteral());
        Field myField = f.getField();
        Object o = jv.getJavaObject();
        loadObject(o);
        try {
            if (myField.getBoolean(o))
                return JoriaAccessTyped.ValForTrue;
            else
                return JoriaAccessTyped.ValForFalse;
        } catch (IllegalArgumentException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with wrong object " + o.getClass() + " -> " + f.getLongName() + " in " + f.getDefiningClass() + " from " + myField.getDeclaringClass().getName());//trdone
        } catch (NullPointerException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with null object " + f.getLongName());
        } catch (IllegalAccessException e) {
            Trace.log(e);
            //System.out.println("IllegalAccessException in accessing a Java Field " + this + " " + myField); //trdone
            throw new JoriaAssertionError("Access Error " + o + " -> " + f.getLongName());
        }
    }

    public String getStringField(JavaField f, JavaValue jv, RunEnv env) throws JoriaDataException {
        //Trace.check(f.getType().isStringLiteral());
        Field myField = f.getField();
        Object o = jv.getJavaObject();
        loadObject(o);
        try {
            Class<?> c = myField.getType();
            if (c == Class.class)
                return ((Class<?>) myField.get(o)).getName();
            if (Object.class.isAssignableFrom(c) && c != String.class) {
                Object oo = myField.get(o);
                if (oo != null)
                    return oo.toString();
                else
                    return "null";//trdone
            }
            return (String) myField.get(o);
        } catch (IllegalArgumentException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with wrong object " + o.getClass() + " -> " + f.getLongName() + " in " + f.getDefiningClass() + " from " + myField.getDeclaringClass().getName());//trdone
        } catch (NullPointerException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with null object " + f.getLongName());
        } catch (IllegalAccessException e) {
            Trace.log(e);
            //System.out.println("IllegalAccessException in accessing a Java Field " + this + " " + myField); //trdone
            throw new JoriaAssertionError("Access Error " + o + " -> " + f.getLongName());
        }
    }

    public Date getDateField(JavaField f, JavaValue jv, RunEnv env) throws JoriaDataException {
        Trace.check(f.getType().isDate());
        Field myField = f.getField();
        Object o = jv.getJavaObject();
        loadObject(o);
        try {
            Object rVal = myField.get(o);
            if (rVal instanceof Calendar)
                return ((Calendar) rVal).getTime();
            else
                return (Date) rVal;
        } catch (IllegalArgumentException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with wrong object " + o.getClass() + " -> " + f.getLongName() + " in " + f.getDefiningClass() + " from " + myField.getDeclaringClass().getName());//trdone
        } catch (NullPointerException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with null object " + f.getLongName());
        } catch (IllegalAccessException e) {
            Trace.log(e);
            //System.out.println("IllegalAccessException in accessing a Java Field " + this + " " + myField); //trdone
            throw new JoriaAssertionError("Access Error " + o + " -> " + f.getLongName());
        }
    }

    public Date getDateMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException {
        Trace.check(m.getType().isDate());
        Object rVal = getMethodValueInner(m, from);
        if (rVal instanceof Calendar)
            return ((Calendar) rVal).getTime();
        else if (rVal instanceof DateTime)
            return ((DateTime) rVal).toDate();
        else
            return (Date) rVal;
    }

    public Object getObjectField(JavaField f, JavaValue jv, RunEnv env) throws JoriaDataException {
        Field myField = f.getField();
        Object o = jv.getJavaObject();
        loadObject(o);
        try {
            return myField.get(o);
        } catch (IllegalArgumentException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with wrong object " + o.getClass() + " -> " + f.getLongName() + " in " + f.getDefiningClass() + " from " + myField.getDeclaringClass().getName());//trdone
        } catch (NullPointerException e) {
            Trace.log(e);
            //e.printStackTrace();
            //System.out.println("JavaObject: " + o); //trdone
            throw new JoriaAssertionError("getValueFromField with null object " + f.getLongName());
        } catch (IllegalAccessException e) {
            Trace.log(e);
            //System.out.println("IllegalAccessException in accessing a Java Field " + this + " " + myField); //trdone
            throw new JoriaAssertionError("Access Error " + o + " -> " + f.getLongName());
        }
    }

    public Object getObjectMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException {
        return getMethodValueInner(m, from);
    }

    public void evictObject(Object o) {
        // default does nothing
    }

    public long getIntMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException {
        Trace.check(m.getType().isIntegerLiteral());
        Object rVal = getMethodValueInner(m, from);
        if (rVal == null)
            return Long.MIN_VALUE + 1;
        return ((Number) rVal).longValue();
    }

    public double getFloatMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException {
        Trace.check(m.getType().isRealLiteral() || m.getType().isIntegerLiteral());
        Object rVal = getMethodValueInner(m, from);
        if (rVal == null)
            return Double.NaN;
        return ((Number) rVal).doubleValue();
    }

    public int getBooleanMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException {
        Trace.check(m.getType().isBooleanLiteral());
        Object rVal = getMethodValueInner(m, from);
        if (rVal == null)
            return JoriaAccessTyped.ValForNull;
        else if ((Boolean) rVal)
            return JoriaAccessTyped.ValForTrue;
        else
            return JoriaAccessTyped.ValForFalse;
    }

    public String getStringMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException {
        //		Trace.check(m.getType().isStringLiteral());
        Object rVal = getMethodValueInner(m, from);
        if (rVal != null) {
            if (Class.class == rVal.getClass())
                return ((Class<?>) rVal).getName();
            else
                return rVal.toString();
        }
        return (String) rVal;
    }

    public boolean isPersistentClass(Class<?> definingClass) {
        return true;
    }

    public Class<?> loadMangledClass(String name) {
        //System.out.println("loadMangledClass = " + name); //trdone
        if (name.endsWith("[]")) {
            name = "[L" + name.substring(0, name.length() - 2) + ';';//trdone
        }
        try {
            return null;//contentClassloader.loadClass(name);
        } catch (Error ex) {
            int index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(0, index) + "$" + name.substring(index + 1);
                return loadMangledClass(name);
            } else
                throw new Error(ex);
        }
    }

}
