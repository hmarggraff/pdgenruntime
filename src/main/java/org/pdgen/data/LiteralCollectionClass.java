// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import org.pdgen.env.Env;

public class LiteralCollectionClass extends AbstractJoriaClass {
    private static final long serialVersionUID = 7L;
    protected String name;
    transient JoriaType literalType;
    transient LiteralCollectionMember myMember;


    public LiteralCollectionClass(JoriaType literalType) {
        name = buildName(literalType);
        myMember = new LiteralCollectionMember(this, literalType);
        members = new JoriaAccess[]{myMember};
        this.literalType = literalType;
    }

    public String getParamString() {
        return getName();
    }

    public String getName() {
        return name;
    }

    protected Object readResolve() {
        Named that = Env.schemaInstance.findInternalType(name);
        if (that == null) {
            name = name.replace('.', '_');
            that = Env.schemaInstance.findInternalType(name);
        }
        if (that == null || !(that instanceof LiteralCollectionClass))
            return JoriaUnknownType.createJoriaUnknownType(name);
        else
            return that;
    }

    public static String buildName(JoriaType literalType) {
        return "C<" + literalType.getName() + ">";
    }

    public JoriaType getLiteralType() {
        return literalType;
    }
}
