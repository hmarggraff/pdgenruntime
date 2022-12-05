// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.DBCollection;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.RunEnv;

public class ExistsNode extends ForallNode {

    public ExistsNode(NodeInterface p1, NodeInterface p2, IteratorRef it) {
        super(p1, p2, it);
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        DBCollection collVal = (DBCollection) left.getValue(env, p0);
        if (collVal == null || collVal.isNull())
            return false;
        boolean ret = false;
        while (collVal.next()) {
            DBData d = collVal.current();
            if (right.getBooleanValue(env, d)) {
                ret = true;
                break;
            }
        }
        collVal.reset();
        return ret;
    }

    public String getTokenString() {
        return "exists " + iteratorVariable.getName() + " in " + left.getTokenString() + ": " + right.getTokenString();
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 2;
        optBraceEq(bindingLevel, newLevel, collector, '(');
        collector.append(" exists ");
        collector.append(iteratorVariable.getName());
        collector.append(" in ");
        left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        collector.append(": ");
        right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        optBraceEq(bindingLevel, newLevel, collector, ')');
    }

}
