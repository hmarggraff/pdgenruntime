// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import java.util.Map;

/**
 * User: patrick
 * Date: Nov 29, 2004
 * Time: 10:03:00 AM
 */
public interface JoriaReportPrivateAccess extends JoriaAccess {
    JoriaAccess copyReportPrivateAccess(Map<Object, Object> copiedData);
}
