// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;
import org.pdgen.model.run.OutputMode;
import org.pdgen.model.run.RVAny;

import java.awt.*;
import java.util.Stack;

public interface RDBase {
    RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException;

    ModelBase getModel();
}
