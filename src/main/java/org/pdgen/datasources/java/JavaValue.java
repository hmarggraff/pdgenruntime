// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

import org.pdgen.data.AbstractDBObject;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaType;
import org.pdgen.env.Env;

public class JavaValue extends AbstractDBObject {

    protected Object myValue;

    public JavaValue(Object o, JoriaAccess a) {
        super(a);
        myValue = o;
    }

    public JoriaType getActualType() {
        Class<?> c = myValue.getClass();
        JavaSchema sch = (JavaSchema) Env.schemaInstance;
        JoriaType t = sch.findClassOrType(c);
        return t;
    }

    public boolean isAssignableTo(JoriaType t) {
        if (!(t instanceof JavaClass))
            return false;
        Class<?> tc = ((JavaClass) t).myClass;
        return tc.isAssignableFrom(myValue.getClass());
    }

    public Object getJavaObject() {
        return myValue;
    }

    public boolean isNull() {
        return myValue == null;
    }

    public String toString() {
        return myValue.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof JavaValue) {
            JavaValue v = (JavaValue) o;
            if (myValue == null)
                return v.myValue == null;
            else
                return myValue.equals(v.myValue);
        } else
            return false;
    }

    public int hashCode() {
        return myValue.hashCode();
    }

    public boolean same(DBData theOther) {
        if (theOther instanceof JavaValue) {
            JavaValue v = (JavaValue) theOther;
            if (myValue == null)
                return v.myValue == null;
            else
                return myValue.equals(v.myValue);
        } else
            return false;
    }
}
