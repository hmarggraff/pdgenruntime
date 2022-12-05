// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.model.run.RunEnv;
import org.pdgen.projection.PseudoAccess;


public class ThisNode extends Node implements JoriaTypedNode {

    protected JoriaType typ;
    private PseudoAccess deferredAxs;

    public ThisNode(JoriaType t) {
        typ = t;
    }

    public DBCollection getCollection(RunEnv env, DBData p0) throws JoriaDataException {
        return (DBCollection) p0;
    }

    public String getTokenString() {
        return " this ";
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        collector.append(" this ");
    }


    public JoriaType getType() {
        return typ;
    }

    public JoriaType getElementType() {
        if (typ.isCollection())
            return ((JoriaCollection) typ).getElementType();
        return null;
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        if (deferredAxs == null)
            return p0;
        return env.getRuntimeParameterValue(deferredAxs);
    }

    public boolean isBoolean() {
        return typ.isBooleanLiteral();
    }

    public boolean isCharacter() {
        return typ.isCharacterLiteral();
    }

    public boolean isCollection() {
        return typ.isCollection();
    }

    public boolean isLiteralCollection() {
        return typ.isLiteralCollection();
    }

    public boolean isDictionary() {
        return typ.isDictionary();
    }

    public boolean isInteger() {
        return typ.isIntegerLiteral();
    }

    public boolean isReal() {
        return typ.isRealLiteral();
    }

    public boolean isObject() {
        return typ.isClass();
    }

    public boolean isString() {
        return typ.isStringLiteral();
    }

    public boolean hasMofifiedAccess() {
        return typ instanceof JoriaUnknownType;
    }

    public void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException {
        deferredAxs = new PseudoAccess(typ, "this");
        env.putRuntimeParameter(deferredAxs, from);
    }

}
