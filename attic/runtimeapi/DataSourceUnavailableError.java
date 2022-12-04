package org.pdgen.runtimeapi;

/**
 * User: patrick
 * Date: Jun 4, 2007
 * Time: 7:16:08 AM
 */
public class DataSourceUnavailableError extends JoriaUserError
{
    private static final long serialVersionUID = 7L;

    public DataSourceUnavailableError(String message, boolean fatal)
    {
        super(message, fatal);
    }

    public DataSourceUnavailableError(String message, boolean fatal, Throwable cause)
    {
        super(message, fatal, cause);
    }

    public DataSourceUnavailableError(String s)
    {
        super(s);
    }

    public DataSourceUnavailableError(String s, Throwable cause)
    {
        super(s, cause);
    }
}
