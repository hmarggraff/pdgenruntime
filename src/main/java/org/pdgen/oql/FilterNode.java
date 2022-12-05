// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;
import org.pdgen.projection.ComputedDBCollectionValue;

import java.util.ArrayList;


public class FilterNode extends ForallNode implements JoriaTypedNode {

    public FilterNode(JoriaTypedNode p1, NodeInterface p2, IteratorRef it) {
        super(p1, p2, it);
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        throw new JoriaAssertionError("getBooleanValue may not be called for FilterNode");
    }

    public DBCollection getCollection(RunEnv env, DBData p0) throws JoriaDataException {
        DBCollection coll = (DBCollection) left.getValue(env, p0);
        if (coll == null || coll.isNull())
            return null;
        ArrayList<DBData> result = new ArrayList<DBData>();
        coll.reset();
        while (coll.next()) {
            DBData el = coll.current();
            if (right.getBooleanValue(env, el))
                result.add(el);
        }
        return new ComputedDBCollectionValue(result, p0.getAccess());
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return getCollection(env, p0);
    }

    public String getTokenString() {
        return "all " + iteratorVariable.getName() + " in " + left.getTokenString() + ": " + right.getTokenString();
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 2;
        optBraceEq(bindingLevel, newLevel, collector, '(');
        collector.append(" all ");
        collector.append(iteratorVariable.getName());
        collector.append(" in ");
        left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        collector.append(": ");
        right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        optBraceEq(bindingLevel, newLevel, collector, ')');
    }


    public boolean isCollection() {
        return true;
    }

    public boolean isLiteralCollection() {
        return false;
    }

    public boolean isObject() {
        return true;
    }

    public boolean isBoolean() {
        return false;
    }

    public JoriaType getType() {
        return ((JoriaTypedNode) left).getType();
    }

    public JoriaType getElementType() {
        return ((JoriaCollection) ((JoriaTypedNode) left).getType()).getElementType();
    }
}
