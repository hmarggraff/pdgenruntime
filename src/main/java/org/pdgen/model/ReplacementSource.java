// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model;

import org.pdgen.model.run.RunEnv;
import org.pdgen.data.JoriaDataException;

import java.util.ArrayList;

/**
 * User: patrick
 * Date: 04.09.2007
 * Time: 17:27:02
 */
public interface ReplacementSource
{
    ArrayList<String> getValues(RunEnv env) throws JoriaDataException;
}
