// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.model.run.OutputMode;
import org.pdgen.model.run.RVAny;
import org.pdgen.data.DBData;
import org.pdgen.data.JoriaDataException;

import java.util.Stack;
import java.awt.*;

public interface RDBase
{
    RVAny buildRunValue(DBData from, OutputMode outMode, Stack<RDBase> defs, Stack<RVAny> outerVals, Graphics2D g) throws JoriaDataException;
    ModelBase getModel();
}
