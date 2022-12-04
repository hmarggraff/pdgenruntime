// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import javax.swing.*;

/**
 * User: patrick
 * Date: Jul 12, 2006
 * Time: 1:38:42 PM
 */
public interface RVImageBase
{
    Icon getIcon(int i);

    Object getInformation(int i);

    boolean doSpread();

    double getHardScale();
}
