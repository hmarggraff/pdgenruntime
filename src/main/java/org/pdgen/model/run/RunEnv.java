// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.model.run;

import org.jetbrains.annotations.NotNull;
import org.pdgen.data.*;
import org.pdgen.data.view.RuntimeParameter;
import org.pdgen.data.view.SortOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public interface RunEnv {
    void putRuntimeParameter(JoriaAccess key, DBData val);

    Locale getLocale();

    void setLocale(Locale newLocale);

    int getDisplayPageNo();

    int getTotalPagesNumber();

    void putCounter(RuntimeParameter key, DBIntImplMutable counter);

    Object getDatabaseConnection();

    void setDatabaseConnection(Object databaseConnection);

    String getServiceRootValue();

    Properties getUserStorage();

    DBData popFromObjectPath();

    void pushToObjectPath(DBData step);

    HashMap<JoriaCollection, SortOrder[]> getRuntimeOverrides();

    AggregateCollector getPager();

    HashMap<?, ?> getConnectorMap();

    boolean isConnectorMapUsed();

    void removeVariable(JoriaAccess key);

    boolean isReaskVariables();

    int getTransactionCounter();

    void setTransactionCounter(int transactionCounter);

    @SuppressWarnings("UnusedDeclaration")
    HashMap<JoriaClass, List<JoriaAccess>> getPhysicalAccessors();

    DBData loadRootVal() throws JoriaDataException;

    DBData getRootVal();

    DBData getRuntimeParameterValue(@NotNull JoriaAccess param);
}