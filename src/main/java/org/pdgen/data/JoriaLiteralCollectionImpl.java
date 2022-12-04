// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.Res;

public class JoriaLiteralCollectionImpl implements JoriaLiteralCollection
{
    private static final long serialVersionUID = 7L;
    protected String name = Res.asis("LiteralCollection");
	protected JoriaType elementType;

	public JoriaLiteralCollectionImpl(JoriaType elementType)
	{
		this.elementType = elementType;
		makeName();
	}

	public JoriaLiteralCollectionImpl(JoriaType elementType, String tag)
	{
		this.elementType = elementType;
		name = tag + "_" + getElementLiteralType();
	}

	public JoriaLiteralCollectionImpl()
	{
	}

	protected void makeName()
	{
		name = Res.asis("LColl_") + getElementLiteralType();
	}

	public void setElementType(JoriaType aType)
	{
		elementType = aType;
		makeName();
	}

	public String getParamString()
	{
		return getClass().toString() + "[" + name + "," + elementType.getParamString() + "]";
	}

	public boolean isLiteralCollection()
	{
		return true;
	}

	public JoriaType getElementLiteralType()
	{
		return elementType;
	}

	public String getName()
	{
		return name;
	}

	public boolean isBlob()
	{
		return false;
	}

	public boolean isBooleanLiteral()
	{
		return false;
	}

	public boolean isCharacterLiteral()
	{
		return false;
	}

	public boolean isClass()
	{
		return false;
	}

	public boolean isCollection()
	{
		return false;
	}

	public boolean isDictionary()
	{
		return false;
	}

	public boolean isIntegerLiteral()
	{
		return false;
	}

	public boolean isInternal()
	{
		return true;
	}

	public boolean isLiteral()
	{
		return false;
	}

	public boolean isRealLiteral()
	{
		return false;
	}

	public boolean isStringLiteral()
	{
		return false;
	}

	public boolean isUnknown()
	{
		return false;
	}

	public boolean isUserClass()
	{
		return true;
	}

	public boolean isView()
	{
		return false;
	}

	public boolean isVoid()
	{
		return false;
	}

	public boolean isDate()
	{
		return false;
	}

	public boolean isImage()
	{
		return false;
	}

	public JoriaClass getAsParent()
	{
		return elementType.getAsParent();
	}
}
