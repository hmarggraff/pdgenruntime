// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.JoriaException;

public class JoriaDataException extends JoriaException
{
    private static final long serialVersionUID = 7L;

    public JoriaDataException(String message)
	{
		super(message);
	}

    public JoriaDataException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
