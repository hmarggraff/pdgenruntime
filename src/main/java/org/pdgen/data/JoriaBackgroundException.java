// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.env.Res;

/**
 * User: Patrick
 * Date: Jul 26, 2006
 * Time: 4:41:10 PM
 */
public class JoriaBackgroundException extends JoriaDataException {
    private static final long serialVersionUID = 7L;

    public JoriaBackgroundException(Throwable cause) {
        super(Res.asis("none"), cause);
    }
}
