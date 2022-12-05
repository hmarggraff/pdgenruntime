// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.env.JoriaException;

/**
 * User: patrick
 * Date: Dec 13, 2004
 * Time: 2:31:53 PM
 */
public interface AskVariablesHelper {
    boolean processMissingVariables(RuntimeParameter[] pvars, boolean multi, RunEnvImpl runEnv) throws JoriaException;

    boolean removeBoundVariable(RuntimeParameter pvar);
}
