// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class LessNode extends BooleanOperatorNode {
    public LessNode(int p0, NodeInterface p1, NodeInterface p2) {
        super(p0, p1, p2);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        if (commonOperandType == intType) {
            long l = left.getIntValue(env, p0);
            long r = right.getIntValue(env, p0);
            if (l == DBInt.NULL || r == DBInt.NULL)
                return false;
            return l < r;
        } else if (commonOperandType == stringType) {
            return compareStrings(left.getStringValue(env, p0), right.getStringValue(env, p0), env) < 0;
        } else if (commonOperandType == realType) {
            double l = left.getFloatValue(env, p0);
            double r = right.getFloatValue(env, p0);
            if (Double.isNaN(l) || Double.isNaN(r))
                return false;
            return l < r;
        } else if (commonOperandType == charType) {
            char l = left.getCharacterValue(env, p0);
            char r = right.getCharacterValue(env, p0);
            if (l == DBInt.CHARNULL || r == DBInt.CHARNULL)
                return false;
            return l < r;
        } else if (commonOperandType == dateType) {
            DBData l = left.getValue(env, p0);
            DBData r = right.getValue(env, p0);
            if (l == null || l.isNull() || r == null || r.isNull())
                return false;
            return ((DBDateTime) l).getDate().before(((DBDateTime) r).getDate());
        } else
            throw new JoriaAssertionError("Type mismatch when evaluating >=");
    }

    public String getTokenString() {
        return left.getTokenString() + " < " + right.getTokenString();
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 4;
        optBrace(bindingLevel, newLevel, collector, '(');
        left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        collector.append(" < ");
        right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        optBrace(bindingLevel, newLevel, collector, ')');
    }


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return new DBBooleanImpl(null, getBooleanValue(env, p0));
    }
}
