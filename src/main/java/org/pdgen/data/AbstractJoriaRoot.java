// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.Env;

public abstract class AbstractJoriaRoot extends AbstractTypedJoriaAccess
{

	private static final long serialVersionUID = 7L;

	public AbstractJoriaRoot()
	{
	}

	public AbstractJoriaRoot(String name)
	{
		super(name);
	}

	public AbstractJoriaRoot(String name, JoriaType typ)
	{
		super(name, typ);
	}

	public boolean isRoot()
	{
		return true;
	}

	protected Object readResolve()
	{
		JoriaAccess r = Env.schemaInstance.getRoots().find(name);
		if (r == null)
			return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, null, JoriaModifiedAccess.rootNotFound, this, null);
		if (r.getType() != type)
			return JoriaModifiedAccess.createJoriaModifiedAccess(name, type, null, JoriaModifiedAccess.typeChanged, this, r);
		return r;
	}

}
