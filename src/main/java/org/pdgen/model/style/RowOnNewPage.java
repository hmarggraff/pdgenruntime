// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.JoriaAssertionError;
import org.pdgen.env.Res;

import java.io.Serializable;

/**
 * User: patrick
 * Date: Jul 28, 2006
 * Time: 9:59:52 AM
 */
public class RowOnNewPage implements Serializable {
    private static final long serialVersionUID = 7L;
    private final int linesBeforeNewLine;
    public static final int ALWAYS = 1;
    public static final RowOnNewPage always = new RowOnNewPage(ALWAYS);
    public static final int ON_SECOND_ROW = 2;
    public static final RowOnNewPage onSecondRow = new RowOnNewPage(ON_SECOND_ROW);
    public static final int NEVER = 0;
    public static final RowOnNewPage never = new RowOnNewPage(NEVER);

    private RowOnNewPage(int linesBeforeNewLine) {
        this.linesBeforeNewLine = linesBeforeNewLine;
    }

    public String toString() {
        if (linesBeforeNewLine == ALWAYS)
            return Res.str("always");
        if (linesBeforeNewLine == ON_SECOND_ROW)
            return Res.str("on_second_row");
        if (linesBeforeNewLine == NEVER)
            return Res.str("never");
        throw new JoriaAssertionError("unexpected value " + linesBeforeNewLine);
    }

    public int getLinesBeforeNewLine() {
        return linesBeforeNewLine;
    }

    protected Object readResolve() {
        if (linesBeforeNewLine == ALWAYS)
            return always;
        else if (linesBeforeNewLine == ON_SECOND_ROW)
            return onSecondRow;
        else if (linesBeforeNewLine == NEVER)
            return never;
        else throw new JoriaAssertionError("unexpected value " + linesBeforeNewLine);
    }

}
