// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

/**
 * Is thrown when an unexpected condition occurs, that must be fixed by the user.
 * E.g. Existence and correctness of required configuration files etc.
 */
public class JoriaUserError extends Error
{
    private static final long serialVersionUID = 7L;
    boolean exitRequested;
	/*
	 * The mesage containsName a decription of what went wrong.
	 */
	public JoriaUserError(String s)
	{
		super(s);
	}

	public JoriaUserError(String message, boolean fatal)
	{
		super(message);
		exitRequested = fatal;
	}

    public JoriaUserError(String s, Throwable cause)
    {
        super(s, cause);
    }

    public JoriaUserError(String message, boolean fatal, Throwable cause)
    {
        super(message, cause);
        exitRequested = fatal;
    }

	public boolean isExitRequested()
	{
		return exitRequested;
	}

}

