// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import org.pdgen.data.AbstractJoriaClass;
import org.pdgen.data.JoriaClass;
import org.pdgen.data.JoriaAccess;

public class UnboundClassSentinel extends AbstractJoriaClass
{
    private static final long serialVersionUID = 7L;
    String name;
	public UnboundClassSentinel(JoriaClass ori)
	{
		super(new JoriaAccess[0]);
		name = ori.getName();
	}

	public String getParamString()
	{
		return name;
	}

	public String getName()
	{
		return name;
	}
}
