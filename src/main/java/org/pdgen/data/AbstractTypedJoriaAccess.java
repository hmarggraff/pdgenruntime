// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.data.view.MutableView;
import org.pdgen.env.Res;

public abstract class AbstractTypedJoriaAccess extends AbstractJoriaAccess
{
    private static final long serialVersionUID = 7L;
    protected JoriaType type;

	protected AbstractTypedJoriaAccess()
	{
	}

	protected AbstractTypedJoriaAccess(String name)
	{
		super(name);
	}

	protected AbstractTypedJoriaAccess(String name, JoriaType typ)
	{
		super(name);
		type = typ;
	}

	public JoriaType getType()
	{
		return type;
	}

	protected JoriaAccess checkTypeForSchemaChange()
	{
		if (type instanceof MutableView)
		{
			MutableView mutableView = (MutableView) type;
			if (mutableView.fixAccess())
			{
				return new JoriaPlaceHolderAccess(name, type.getName() + Res.stri("affected_by_a_schema_change"));
			}
		}
		return null;
	}

	public JoriaAccess getPlaceHolderIfNeeded()
	{
		return checkTypeForSchemaChange();
	}
}
