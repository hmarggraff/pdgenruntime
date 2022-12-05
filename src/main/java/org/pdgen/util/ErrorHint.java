// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

public class ErrorHint {
    protected String pattern;
    protected String hint;

    public ErrorHint(String msg, String hint) {
        pattern = msg;
        this.hint = hint;
    }

    public boolean matches(String err) {
        return err.startsWith(pattern);
    }

    public String getHint() {
        return hint;
    }
}
