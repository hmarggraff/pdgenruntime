// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Mar 16, 2005
 * Time: 12:19:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoriaParameter {
    String name;
    JoriaType type;

    public JoriaParameter(String name, JoriaType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public JoriaType getType() {
        return type;
    }
}
