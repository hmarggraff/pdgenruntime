// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

public class JoriaUserException extends JoriaException
{
    private static final long serialVersionUID = 7L;

    public JoriaUserException(String s)
	{
		super(s);
	}

	public JoriaUserException(Throwable cause)
	{
		super(cause);
	}

	public JoriaUserException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
