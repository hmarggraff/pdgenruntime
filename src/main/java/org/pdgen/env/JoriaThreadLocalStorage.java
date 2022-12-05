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
public class JoriaThreadLocalStorage extends ThreadLocal {
    protected Object initialValue() {
        return new HashMap<Object, Object>();
    }

    public Map<Object, Object> getMap() {
        return (Map<Object, Object>) get();
    }
}
