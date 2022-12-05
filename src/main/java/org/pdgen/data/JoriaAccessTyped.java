// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.data;

import org.pdgen.model.run.RunEnv;

import java.util.Date;

public interface JoriaAccessTyped extends JoriaAccess {
    int ValForFalse = 0;
    int ValForTrue = 1;
    int ValForNull = 2;

    long getIntValue(DBObject from, RunEnv env) throws JoriaDataException;

    double getFloatValue(DBObject from, RunEnv env) throws JoriaDataException;

    /**
     * gets a boolean value directly without building dbLiterals
     *
     * @param from the containing object
     * @param env  the environmental information for aggregation etc
     * @return true false or null encoded as an int value
     * @throws JoriaDataException in case of problem
     */
    int getBooleanValue(DBObject from, RunEnv env) throws JoriaDataException;

    String getStringValue(DBObject from, RunEnv env) throws JoriaDataException;

    Date getDateValue(DBObject from, RunEnv env) throws JoriaDataException;

    Object getPictureValue(DBObject from, RunEnv env) throws JoriaDataException;
}
