// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

/**
 * This is the base class of all exceptions that are thrown by Pdgen
 * api functions. Catching this exception catches all exceptions. Please be aware that
 * Pdgen also throws Errors. These are the <a href="JoriaUserError">JoriaUserError</a> and the <a href="JoriaInternalError">JoriaInternalError</a>.
 */
public abstract class JoriaException extends Exception
{
    private static final long serialVersionUID = 7L;

    public JoriaException(String s)
	{
		super(s);
	}

	protected JoriaException(Throwable cause)
	{
		super(cause);
	}

	protected JoriaException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
