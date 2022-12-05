// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class DeferredFieldNode extends UnaryOperatorNode implements JoriaTypedNode {
    public DeferredFieldNode(Node sub) {
        super(sub);
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        final DBData value = getValue(env, p0);
        //noinspection SimplifiableIfStatement
        if (value == null || value.isNull())
            return false;
        return ((DBBoolean) value).getBooleanValue();
    }

    public char getCharacterValue(RunEnv env, DBData p0) throws JoriaDataException {
        final DBData value = getValue(env, p0);
        if (value == null || value.isNull())
            return DBInt.CHARNULL;
        return (char) ((DBInt) value).getIntValue();
    }

    public DBCollection getCollection(RunEnv env, DBData p0) throws JoriaDataException {
        return (DBCollection) getValue(env, p0);
    }

    public double getFloatValue(RunEnv env, DBData from) throws JoriaDataException {
        final DBData value = getValue(env, from);
        if (value == null || value.isNull())
            return DBReal.NULL;
        return ((DBReal) value).getRealValue();
    }

    public long getIntValue(RunEnv env, DBData from) throws JoriaDataException {
        final DBData value = getValue(env, from);
        if (value == null || value.isNull())
            return DBInt.NULL;
        return ((DBInt) value).getIntValue();
    }

    public String getStringValue(RunEnv env, DBData from) throws JoriaDataException {
        final DBData value = getValue(env, from);
        if (value == null || value.isNull())
            return null;
        else
            return ((DBString) value).getStringValue();
    }

    public String getTokenString() {
        return sub.getTokenString();
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        sub.buildTokenStringWithRenamedAccess(access, newName, collector, 12);
    }

    public JoriaType getType() {
        return ((FieldNode) sub).getType();
    }

    public JoriaType getElementType() {
        return ((FieldNode) sub).getElementType();
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return env.getRuntimeParameterValue(((FieldNode) sub).getAccess());
    }

}
