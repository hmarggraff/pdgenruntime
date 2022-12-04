// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

public class JoriaAssertionError extends Error {
    private static final long serialVersionUID = 7L;

    public JoriaAssertionError(String s) {
		 super(s);
		}

	public JoriaAssertionError(String s, Throwable e)
	{
		super(s, e);
	}
}
