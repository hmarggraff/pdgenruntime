// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;
import org.pdgen.model.cells.EnvValueCell;
import org.pdgen.model.style.CellStyle;

import java.awt.*;

public class RVEnvValue extends RVString
{
    public RVEnvValue(String string, CellStyle cs, Graphics2D g)
    {
        super(string, cs, g);
    }

    public RVEnvValue(CellDef rb, RunEnvImpl env)
    {
        EnvValueCell cd = (EnvValueCell) rb;
        string = cd.getFormattedString(env.getPager());
    }

}
