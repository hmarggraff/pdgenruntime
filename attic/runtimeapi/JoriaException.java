package org.pdgen.runtimeapi;

/**
 * This is the base class of all exceptions that are thrown by ReportsAnywheres
 * api functions. Catching this exception catches all exceptions. Please be aware that
 * ReportsAnywhere also throws Errors. These are the <a href="JoriaUserError">JoriaUserError</a> and the <a href="JoriaInternalError">JoriaInternalError</a>.
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
