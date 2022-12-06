// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.env;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Feb 25, 2005
 * Time: 2:56:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoriaThreadLocalStorage extends ThreadLocal<Map<Object,Object>> {

    @Override
    protected Map<Object,Object> initialValue() {
        return  new HashMap<>();
    }

    public Map<Object,Object> getMap() {
        return get();
    }
}
