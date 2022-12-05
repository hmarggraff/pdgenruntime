// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.io.PrintStream;
import java.io.PrintWriter;

public class JoriaDataRetrievalExceptionInUserMethod extends Error {


    private static final long serialVersionUID = 7L;

    public JoriaDataRetrievalExceptionInUserMethod(String s, Exception ex) {
        super(s, ex);
    }

    public void printStackTrace() {
        super.printStackTrace();
    }

    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
    }

    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
    }

    public Exception getInner() {
        return (Exception) getCause();
    }
}
