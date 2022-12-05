// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

//MARKER The strings in this file shall not be translated

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;
import org.pdgen.projection.PseudoAccess;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public abstract class Node implements NodeInterface, OQLNode {

    protected Node() {
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        throw new JoriaAssertionError("Default implementation of getValue may not be called. Origin: " + getClass());
    }

    public char getCharacterValue(RunEnv env, DBData p0) throws JoriaDataException {
        throw new JoriaAssertionError("Default implementation of getValue may not be called. Origin: " + getClass());
    }

    public DBCollection getCollection(RunEnv env, DBData p0) throws JoriaDataException {
        throw new JoriaAssertionError("Default implementation of getCollection may not be called. Origin: " + getClass());
    }

    public double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException {
        throw new JoriaAssertionError("Default implementation of getValue may not be called. Origin: " + getClass());
    }

    public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException {
        throw new JoriaAssertionError("Default implementation of getValue may not be called. Origin: " + getClass());
    }

    public String getStringValue(RunEnv env, DBData p0) throws JoriaDataException {
        throw new JoriaAssertionError("Default implementation of getValue may not be called. Origin: " + getClass());
    }

    /**
     * get a value of any type and wrap it as a DBData
     * Creation date: (18.1.00 14:06:11)
     *
     * @return DBData
     */
    public DBData getWrappedValue(RunEnv env, DBData from) throws JoriaDataException {
        if (isBoolean())
            return new DBBooleanImpl(new PseudoAccess(DefaultBooleanLiteral.instance()), getBooleanValue(env, from));
        else if (isInteger()) {
            long intValue1 = getIntValue(env, from);
            if (intValue1 == Long.MIN_VALUE + 1)
                return null;
            return new DBIntImpl(new PseudoAccess(DefaultIntLiteral.instance()), intValue1);
        } else if (isReal()) {
            double floatValue = getFloatValue(env, from);
            if (Double.isNaN(floatValue))
                return null;
            return new DBRealImpl(new PseudoAccess(DefaultRealLiteral.instance()), floatValue);
        } else if (isString()) {
            final String sValue = getStringValue(env, from);
            if (sValue == null)
                return null;
            else
                return new DBStringImpl(new PseudoAccess(DefaultStringLiteral.instance()), sValue);
        } else if (isCharacter()) {
            char characterValue = getCharacterValue(env, from);
            if (DBInt.CHARNULL == characterValue)
                return null;
            return new DBIntImpl(new PseudoAccess(DefaultCharacterLiteral.instance()), characterValue);
        } else {
            return getValue(env, from);
        }
    }

    public boolean isBoolean() {
        return false;
    }

    public boolean isCharacter() {
        return false;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isLiteralCollection() {
        return false;
    }

    public boolean isDictionary() {
        return false;
    }

    public boolean isInteger() {
        return false;
    }

    public boolean isReal() {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isDate() {
        return false;
    }

    public boolean isBlob() {
        return false;
    }

    public boolean hasMofifiedAccess() {
        return false;
    }

    public void cacheDeferredFields(final RunEnv env, final DBData from) throws JoriaDataException {
        //nothing to do
    }

    public void i18nKeys(HashMap<String, List<I18nKeyHolder>> collect) {
        //nothing to do
    }


    public String getTypeName() {
        if (isBoolean())
            return "boolean";
        else if (isInteger())
            return "int";
        else if (isReal())
            return "float";
        else if (isString())
            return "String";
        else if (isCharacter())
            return "char";
        else if (isDate())
            return "Date";
        else if (isCollection())
            return "Collection";
        else if (isObject())
            return "Object";
        else if (isLiteralCollection())
            return "LiteralCollection";
        else if (isBlob())
            return "BinaryData";
        else
            return "UnknownType";
    }

    public boolean hasText(final String text, final boolean searchLabels, final boolean searchData) {
        return false;
    }

    public void getUsedAccessors(final Set<JoriaAccess> ret) {
        // nothing to do
    }


    /**
     * emit a brace if needed in the expression.
     * This is if the outer binding level is greater, than the inner.
     *
     * @param oldLevel  outer binding level
     * @param newLevel  current binding level
     * @param collector output buffer
     * @param brace     left or right brace
     */
    protected void optBrace(int oldLevel, int newLevel, StringBuffer collector, char brace) {
        if (oldLevel > newLevel)
            collector.append(brace);
    }

    /**
     * emit a brace if needed in the expression for operators, that can not be chained
     * This is if the outer binding level is greater or equal, than the inner.
     *
     * @param oldLevel  outer binding level
     * @param newLevel  current binding level
     * @param collector output buffer
     * @param brace     left or right brace
     */
    protected void optBraceEq(int oldLevel, int newLevel, StringBuffer collector, char brace) {
        if (oldLevel >= newLevel)
            collector.append(brace);
    }

    public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen) {
        // nothing to do
    }
}
