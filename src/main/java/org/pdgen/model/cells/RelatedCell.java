// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.cells;

/**
 * this relates two cells. The role indicates the role of the originatig cell
 * for the desination cell.
 */
public interface RelatedCell extends CellDef {
    CellDef getDest();

    void setDest(CellDef newDest);
}

