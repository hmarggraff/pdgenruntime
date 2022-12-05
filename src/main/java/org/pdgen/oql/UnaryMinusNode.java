// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class UnaryMinusNode extends UnaryOperatorNode {
    public UnaryMinusNode(NodeInterface p0) {
        super(p0);
    }

    public double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException {
        double value = sub.getFloatValue(env, p0);
        if (Double.isNaN(value))
            return DBReal.NULL;
        return -value;
    }

    public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException {
        long value = sub.getIntValue(env, p0);
        if (value == DBInt.NULL)
            return DBInt.NULL;
        return -value;
    }

    public String getTokenString() {
        return '-' + sub.getTokenString();
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 5;
        optBrace(bindingLevel, newLevel, collector, '(');
        collector.append(" - ");
        sub.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        optBrace(bindingLevel, newLevel, collector, ')');
    }


    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        DBData value = sub.getValue(env, p0);
        if (value == null || value.isNull())
            return null;
        if (value instanceof DBReal)
            return new DBRealImpl(value.getAccess(), -((DBReal) value).getRealValue());
        if (p0 instanceof DBInt)
            return new DBIntImpl(value.getAccess(), -((DBInt) value).getIntValue());
        throw new JoriaAssertionError("unexpected data type " + value.getClass());
    }
}
