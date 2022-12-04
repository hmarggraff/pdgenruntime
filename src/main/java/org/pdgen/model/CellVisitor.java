// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.model.cells.CellDef;

/**
 * User: patrick
 * Date: Feb 8, 2006
 * Time: 9:34:22 AM
 */
public interface CellVisitor
{
    boolean visit(CellDef cd, int r, int c);
}
