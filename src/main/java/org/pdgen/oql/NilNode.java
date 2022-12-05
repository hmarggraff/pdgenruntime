// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;


public class NilNode extends Node implements JoriaTypedNode {

    public String getTokenString() {
        return " null ";
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        collector.append(" null ");
    }


    public JoriaType getType() {
        return JoriaClassVoid.voidType;
    }

    public JoriaType getElementType() {
        return null;
    }

    public DBData getValue(RunEnv env, DBData p0) {
        return null;
    }

    public boolean isCollection() {
        return true;
    }

    public boolean isLiteralCollection() {
        return true;
    }

    public boolean isDictionary() {
        return true;
    }

    public boolean isObject() {
        return true;
    }

    public boolean isString() {
        return true;
    }

    public boolean isDate() {
        return true;
    }

    public boolean isCharacter() {
        return true;
    }

    public boolean isInteger() {
        return true;
    }

    public boolean isReal() {
        return true;
    }

    public DBCollection getCollection(RunEnv env, DBData p0) throws JoriaDataException {
        return null;
    }

    public String getStringValue(RunEnv env, DBData p0) throws JoriaDataException {
        return null;
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        return false;
    }

    public char getCharacterValue(RunEnv env, DBData p0) throws JoriaDataException {
        return DBInt.CHARNULL;
    }

    public double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException {
        return DBReal.NULL;
    }

    public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException {
        return DBInt.NULL;
    }


}
