// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;

//

/**
 * this class is used to create neutral (unbound) report templates
 * the members of this class can be added to a neutral report as placeholders to
 * be bound to actual data fields later.
 */
public class UnboundMembersClass extends DefaultJoriaClass {
    private static final long serialVersionUID = 7L;
    static UnboundMembersClass theInstance = new UnboundMembersClass();
    static JoriaAccess theAxsInstance = new PseudoAccess(theInstance);
    private static UnboundAccess unboundLiteral;
    public static UnboundAccess unboundInt;
    public static UnboundAccess unboundFloat;
    public static UnboundAccess unboundString;
    public static UnboundAccess unboundObject;
    public static UnboundAccess unboundCollection;


    private UnboundMembersClass() {
        super("Unbound");
        unboundObject = new UnboundAccess(this, "object", this);
        unboundLiteral = new UnboundAccess(this, "any", UnboundLiteralType.instance());
        unboundInt = new UnboundAccess(this, "integer", DefaultIntLiteral.instance());
        unboundFloat = new UnboundAccess(this, "float", DefaultRealLiteral.instance());
        unboundString = new UnboundAccess(this, "string", DefaultStringLiteral.instance());
        unboundCollection = new UnboundAccess(this, "collection", UnboundCollection.instance);
        members = new JoriaAccess[]{unboundString, unboundInt, unboundFloat, unboundLiteral, unboundObject, unboundCollection,};
        UnboundCollection.instance.setElementType(this);
    }

    public static UnboundMembersClass instance() {
        return theInstance;
    }

    public static JoriaAccess axsInstance() {
        return theAxsInstance;
    }

    public static UnboundAccess getUnboundLiteral() {
        return unboundLiteral;
    }

    public static JoriaAccess unboundFor(JoriaType t) {
        if (t.isCollection())
            return unboundCollection;
        else if (t.isClass())
            return unboundObject;
        else if (t.isStringLiteral())
            return unboundString;
        else if (t.isIntegerLiteral())
            return unboundInt;
        else if (t.isRealLiteral())
            return unboundFloat;
        else
            return unboundLiteral;
    }

    protected Object readResolve() {
        return theInstance;
    }
}
