// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class ModuloNode extends BinaryOperatorNode {

    public ModuloNode(int p0, NodeInterface p1, NodeInterface p2) {
        super(p0, p1, p2);
    }

    public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException {
        long intValueLeft = left.getIntValue(env, p0);
        long intValueRight = right.getIntValue(env, p0);
        if (intValueLeft == DBInt.NULL || intValueRight == DBInt.NULL)
            return DBInt.NULL;
        return intValueLeft % intValueRight;
    }

    public String getTokenString() {
        return left.getTokenString() + " mod " + right.getTokenString();
    }


    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 6;
        optBrace(bindingLevel, newLevel, collector, '(');
        left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        collector.append(" mod ");
        right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        optBrace(bindingLevel, newLevel, collector, ')');
    }


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return new DBIntImpl(null, getIntValue(env, p0));
    }
}
