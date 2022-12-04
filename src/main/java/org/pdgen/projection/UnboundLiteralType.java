// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.projection;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.AbstractJoriaLiteral;

public class UnboundLiteralType extends AbstractJoriaLiteral
{
    private static final long serialVersionUID = 7L;
    protected static UnboundLiteralType theInstance = new UnboundLiteralType();

	private UnboundLiteralType()
	{
	}

	public String getParamString()
	{
		return "Unbound Literal";
	}

	public String getName()
	{
		return "Any";
	}

	public boolean isLiteral()
	{
	   return true;
	}

	public static UnboundLiteralType instance()
	{
		return theInstance;
	}

	protected Object readResolve()
	{
		return theInstance;
	}
}
