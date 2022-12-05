// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

import java.awt.*;
import java.util.Locale;

public interface TextCellLayouter {
    void paint(Graphics2D g, float x0, float y0, float w, float h, Locale loc);

    void calcSize(Locale loc, Graphics2D g);
}
