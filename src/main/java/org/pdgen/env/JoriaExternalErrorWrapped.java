// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;
//MARKER The strings in this file shall not be translated

import java.io.PrintStream;
import java.io.PrintWriter;

public class JoriaExternalErrorWrapped extends JoriaUserError
{

    private static final long serialVersionUID = 7L;

    public JoriaExternalErrorWrapped(String s)
	{
		super(s);
	}

	public JoriaExternalErrorWrapped(String s, Throwable inner)
	{
		super(s, inner);
	}

	public void printStackTrace()
	{
		if (getCause() != null)
			getCause().printStackTrace();
		else
			System.err.println("JoriaExternalErrorWrapped - no inner");
		super.printStackTrace();
	}

	public void printStackTrace(PrintStream s)
	{
		if (getCause() != null)
			getCause().printStackTrace(s);
		else
			System.err.println("JoriaExternalErrorWrapped - no inner");
		super.printStackTrace(s);
	}

	public void printStackTrace(PrintWriter s)
	{
		if (getCause() != null)
			getCause().printStackTrace(s);
		else
			System.err.println("JoriaExternalErrorWrapped - no inner");
		super.printStackTrace(s);
	}
}
