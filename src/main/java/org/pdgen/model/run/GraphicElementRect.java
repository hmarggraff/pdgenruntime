// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.CellDef;

import javax.swing.*;
import java.awt.*;

public class GraphicElementRect extends GraphElContent {
    private static final long serialVersionUID = 7L;

    private GraphicElementRect(GraphicElementRect from) {
        super(from);
    }

    public GraphicElementRect(Color color, CellDef src, ImageIcon backgroundImage) {
        super(color, src, backgroundImage);
    }

    public GraphElContent copy() {
        return new GraphicElementRect(this);
    }

    public float getContentWidth() {
        return width;
    }

    public void print(JoriaPrinter pr) {
        pr.printGERect(this);
    }
}
