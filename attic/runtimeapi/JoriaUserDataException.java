package org.pdgen.runtimeapi;

import org.pdgen.data.JoriaDataException;

public class JoriaUserDataException extends JoriaDataException
{

    private static final long serialVersionUID = 7L;

    public JoriaUserDataException(String message)
	{
		super(message);
	}

    public JoriaUserDataException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
