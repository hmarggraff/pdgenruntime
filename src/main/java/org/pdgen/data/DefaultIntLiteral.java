// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;
//MARKER The strings in this file shall not be translated

import java.io.ObjectStreamException;


public class DefaultIntLiteral extends AbstractJoriaLiteral
{
    private static final long serialVersionUID = 7L;
    static DefaultIntLiteral theInstance = new DefaultIntLiteral();

	/* ----------------------------------------------------------------------- instance */
	public static DefaultIntLiteral instance()
	{
		return theInstance;
	}

	/* ----------------------------------------------------------------------- getName */
	public String getName()
	{
		return "int";
	}

	/* ----------------------------------------------------------------------- getParamString */
	public String getParamString()
	{
		return "DefaultIntLiteral";
	}

	/* ----------------------------------------------------------------------- isIntegerLiteral */
	public boolean isIntegerLiteral()
	{
		return true;
	}

	/* ----------------------------------------------------------------------- isLiteral */
	public boolean isLiteral()
	{
		return true;
	}

	/* ----------------------------------------------------------------------- readResolve */
	protected Object readResolve() throws ObjectStreamException
	{
		return theInstance;
	}
}
