// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import org.pdgen.data.JoriaDataException;

public class JoriaUserDataException extends JoriaDataException {

    private static final long serialVersionUID = 7L;

    public JoriaUserDataException(String message) {
        super(message);
    }

    public JoriaUserDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
