package org.pdgen.runtimeapi;

public class JoriaInternalError extends Error
{
    private static final long serialVersionUID = 7L;

    public JoriaInternalError(String s)
    {
        super(s);
    }

    public JoriaInternalError(String s, Throwable inner)
    {
        super(s, inner);
    }

    public Throwable getInner()
    {
        return getCause();
    }
}
