// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import java.awt.*;
import java.awt.geom.AffineTransform;


/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Jan 15, 2003
 * Time: 7:19:20 AM
 * To change this template use Options | File Templates.
 */
public class DisplayPrinter extends Graphics2DPrinter {
    public static Point offset = new Point(10, 10);

    public DisplayPrinter(Graphics2D gr) {
        super(gr);
    }

    public void printDecoration(GrelViewer gv) {
        Rectangle cr = g.getClipBounds();
        g.setColor(Color.lightGray);
        g.fill(cr);
        g.setColor(Color.darkGray);
        g.fillRect(20, 20, gv.getPageWidth(), gv.getPageHeight());
        g.setColor(Color.white);
        g.fillRect(10, 10, gv.getPageWidth(), gv.getPageHeight());
        g.translate(offset.getX(), offset.getY());
        if (gv.getScale() != 1) {
            AffineTransform transform = g.getTransform();
            transform.scale(gv.getScale(), gv.getScale());
            g.setTransform(transform);
        }

        super.printDecoration(gv);
    }

}
