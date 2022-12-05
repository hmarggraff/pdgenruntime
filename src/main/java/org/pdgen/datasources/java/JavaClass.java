// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.env.Env;

import java.io.ObjectStreamException;

public class JavaClass extends AbstractJoriaClass implements JoriaPhysicalClass {
    private static final long serialVersionUID = 7L;
    protected String myName;
    protected boolean internal;
    protected transient Class<?> myClass;

    public JavaClass(Class<?> c) {
        myClass = c;
        myName = c.getName();
    }

    public JavaClass(Class<?> c, String name) {
        myClass = c;
        myName = name;
    }

    public String getName() {
        return myName;
    }

    public String getParamString() {
        return "JavaClass[" + myClass.getName() + "]";
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isUnknown() {
        return Object.class.equals(myClass);
    }

    public void setBaseClasses(JoriaClass[] bases) {
        baseClasses = bases;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public void setName(String name) {
        myName = name;
    }

    public void setMembers(JoriaAccess[] members) {
        this.members = members;
        flat_members = null;
    }

    public Class<?> theClass() {
        return myClass;
    }

    protected Object readResolve() throws ObjectStreamException {
        JoriaClass cls = Env.schemaInstance.findClass(myName);
        if (cls != null) {
            return cls;
        } else {
            return JoriaUnknownType.createJoriaUnknownType(myName);
        }
    }

    public String getPhysicalClassName() {
        return myClass.getName();
    }

}

