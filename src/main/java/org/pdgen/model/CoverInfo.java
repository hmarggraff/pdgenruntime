// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.model.cells.CellDef;

/**
 * User: patrick
 * Date: Feb 21, 2007
 * Time: 12:47:45 PM
 */
public class CoverInfo
{
    public CellDef cell;
    public int row;
    public int col;

    public CoverInfo(CellDef cell, int row, int col)
    {
        this.cell = cell;
        this.row = row;
        this.col = col;
    }
}
