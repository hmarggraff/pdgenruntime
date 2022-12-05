// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.env.Env;

import java.io.ObjectStreamException;

public class JavaList extends AbstractJoriaCollection {
    private static final long serialVersionUID = 7L;
    protected transient Class<?> javaClass;
    boolean large;

    public JavaList(Class<?> aJavaClass, JoriaClass eltype) {
        super(eltype);
        if (aJavaClass == null)
            throw new Error("Cannot make JavaList when collection class is null");
        javaClass = aJavaClass;
        name = makeCollectionName(javaClass, elementType);
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public static String makeCollectionName(Class<?> javaClass, JoriaClass elementType) {
        String elementTypeName = elementType.getName();
        if (elementTypeName.startsWith("java.lang.")) {
            elementTypeName = elementTypeName.substring("java.lang.".length());
        }

        if (javaClass.isArray()) {
            return "Array<" + elementTypeName + ">";
        } else {
            String collectionClassName = javaClass.getName();
            if (collectionClassName.startsWith("java.util.")) {
                collectionClassName = collectionClassName.substring("java.util.".length());
            } else if (collectionClassName.startsWith("java.lang.")) {
                collectionClassName = collectionClassName.substring("java.lang.".length());
            }
            return collectionClassName + "<" + elementTypeName + ">";
        }
    }

    protected Object readResolve() throws ObjectStreamException {
        JoriaSchema sch = Env.schemaInstance;
        JoriaType cls = sch.findInternalType(name);
        if (cls != null)
            return cls;
        return JoriaUnknownType.createJoriaUnknownType(name);
    }

    public void setElementType(JoriaClass val) {
        if (val == null)
            throw new Error("JavaList.setElementType(null) not allowed.");
        elementType = val;
        name = makeCollectionName(javaClass, elementType);
    }

    public boolean isLarge() {
        return large;
    }

    public void setLarge(boolean large) {
        this.large = large;
    }
}
