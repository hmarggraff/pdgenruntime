// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.DBData;
import org.pdgen.model.run.RunEnv;

import java.awt.*;
import java.io.Serializable;

public interface ColorSeries extends Serializable {
    void paintDesignerComponent(Graphics g);

    Color getColorAt(int index, DBData value, RunEnv env);

}
