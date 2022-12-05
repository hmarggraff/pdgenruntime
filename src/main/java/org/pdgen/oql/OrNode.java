// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.DBBooleanImpl;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

public class OrNode extends BooleanOperatorNode {
    public OrNode(int p0, NodeInterface p1, NodeInterface p2) {
        super(p0, p1, p2);
    }

    /**
     * ----------------------------------------------------------------------- getBooleanValue
     */
    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        return left.getBooleanValue(env, p0) || right.getBooleanValue(env, p0);
    }

    /**
     * ----------------------------------------------------------------------- getTokenString
     */
    public String getTokenString() {
        return left.getTokenString() + " or " + right.getTokenString(); //trdone
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        optBrace(bindingLevel, 1, collector, '(');
        left.buildTokenStringWithRenamedAccess(access, newName, collector, 1);
        collector.append(" or ");
        right.buildTokenStringWithRenamedAccess(access, newName, collector, 1);
        optBrace(bindingLevel, 1, collector, ')');
    }


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }
}
