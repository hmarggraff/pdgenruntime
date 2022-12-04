// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.DBData;

/**
 * User: hmf
 * Date: Jun 24, 2005
 * Time: 3:49:09 PM
 */
public interface LabeledDBObjectBase extends Comparable<LabeledDBObjectBase>{
    DBData getLabel();
}
