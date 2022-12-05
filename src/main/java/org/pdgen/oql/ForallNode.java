// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ForallNode extends Node {
    protected IteratorRef iteratorVariable;
    protected NodeInterface left;
    protected NodeInterface right;

    public ForallNode(NodeInterface p1, NodeInterface p2, IteratorRef it) {
        iteratorVariable = it;
        left = p1;
        right = p2;
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        DBCollection coll = (DBCollection) left.getValue(env, p0);
        if (coll == null || coll.isNull())
            return false;
        coll.reset();
        boolean ret = true;
        while (coll.next()) {
            DBData el = coll.current();
            final boolean b = right.getBooleanValue(env, el);
            if (!b) {
                ret = false;
                break;
            }
        }
        coll.reset();
        return ret;
    }

    public String getTokenString() {
        return " for all " + iteratorVariable.getName() + " in " + left.getTokenString() + ": " + right.getTokenString();
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 2;
        optBraceEq(bindingLevel, newLevel, collector, '(');
        collector.append(" for all ");
        collector.append(iteratorVariable.getName());
        collector.append(" in ");
        left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        collector.append(": ");
        right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        optBraceEq(bindingLevel, newLevel, collector, ')');
    }


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }

    public boolean isBoolean() {
        return true;
    }

    public boolean hasMofifiedAccess() {
        return left.hasMofifiedAccess() || right.hasMofifiedAccess();
    }

    public void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException {
        left.cacheDeferredFields(env, from);
        right.cacheDeferredFields(env, from);
    }

    public void i18nKeys(HashMap<String, List<I18nKeyHolder>> collect) {
        left.i18nKeys(collect);
        right.i18nKeys(collect);
    }

    public void getUsedAccessors(Set<JoriaAccess> ret) {
        left.getUsedAccessors(ret);
        right.getUsedAccessors(ret);
    }

    public boolean hasText(final String text, final boolean searchLabels, final boolean searchData) {
        return left.hasText(text, searchLabels, searchData) || right.hasText(text, searchLabels, searchData);
    }

    public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen) {
        left.collectVariables(set, seen);
        right.collectVariables(set, seen);
    }
}
