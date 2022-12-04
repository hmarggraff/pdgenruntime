// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.model.run.RunEnv;
import org.pdgen.data.AbstractTypedJoriaAccess;
import org.pdgen.data.JoriaType;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaAccess;
import org.pdgen.data.JoriaDataException;

public class DrillDownAccess extends AbstractTypedJoriaAccess
{
    private static final long serialVersionUID = 7L;

    public DrillDownAccess(String name, JoriaType typ)
	{
		super(name, typ);
	}

	public DBData getValue(DBData from, JoriaAccess asView, RunEnv env) throws JoriaDataException
	{
		return from;
	}
}
