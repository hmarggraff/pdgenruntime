// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.oql;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.model.run.RunEnv;

import java.util.Set;

public class VarNode extends Node implements JoriaTypedNode {
    RuntimeParameter runtimeParameter;
    String tokenString;

    public VarNode(RuntimeParameter variable) {
        runtimeParameter = variable;
        tokenString = "$" + runtimeParameter.getName();
    }

    public String getName() {
        return runtimeParameter.getName();
    }

    public JoriaType getType() {
        return runtimeParameter.getType();
    }

    public JoriaType getElementType() {
        return null;
    }

    public double getFloatValue(RunEnv env, DBData p0) throws JoriaDataException {
        Trace.check(isReal());
        DBData data = env.getRuntimeParameterValue(runtimeParameter);
        if (data instanceof DBNull)
            return DBReal.NULL;
        DBReal dbr = (DBReal) data;
        if (dbr == null || dbr.isNull())
            return DBReal.NULL;
        return dbr.getRealValue();
    }

    public boolean isReal() {
        return runtimeParameter.getType().isRealLiteral();
    }

    public boolean isObject() {
        return runtimeParameter.getType().isClass();
    }

    public boolean isDictionary() {
        return runtimeParameter.getType().isDictionary();
    }

    public boolean isCollection() {
        return runtimeParameter.getType().isCollection();
    }

    public boolean isLiteralCollection() {
        return runtimeParameter.getType().isLiteralCollection();
    }

    public long getIntValue(RunEnv env, DBData p0) throws JoriaDataException {
        Trace.check(isInteger());
        DBData data = env.getRuntimeParameterValue(runtimeParameter);
        if (data instanceof DBNull)
            return DBInt.NULL;
        DBInt dbr = (DBInt) data;
        if (dbr == null || dbr.isNull())
            return DBInt.NULL;
        return dbr.getIntValue();
    }

    public String getTokenString() {
        return tokenString;
    }

    public void buildTokenStringWithRenamedAccess(final JoriaAccess access, final String newName, final StringBuffer collector, final int bindingLevel) {
        collector.append(tokenString);
    }


    public boolean isInteger() {
        return runtimeParameter.getType().isIntegerLiteral();
    }

    public DBData getValue(RunEnv env, DBData p0) throws JoriaDataException {
        return env.getRuntimeParameterValue(runtimeParameter);
    }

    public boolean isString() {
        return runtimeParameter.getType().isStringLiteral();
    }

    public boolean isDate() {
        return runtimeParameter.getType().isDate();
    }

    public boolean isBoolean() {
        return runtimeParameter.getType().isBooleanLiteral();
    }

    public String getStringValue(RunEnv env, DBData p0) throws JoriaDataException {
        Trace.check(isString());
        DBData data = env.getRuntimeParameterValue(runtimeParameter);
        if (data instanceof DBNull)
            return null;
        DBString dbr = (DBString) data;
        if (dbr == null || dbr.isNull())
            return null;
        return dbr.getStringValue();
    }

    public boolean getBooleanValue(RunEnv env, DBData p0) throws JoriaDataException {
        Trace.check(isBoolean());
        DBData data = env.getRuntimeParameterValue(runtimeParameter);
        if (data instanceof DBNull)
            return false;
        DBBoolean dbr = (DBBoolean) data;
        //noinspection SimplifiableIfStatement
        if (dbr == null || dbr.isNull())
            return false;
        return dbr.getBooleanValue();
    }

    public void getUsedAccessors(Set<JoriaAccess> s) {
        s.add(runtimeParameter);
        runtimeParameter.getUsedAccessors(s);
    }

    public boolean hasText(final String text, final boolean searchLabels, final boolean searchData) {
        return searchLabels && tokenString != null && tokenString.toLowerCase().contains(text);
    }

    public void collectVariables(final Set<RuntimeParameter> set, final Set<Object> seen) {
        if (seen.contains(runtimeParameter))
            return;
        set.add(runtimeParameter);
        seen.add(runtimeParameter);
        runtimeParameter.collectVariables(set, seen);

    }
}
