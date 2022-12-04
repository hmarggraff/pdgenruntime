// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data.view;

import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Apr 4, 2003
 * Time: 11:30:44 AM
 * To change this template use Options | File Templates.
 */
public interface CollectUsedViewsAccess
{
    void  collectUsedViews(Set<MutableView> s);

    void collectViewUsage(Map<MutableView, Set<Object>> viewUsage, Set<MutableView> visitedViews);
}
