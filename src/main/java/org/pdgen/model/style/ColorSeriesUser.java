// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.style;

import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.JoriaInternalError;
import org.pdgen.model.run.RunEnv;
import org.pdgen.oql.JoriaQuery;
import org.pdgen.oql.OQLParser;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class ColorSeriesUser implements ColorSeries {
    private static final long serialVersionUID = 7L;
    final Color[] colors; // one more colors than condititions (default color)

    public ColorSeriesUser(Color[] colors) {
        this.colors = colors;
    }

    public Color getColorAt(int index, DBData val, RunEnv env)  {
        return colors[index%colors.length];
    }

    public void paintDesignerComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        final Rectangle bounds = g2.getClipBounds();
        final float width = bounds.width;
        final int slices = colors.length;
        float step = width / slices;
        float at = 0;
        final int height = bounds.height;
        for (Color color : colors) {
            g2.setColor(color);
            g2.fill(new Rectangle2D.Float(at, 0, step, height));
            at += step;
        }
    }

    public Color[] getColors() {
        return colors;
    }
}
