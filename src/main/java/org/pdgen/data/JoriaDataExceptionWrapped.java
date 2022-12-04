// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.io.PrintStream;
import java.io.PrintWriter;

public class JoriaDataExceptionWrapped extends JoriaDataException
{
    private static final long serialVersionUID = 7L;

    public JoriaDataExceptionWrapped(String message, Throwable inner)
	{
		super(message, inner);
    }

	public void printStackTrace()
	{
		super.printStackTrace();
	}

	public void printStackTrace(PrintStream s)
	{
		super.printStackTrace(s);
	}

	public void printStackTrace(PrintWriter s)
	{
		super.printStackTrace(s);
	}
}
