// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.model.run.RunEnv;

/**
 * Created by IntelliJ IDEA.
 * User: patrick
 * Date: Mar 16, 2005
 * Time: 12:17:11 PM
 * To change this template use File | Settings | File Templates.
 */
public interface JoriaAccessWithParameters extends JoriaAccess
{
    String getLongName();
    JoriaClass getDefiningClass();
    JoriaType getType();
    DBData getValue(DBData from, JoriaAccess asView, RunEnv env, DBData[] parameterValues) throws JoriaDataException;
    boolean isRootUsable();
    JoriaParameter[] getParameters();
}
