// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.model.run.RunEnv;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.AbstractTypedJoriaAccess;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.data.JoriaType;

public class UnboundAccessSentinel extends AbstractTypedJoriaAccess
{
    private static final long serialVersionUID = 7L;

    public UnboundAccessSentinel(JoriaAccess old)
	{
		super(old.getName(), old.getType());
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		return null;
	}

	public boolean isBindable(JoriaAccess t)
	{
		JoriaType tt = t.getType();
		if (type.isClass() && tt.isClass())
			return true;
		if (type.isCollection() && tt.isCollection())
			return true;
		if (type.isStringLiteral() && tt.isStringLiteral())
			return true;
		if (type.isIntegerLiteral() && tt.isIntegerLiteral())
			return true;
		if (type.isRealLiteral() && tt.isRealLiteral())
			return true;
		if (type.isDate() && tt.isDate())
			return true;
		if (type.isBooleanLiteral() && tt.isBooleanLiteral())
			return true;
		if (type.isCharacterLiteral() && tt.isCharacterLiteral())
			return true;
		if (type.isBlob() && tt.isBlob())
			return true;
		if (type.isDictionary() && tt.isDictionary())
			return true;
		if (type.isImage() && tt.isImage())
			return true;
        return type.isLiteralCollection() && tt.isLiteralCollection();
	}
}
