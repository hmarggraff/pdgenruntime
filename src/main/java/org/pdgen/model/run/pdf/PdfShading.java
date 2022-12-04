// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run.pdf;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Nov 17, 2004
 * Time: 9:24:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class PdfShading
{
    PdfShading(byte[] on, Color c1, Color c2, Point2D p1, Point2D p2)
    {
        outName = on;
        this.c1 = c1;
        this.c2 = c2;
        this.p1 = p1;
        this.p2 = p2;
    }

    public byte[] outName;
    public Color c1;
    public Color c2;
    public Point2D p1;
    public Point2D p2;
}
