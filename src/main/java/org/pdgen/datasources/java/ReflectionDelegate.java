// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

/**
 * This allows to call reflection indirectly so that object data bases which modify the class
 * files have a chance to modify the reflection calls.
 * This is used by the Versant database.
 **/
public interface ReflectionDelegate {

    ArrayList<Field> getFields(Class<?> c);

    Field[] getDeclaredFields(Class<?> c);

    Class<?>[] getInterfaces(Class<?> c);

    Method[] getMethods(Class<?> c);

    Field getField(Class<?> c, String n);

    Method getMethod(Class<?> c, String n);

    Method getMethod(Class<?> c, Class<?> e, String n);

    String getRealMethodName(Method method);

    boolean isInternal(Class<?> c);

    int getModifiers(Member f);

    Class<?> getSuperclass(Class<?> c);

    DBData getFieldValue(JavaField f, JavaValue jv, JoriaAccess asView, RunEnv env) throws JoriaDataException;

    DBData getMethodValue(JavaMethod m, JavaValue jv, JoriaAccess asView, RunEnv env) throws JoriaDataException;

    @SuppressWarnings("rawtypes")
    DBData getAttachedMethodValue(JavaAttachedMethod m, JavaValue jv, JoriaAccess asView, RunEnv env) throws JoriaDataException;

    void loadObject(Object o) throws JoriaDataException;

    long getIntField(JavaField f, JavaValue from, RunEnv env) throws JoriaDataException;

    double getFloatField(JavaField f, JavaValue from, RunEnv env) throws JoriaDataException;

    int getBooleanField(JavaField f, JavaValue from, RunEnv env) throws JoriaDataException;

    String getStringField(JavaField f, JavaValue from, RunEnv env) throws JoriaDataException;

    long getIntMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException;

    double getFloatMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException;

    int getBooleanMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException;

    String getStringMethod(JavaMethod m, JavaValue from, RunEnv env) throws JoriaDataException;

    Date getDateField(JavaField am, JavaValue from, RunEnv env) throws JoriaDataException;

    Date getDateMethod(JavaMethod am, JavaValue from, RunEnv env) throws JoriaDataException;

    Object getObjectField(JavaField javaField, JavaValue javaValue, RunEnv env) throws JoriaDataException;

    Object getObjectMethod(JavaMethod javaMethod, JavaValue javaValue, RunEnv env) throws JoriaDataException;

    /**
     * set the object to the hollow state to allow dependent object to be garbage collected
     *
     * @param o the object to garbage collect
     */
    void evictObject(Object o);

}
