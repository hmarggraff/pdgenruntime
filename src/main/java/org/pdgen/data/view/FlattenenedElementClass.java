// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;
//MARKER The strings in this file shall not be translated

import org.pdgen.data.DefaultJoriaClass;
import org.pdgen.data.JoriaAccess;
import org.pdgen.env.Res;

public class FlattenenedElementClass extends DefaultJoriaClass
{
    private static final long serialVersionUID = 7L;

    public FlattenenedElementClass(JoriaAccess outer, JoriaAccess inner)
	{
		super(Res.asis("Flatten_") + inner.getCollectionTypeAsserted().getElementType().getName() + '_' + outer.getCollectionTypeAsserted().getElementType().getName());
		FlattenedElementAccess fo = new FlattenedElementAccess(this, Res.asis("outer"), outer.getCollectionTypeAsserted().getElementType(), false);
		FlattenedElementAccess fi = new FlattenedElementAccess(this, Res.asis("inner"), inner.getCollectionTypeAsserted().getElementType(), true);
		members = new JoriaAccess[]{fi, fo};
	}

	public String toString()
	{
		return myName;
	}
}
