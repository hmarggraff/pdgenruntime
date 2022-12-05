// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.env.Env;
import org.pdgen.model.run.RunEnv;

import java.lang.reflect.Field;
import java.util.Date;

public class JavaField extends JavaMember {
    private static final long serialVersionUID = 7L;
    public transient Field myField;

    public JavaField(JavaClass a, Field f, JoriaType t) {
        super(a, f.getName(), t);
        myField = f;
    }

    public JavaField(JavaClass a, Field f, JoriaType t, String name) {
        super(a, name, t);
        myField = f;
    }

    public Field getField() {
        return myField;
    }

    public DBData getValue(DBData jv, JoriaAccess asView, RunEnv env) throws JoriaDataException {
        if (jv == null)
            throw new JoriaDataException("getValue from Java field with null object");
        if (!(jv instanceof JavaValue))
            throw new JoriaDataException("getValue from field with wrong object type " + jv.getClass().getName() + " field: " + getName());
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getFieldValue(this, (JavaValue) jv, asView, env);
    }

    public String toString() {
        return getLongName();
    }

    public long getIntValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getIntField(this, (JavaValue) from, env);
    }

    public double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getFloatField(this, (JavaValue) from, env);
    }

    public int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getBooleanField(this, (JavaValue) from, env);
    }

    public String getStringValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getStringField(this, (JavaValue) from, env);
    }

    public Date getDateValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getDateField(this, (JavaValue) from, env);
    }

    public Object getPictureValue(DBObject from, RunEnv env) throws JoriaDataException {
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        return sch.getReflectionDelegate().getObjectField(this, (JavaValue) from, env);
    }

}
