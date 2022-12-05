// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

/**
 * User: patrick
 * Date: May 23, 2007
 * Time: 11:20:23 AM
 */
public interface AccessVisitor {
    boolean visit(JoriaAccess access);

    boolean stopAccessSearchOnError();
}
