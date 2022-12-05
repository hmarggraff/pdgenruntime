// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class ClassNameNode extends Node implements JoriaTypedNode {

    // fields
    protected JoriaType type;

    public ClassNameNode(JoriaType p0) {
        type = p0;
    }

    /* ----------------------------------------------------------------------- getTokenString */
    public String getTokenString() {
        return '(' + type.getName() + ')';
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        collector.append("(");
        collector.append(type.getName());
        collector.append(")");
    }


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        throw new JoriaAssertionError("Default implementation of getValue may not be called. Origin: " + getClass());
    }

    /* ----------------------------------------------------------------------- getType */
    public JoriaType getType() {
        return type;
    }

    public JoriaType getElementType() {
        if (type.isCollection())
            return ((JoriaCollection) type).getElementType();
        return null;
    }

    public boolean hasMofifiedAccess() {
        return type instanceof JoriaUnknownType;
    }
}
