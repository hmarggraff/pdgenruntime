package org.pdgen.runtimeapi;

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
