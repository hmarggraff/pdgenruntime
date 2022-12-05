// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class DotNode extends BinaryOperatorNode implements JoriaTypedNode {

    public DotNode(int p0, JoriaTypedNode p1, Node p2) {
        super(p0, p1, p2);
    }

    public DBCollection getCollection(RunEnv env, DBData d) throws JoriaDataException {
        return right.getCollection(env, left.getValue(env, d));
    }

    public double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException {
        final double d = right.getFloatValue(env, left.getValue(env, p0));
        return d;
    }

    public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException {
        return right.getIntValue(env, left.getValue(env, p0));
    }

    public String getStringValue(RunEnv env, DBData p0) throws JoriaDataException {
        return right.getStringValue(env, left.getValue(env, p0));
    }

    public String getTokenString() {
        return left.getTokenString() + '.' + right.getTokenString();
    }


    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        final int newLevel = 10;
        optBrace(bindingLevel, newLevel, collector, '(');
        left.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        collector.append(".");
        right.buildTokenStringWithRenamedAccess(access, newName, collector, newLevel);
        optBrace(bindingLevel, newLevel, collector, ')');
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        DBData lv = left.getValue(env, p0);
        if (lv == null || lv.isNull())
            return null;
        return right.getValue(env, lv);
    }

    public char getCharacterValue(RunEnv env, DBData p0) throws JoriaDataException {
        return right.getCharacterValue(env, left.getValue(env, p0));
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        return right.getBooleanValue(env, left.getValue(env, p0));
    }

    public JoriaType getElementType() {
        if (right instanceof JoriaTypedNode)
            return ((JoriaTypedNode) right).getElementType();
        return null;
    }

    public JoriaType getType() {
        if ((right instanceof JoriaTypedNode) && (isObject() || isCollection() || isDictionary() || isLiteralCollection()))
            return ((JoriaTypedNode) right).getType();
        if (isBoolean())
            return DefaultBooleanLiteral.instance();
        else if (isInteger())
            return DefaultIntLiteral.instance();
        else if (isReal())
            return DefaultRealLiteral.instance();
        else if (isString())
            return DefaultStringLiteral.instance();
        else if (isCharacter())
            return DefaultCharacterLiteral.instance();
        else if (isDate())
            return JoriaDateTime.instance();
        else
            throw new JoriaAssertionError("Unhandled type when getting query type " + getTokenString());
    }

}
