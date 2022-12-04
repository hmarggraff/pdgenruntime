// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

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
