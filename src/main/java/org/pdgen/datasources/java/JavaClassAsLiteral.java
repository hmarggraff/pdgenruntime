// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.datasources.java;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.AbstractJoriaLiteral;

import java.io.ObjectStreamException;

public class JavaClassAsLiteral extends AbstractJoriaLiteral {

    private static final long serialVersionUID = 7L;
    protected String myName = "Class";
    protected static JavaClassAsLiteral theInstance = new JavaClassAsLiteral();

    private JavaClassAsLiteral() {
    }

    public static JavaClassAsLiteral instance() {
        return theInstance;
    }

    /* ----------------------------------------------------------------------- readResolve */
    protected Object readResolve() throws ObjectStreamException {
        return theInstance;
    }

    /* ----------------------------------------------------------------------- isLiteral */
    public boolean isLiteral() {
        return true;
    }

    public String getName() {
        return myName;
    }

    /* ----------------------------------------------------------------------- isStringLiteral */
    public boolean isStringLiteral() {
        return true;
    }

    public String getParamString() {
        return "JavaClassAsLiteral";
    }
}
