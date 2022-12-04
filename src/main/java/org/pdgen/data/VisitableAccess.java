// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.util.Set;

/**
 * User: patrick
 * Date: May 23, 2007
 * Time: 11:36:33 AM
 */
public interface VisitableAccess extends JoriaAccess
{
    boolean visitAllAccesses(AccessVisitor visitor, Set<JoriaAccess> seen);
}
