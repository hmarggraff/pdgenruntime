// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.ModelBase;
import org.pdgen.model.RDBase;

import java.awt.*;
import java.util.Stack;

/**
 * built for 2nd and further lines of a multi line repeater
 * refers to the first repeater
 */
public class RDRepeaterNext implements RDBase {
    private final RDRepeater myRepeater;

    public RDRepeaterNext(RDBase r) {
        if (r instanceof RDRepeater)
            myRepeater = (RDRepeater) r;
        else
            myRepeater = ((RDRepeaterNext) r).myRepeater;
    }

    public RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException {
        return null;
    }

    public ModelBase getModel() {
        return myRepeater.getModel();
    }

    public RDRepeater getRDRepeater() {
        return myRepeater;
    }
}
