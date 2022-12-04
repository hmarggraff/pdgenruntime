// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.Filter;
import org.pdgen.data.view.MutableAccess;
import org.pdgen.data.view.SortOrder;
import org.pdgen.model.run.RunEnv;

@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class AbstractJoriaCollection implements JoriaCollection
{
    private static final long serialVersionUID = 7L;
    protected transient JoriaClass elementType;
	protected String name = "ListAny";

	protected AbstractJoriaCollection(JoriaClass et)
	{
		elementType = et;
		name = "List_" + getElementType();
	}

	protected AbstractJoriaCollection()
	{
	}

	public void setElementType(JoriaClass et)
	{
		elementType = et;
		name = "List_" + getElementType();
	}

	public JoriaClass getElementMatchType()
	{
		return null;
	}

	public JoriaClass getElementType()
	{
		return elementType;
	}

	public String getName()
	{
		return name;
	}

	public String getElementXmlTag()
	{
		return MutableAccess.escape(getElementMatchType().getName());
	}

	public String getParamString()
	{
		return getClass().toString() + "[" + name + "]";
	}

	public boolean isBlob()
	{
		return false;
	}

	public boolean isClass()
	{
		return false;
	}

	public boolean isCollection()
	{
		return true;
	}

	public boolean isDictionary()
	{
		return false;
	}

	public boolean isInternal()
	{
		return false;
	}

	public boolean isLiteral()
	{
		return false;
	}

	public boolean isUnknown()
	{
		return false;
	}

	public boolean isUserClass()
	{
		return false;
	}

	public boolean isView()
	{
		return false;
	}

	public boolean isVoid()
	{
		return false;
	}

	public String toString()
	{
		return name;
	}

	public boolean isLiteralCollection()
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

	public boolean isIntegerLiteral()
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

	public boolean isDate()
	{
		return false;
	}

	public boolean isImage()
	{
		return false;
	}

	public boolean isLarge()
	{
		return false;
	}

	public int getMinTopN(RunEnv env)
	{
		return 0;
	}

	public boolean hasFilterOrSortingOrTopN()
	{
		return false;
	}

	public SortOrder[] getSorting()
	{
		return null;
	}

	public Filter getFilter()
	{
		return null;
	}

	public void setFilter(Filter f)
	{
		// Abstract List has no filter
	}

	public JoriaClass getAsParent()
	{
		return elementType.getAsParent();
	}	
}
