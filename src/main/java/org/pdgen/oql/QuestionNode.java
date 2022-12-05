// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;

import java.util.Set;
//Date: Feb 6, 2002 8:04:41 PM

public class QuestionNode extends BinaryOperatorNode {

    NodeInterface cond;

    public QuestionNode(int p0, NodeInterface cond, NodeInterface p1, NodeInterface p2) {
        super(p0, p1, p2);
        this.cond = cond;
    }

    public String getTokenString() {
        return cond.getTokenString() + "?" + left.getTokenString() + ":" + right.getTokenString();
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        optBraceEq(bindingLevel, 0, collector, '(');
        cond.buildTokenStringWithRenamedAccess(access, newName, collector, 0);
        collector.append("?");
        left.buildTokenStringWithRenamedAccess(access, newName, collector, 0);
        collector.append(':');
        right.buildTokenStringWithRenamedAccess(access, newName, collector, 0);
        optBraceEq(bindingLevel, 0, collector, ')');
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        boolean c = cond.getBooleanValue(env, p0);
        if (c)
            return left.getBooleanValue(env, p0);
        else
            return right.getBooleanValue(env, p0);
    }

    public double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException {
        boolean c = cond.getBooleanValue(env, p0);
        if (c)
            return left.getFloatValue(env, p0);
        else
            return right.getFloatValue(env, p0);
    }

    public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException {
        boolean c = cond.getBooleanValue(env, p0);
        if (c)
            return left.getIntValue(env, p0);
        else
            return right.getIntValue(env, p0);
    }

    public String getStringValue(RunEnv env, DBData p0) throws JoriaDataException {
        boolean c = cond.getBooleanValue(env, p0);
        if (c)
            return left.getStringValue(env, p0);
        else
            return right.getStringValue(env, p0);
    }

    public boolean hasMofifiedAccess() {
        return cond.hasMofifiedAccess() || left.hasMofifiedAccess() || right.hasMofifiedAccess();
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        boolean c = cond.getBooleanValue(env, p0);
        if (c)
            return left.getValue(env, p0);
        else
            return right.getValue(env, p0);
    }

    @Override
    public void getUsedAccessors(final Set<JoriaAccess> ret) {
        super.getUsedAccessors(ret);
        cond.getUsedAccessors(ret);
    }

    @Override
    public boolean hasText(final String text, final boolean searchLabels, final boolean searchData) {
        return super.hasText(text, searchLabels, searchData) || cond.hasText(text, searchLabels, searchData);
    }

    public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen) {
        super.collectVariables(set, seen);
        cond.collectVariables(set, seen);
    }
}
