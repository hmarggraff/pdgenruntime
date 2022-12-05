// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.model.cells.DeferredTotalPagesCell;

import java.awt.*;
import java.io.Serializable;

/**
 * User: hmf
 * Date: 15.10.2008
 */
public interface GrahElPostprocess extends Shape, Cloneable, Serializable, GraphicElement {
    /**
     * update the text after some postporcessing (totalPages, table of contents etc
     *
     * @param newText the actual text to use
     * @param g       context to use
     */
    void setText(String newText, Graphics2D g);

    DeferredTotalPagesCell getPostprocessSource();

    int getPosInDisplayList();

    void setPosInDisplayList(int posInDisplayList);
}
