// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.DBData;
import org.pdgen.model.run.RunEnv;

import java.awt.*;

public class ColorSeriesSingle implements ColorSeries {
    private static final long serialVersionUID = 7L;
    final Color myColor;

    public ColorSeriesSingle(Color color) {
        myColor = color;
    }

    @Override
    public Color getColorAt(int index, DBData value, RunEnv env) {
        return  myColor;
    }

    public Color getColor() {
        return myColor;
    }

    public void paintDesignerComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        final Rectangle bounds = g2.getClipBounds();
        g2.setColor(myColor);
        g2.fill(bounds);
    }

}
