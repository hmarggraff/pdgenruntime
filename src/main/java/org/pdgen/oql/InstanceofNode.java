// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;

import java.util.Set;

public class InstanceofNode extends Node {
    NodeInterface lval;
    JoriaClass typ;

    public InstanceofNode(NodeInterface left, JoriaClass right) {
        lval = left;
        typ = right;
    }

    public String getTokenString() {
        return lval.getTokenString() + " instanceof " + typ.getName();
    }


    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 7;
        optBrace(bindingLevel, newLevel, collector, '(');
        lval.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        collector.append(" instanceof ");
        collector.append(typ.getName());
        optBrace(bindingLevel, newLevel, collector, ')');
    }


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }

    public boolean isBoolean() {
        return true;
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        DBData d = lval.getValue(env, p0);
        if (!(d instanceof DBObject) || d.isNull())
            return false;
        JoriaType ac = d.getActualType();
        return ac instanceof JoriaClass && JoriaClassHelper.isAssignableFrom(d, typ);
    }

    public boolean hasMofifiedAccess() {
        return lval.hasMofifiedAccess() || typ instanceof JoriaUnknownType;
    }

    public void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException {
        lval.cacheDeferredFields(env, from);
    }

    public void getUsedAccessors(Set<JoriaAccess> ret) {
        lval.getUsedAccessors(ret);
    }

    public boolean hasText(final String text, final boolean searchLabels, final boolean searchData) {
        return lval.hasText(text, searchLabels, searchData);
    }

    public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen) {
        lval.collectVariables(set, seen);
    }
}
